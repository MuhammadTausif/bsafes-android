// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package org.chromium.chrome.browser.metrics;

/**
 * Emits background task memory usage UMA metrics.
 */
public class BackgroundTaskMemoryMetricsEmitter {
    /**
     * Emits background task memory usage UMA metrics once.
     * @param isReducedMode Whether Chrome is running in Reduced Mode.
     * @param taskTypeAffix Which affix to add to identify memory histograms specific to the task
     *         type. For example, if this is "OfflinePrefetch", histograms
     *         "Memory.BackgroundTask.OfflinePrefetch.Browser.*" will be emitted. If null, no
     *         specific histogram is emitted for this task. The generic
     *         "Memory.BackgroundTask.Browser.*" histograms will still be emitted, regardless.
     */
    public static void reportMemoryUsage(boolean isReducedMode, String taskTypeAffix) {
        nativeReportMemoryUsage(isReducedMode, taskTypeAffix);
    }

    private static native void nativeReportMemoryUsage(boolean isReducedMode, String taskTypeAffix);
}
