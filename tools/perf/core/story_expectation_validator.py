#!/usr/bin/env vpython
# Copyright 2017 The Chromium Authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
"""Script to check validity of StoryExpectations."""

import logging
import os


from core import benchmark_utils
from core import benchmark_finders
from core import path_util
path_util.AddTelemetryToPath()
path_util.AddAndroidPylibToPath()

from typ import expectations_parser as typ_expectations_parser


CLUSTER_TELEMETRY_DIR = os.path.join(
    path_util.GetChromiumSrcDir(), 'tools', 'perf', 'contrib',
    'cluster_telemetry')
CLUSTER_TELEMETRY_BENCHMARKS = [
    ct_benchmark.Name() for ct_benchmark in
    benchmark_finders.GetBenchmarksInSubDirectory(CLUSTER_TELEMETRY_DIR)
]
MOBILE_PREFIXES = {'android', 'mobile'}
DESKTOP_PREFIXES = {'chromeos', 'desktop', 'linux', 'mac', 'win'}


def is_desktop_tag(tag):
    return any(tag.lower().startswith(t) for t in DESKTOP_PREFIXES)


def is_mobile_tag(tag):
    return any(tag.lower().startswith(t) for t in MOBILE_PREFIXES)


def validate_story_names(benchmarks, test_expectations):
  stories = []
  for benchmark in benchmarks:
    if benchmark.Name() in CLUSTER_TELEMETRY_BENCHMARKS:
      continue
    story_set = benchmark_utils.GetBenchmarkStorySet(benchmark())
    stories.extend([benchmark.Name() + '/' + s.name for s in story_set.stories])
  broken_expectations = test_expectations.check_for_broken_expectations(stories)
  unused_patterns = ''
  for pattern in set([e.test for e in broken_expectations]):
    unused_patterns += ("Expectations with pattern '%s'"
                        " do not apply to any stories\n" % pattern)
  assert not unused_patterns, unused_patterns


def validate_expectations_component_tags(test_expectations):
  expectations = []
  for exps in test_expectations.individual_exps.values():
    expectations.extend(exps)
  for exps in test_expectations.glob_exps.values():
    expectations.extend(exps)
  for e in expectations:
    if len(e.tags) > 1:
      has_mobile_tags = any(is_mobile_tag(t) for t in e.tags)
      has_desktop_tags = any(is_desktop_tag(t) for t in e.tags)
      assert not (has_mobile_tags and has_desktop_tags), (
              ("Expectation on %d is mixing "
               "mobile and desktop condition tags") % e.lineno)


def main():
  benchmarks = benchmark_finders.GetAllBenchmarks()
  with open(path_util.GetExpectationsPath()) as fp:
    raw_expectations_data = fp.read()
  test_expectations = typ_expectations_parser.TestExpectations()
  ret, msg = test_expectations.parse_tagged_list(raw_expectations_data)
  if ret:
    logging.error(msg)
    return ret
  validate_story_names(benchmarks, test_expectations)
  validate_expectations_component_tags(test_expectations)
  return 0
