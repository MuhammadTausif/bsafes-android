// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "third_party/blink/renderer/modules/scheduler/window_scheduler.h"

#include "third_party/blink/renderer/core/frame/local_dom_window.h"
#include "third_party/blink/renderer/modules/scheduler/scheduler.h"

namespace blink {

Scheduler* WindowScheduler::scheduler(LocalDOMWindow& window) {
  if (Document* document = window.document()) {
    return Scheduler::From(*document);
  }
  return nullptr;
}

}  // namespace blink
