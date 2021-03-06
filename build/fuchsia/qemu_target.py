# Copyright 2018 The Chromium Authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.

"""Implements commands for running and interacting with Fuchsia on QEMU."""

import boot_data
import common
import logging
import md5
import os
import platform
import shutil
import socket
import subprocess
import sys
import target
import tempfile
import time

from common import GetQemuRootForPlatform, EnsurePathExists


# Virtual networking configuration data for QEMU.
GUEST_NET = '192.168.3.0/24'
GUEST_IP_ADDRESS = '192.168.3.9'
HOST_IP_ADDRESS = '192.168.3.2'
GUEST_MAC_ADDRESS = '52:54:00:63:5e:7b'

# Capacity of the system's blobstore volume.
EXTENDED_BLOBSTORE_SIZE = 1073741824  # 1GB


class QemuTarget(target.Target):
  def __init__(self, output_dir, target_cpu, cpu_cores, system_log_file,
               require_kvm, ram_size_mb=2048):
    """output_dir: The directory which will contain the files that are
                   generated to support the QEMU deployment.
    target_cpu: The emulated target CPU architecture.
                Can be 'x64' or 'arm64'."""
    super(QemuTarget, self).__init__(output_dir, target_cpu)
    self._qemu_process = None
    self._ram_size_mb = ram_size_mb
    self._system_log_file = system_log_file
    self._cpu_cores = cpu_cores
    self._require_kvm = require_kvm

  def __enter__(self):
    return self

  # Used by the context manager to ensure that QEMU is killed when the Python
  # process exits.
  def __exit__(self, exc_type, exc_val, exc_tb):
    self.Shutdown();

  def Start(self):
    boot_data.AssertBootImagesExist(self._GetTargetSdkArch(), 'qemu')

    qemu_path = os.path.join(GetQemuRootForPlatform(), 'bin',
                             'qemu-system-' + self._GetTargetSdkLegacyArch())
    kernel_args = boot_data.GetKernelArgs(self._output_dir)

    # TERM=dumb tells the guest OS to not emit ANSI commands that trigger
    # noisy ANSI spew from the user's terminal emulator.
    kernel_args.append('TERM=dumb')

    # Enable logging to the serial port. This is a temporary fix to investigate
    # the root cause for https://crbug.com/869753 .
    kernel_args.append('kernel.serial=legacy')

    qemu_command = [qemu_path,
        '-m', str(self._ram_size_mb),
        '-nographic',
        '-kernel', EnsurePathExists(
            boot_data.GetTargetFile('qemu-kernel.kernel',
                                    self._GetTargetSdkArch(),
                                    boot_data.TARGET_TYPE_QEMU)),
        '-initrd', EnsurePathExists(
            boot_data.GetBootImage(self._output_dir, self._GetTargetSdkArch(),
                                   boot_data.TARGET_TYPE_QEMU)),
        '-smp', str(self._cpu_cores),

        # Attach the blobstore and data volumes. Use snapshot mode to discard
        # any changes.
        '-snapshot',
        '-drive', 'file=%s,format=qcow2,if=none,id=blobstore,snapshot=on' %
                    _EnsureBlobstoreQcowAndReturnPath(self._output_dir,
                                                      self._GetTargetSdkArch()),
        '-device', 'virtio-blk-pci,drive=blobstore',

        # Use stdio for the guest OS only; don't attach the QEMU interactive
        # monitor.
        '-serial', 'stdio',
        '-monitor', 'none',

        '-append', ' '.join(kernel_args)
      ]

    # Configure the machine to emulate, based on the target architecture.
    if self._target_cpu == 'arm64':
      qemu_command.extend([
          '-machine','virt',
      ])
      netdev_type = 'virtio-net-pci'
    else:
      qemu_command.extend([
          '-machine', 'q35',
      ])
      netdev_type = 'e1000'

    # Configure the CPU to emulate.
    # On Linux, we can enable lightweight virtualization (KVM) if the host and
    # guest architectures are the same.
    enable_kvm = self._require_kvm or (sys.platform.startswith('linux') and (
        (self._target_cpu == 'arm64' and platform.machine() == 'aarch64') or
        (self._target_cpu == 'x64' and platform.machine() == 'x86_64')) and
      os.access('/dev/kvm', os.R_OK | os.W_OK))
    if enable_kvm:
      qemu_command.extend(['-enable-kvm', '-cpu', 'host,migratable=no'])
    else:
      logging.warning('Unable to launch QEMU with KVM acceleration.')
      if self._target_cpu == 'arm64':
        qemu_command.extend(['-cpu', 'cortex-a53'])
      else:
        qemu_command.extend(['-cpu', 'Haswell,+smap,-check,-fsgsbase'])

    # Configure virtual network. It is used in the tests to connect to
    # testserver running on the host.
    netdev_config = 'user,id=net0,net=%s,dhcpstart=%s,host=%s' % \
            (GUEST_NET, GUEST_IP_ADDRESS, HOST_IP_ADDRESS)

    self._host_ssh_port = common.GetAvailableTcpPort()
    netdev_config += ",hostfwd=tcp::%s-:22" % self._host_ssh_port
    qemu_command.extend([
      '-netdev', netdev_config,
      '-device', '%s,netdev=net0,mac=%s' % (netdev_type, GUEST_MAC_ADDRESS),
    ])

    # We pass a separate stdin stream to qemu. Sharing stdin across processes
    # leads to flakiness due to the OS prematurely killing the stream and the
    # Python script panicking and aborting.
    # The precise root cause is still nebulous, but this fix works.
    # See crbug.com/741194.
    logging.debug('Launching QEMU.')
    logging.debug(' '.join(qemu_command))

    # Zircon sends debug logs to serial port (see kernel.serial=legacy flag
    # above). Serial port is redirected to a file through QEMU stdout.
    # Unless a |_system_log_file| is explicitly set, we output the kernel serial
    # log to a temporary file, and print that out if we are unable to connect to
    # the QEMU guest, to make it easier to diagnose connectivity issues.
    temporary_system_log_file = None
    if self._system_log_file:
      stdout = self._system_log_file
      stderr = subprocess.STDOUT
    else:
      temporary_system_log_file = tempfile.NamedTemporaryFile('w')
      stdout = temporary_system_log_file
      stderr = sys.stderr

    self._qemu_process = subprocess.Popen(qemu_command, stdin=open(os.devnull),
                                          stdout=stdout, stderr=stderr)
    try:
      self._WaitUntilReady();
    except target.FuchsiaTargetException:
      if temporary_system_log_file:
        logging.info("Kernel logs:\n" +
                     open(temporary_system_log_file.name, 'r').read())
      raise

  def Shutdown(self):
    if self._IsQemuStillRunning():
      logging.info('Shutting down QEMU.')
      self._qemu_process.kill()

  def _IsQemuStillRunning(self):
    if not self._qemu_process:
      return False
    return os.waitpid(self._qemu_process.pid, os.WNOHANG)[0] == 0

  def _GetEndpoint(self):
    if not self._IsQemuStillRunning():
      raise Exception('QEMU quit unexpectedly.')
    return ('localhost', self._host_ssh_port)

  def _GetSshConfigPath(self):
    return boot_data.GetSSHConfigPath(self._output_dir)


