// Copyright 2017 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "third_party/blink/renderer/core/messaging/blink_cloneable_message.h"

namespace blink {

BlinkCloneableMessage::BlinkCloneableMessage() = default;
BlinkCloneableMessage::~BlinkCloneableMessage() = default;

BlinkCloneableMessage::BlinkCloneableMessage(BlinkCloneableMessage&&) noexcept =
    default;
BlinkCloneableMessage& BlinkCloneableMessage::operator=(
    BlinkCloneableMessage&&) noexcept = default;

}  // namespace blink