def _ComputeFileHash(filename):
  hasher = md5.new()
  with open(filename, 'rb') as f:
    buf = f.read(4096)
    while buf:
      hasher.update(buf)
      buf = f.read(4096)

  return hasher.hexdigest()


def _EnsureBlobstoreQcowAndReturnPath(output_dir, target_arch):
  """Returns a file containing the Fuchsia blobstore in a QCOW format,
  with extra buffer space added for growth."""

  qimg_tool = os.path.join(common.GetQemuRootForPlatform(), 'bin', 'qemu-img')
  fvm_tool = os.path.join(common.SDK_ROOT, 'tools', 'fvm')
  blobstore_path = boot_data.GetTargetFile('storage-full.blk', target_arch,
                                           'qemu')
  qcow_path = os.path.join(output_dir, 'gen', 'blobstore.qcow')

  # Check a hash of the blobstore to determine if we can re-use an existing
  # extended version of it.
  blobstore_hash_path = os.path.join(output_dir, 'gen', 'blobstore.hash')
  current_blobstore_hash = _ComputeFileHash(blobstore_path)

  if os.path.exists(blobstore_hash_path) and os.path.exists(qcow_path):
    if current_blobstore_hash == open(blobstore_hash_path, 'r').read():
      return qcow_path

  # Add some extra room for growth to the Blobstore volume.
  # Fuchsia is unable to automatically extend FVM volumes at runtime so the
  # volume enlargement must be performed prior to QEMU startup.

  # The 'fvm' tool only supports extending volumes in-place, so make a
  # temporary copy of 'blobstore.bin' before it's mutated.
  extended_blobstore = tempfile.NamedTemporaryFile()
  shutil.copyfile(blobstore_path, extended_blobstore.name)
  subprocess.check_call([fvm_tool, extended_blobstore.name, 'extend',
                         '--length', str(EXTENDED_BLOBSTORE_SIZE),
                         blobstore_path])

  # Construct a QCOW image from the extended, temporary FVM volume.
  # The result will be retained in the build output directory for re-use.
  subprocess.check_call([qimg_tool, 'convert', '-f', 'raw', '-O', 'qcow2',
                         '-c', extended_blobstore.name, qcow_path])

  # Write out a hash of the original blobstore file, so that subsequent runs
  # can trivially check if a cached extended FVM volume is available for reuse.
  with open(blobstore_hash_path, 'w') as blobstore_hash_file:
    blobstore_hash_file.write(current_blobstore_hash)

  return qcow_path


