// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "third_party/blink/renderer/platform/graphics/dark_mode_filter.h"

#include "base/optional.h"
#include "cc/paint/paint_flags.h"
#include "testing/gtest/include/gtest/gtest.h"
#include "third_party/blink/renderer/platform/graphics/color.h"
#include "third_party/blink/renderer/platform/graphics/dark_mode_settings.h"
#include "third_party/skia/include/core/SkColor.h"

namespace blink {
namespace {

// TODO(gilmanmh): Add expectations for image inversion to each test.
TEST(DarkModeFilterTest, DoNotApplyFilterWhenDarkModeIsOff) {
  DarkModeFilter filter;

  DarkModeSettings settings;
  settings.mode = DarkMode::kOff;
  filter.UpdateSettings(settings);

  EXPECT_EQ(Color::kWhite,
            filter.InvertColorIfNeeded(
                Color::kWhite, DarkModeFilter::ElementRole::kBackground));
  EXPECT_EQ(Color::kBlack,
            filter.InvertColorIfNeeded(
                Color::kBlack, DarkModeFilter::ElementRole::kBackground));

  EXPECT_EQ(base::nullopt,
            filter.ApplyToFlagsIfNeeded(
                cc::PaintFlags(), DarkModeFilter::ElementRole::kBackground));
}

TEST(DarkModeFilterTest, ApplyDarkModeToColorsAndFlags) {
  DarkModeFilter filter;

  DarkModeSettings settings;
  settings.mode = DarkMode::kSimpleInvertForTesting;
  filter.UpdateSettings(settings);

  EXPECT_EQ(Color::kBlack,
            filter.InvertColorIfNeeded(
                Color::kWhite, DarkModeFilter::ElementRole::kBackground));
  EXPECT_EQ(Color::kWhite,
            filter.InvertColorIfNeeded(
                Color::kBlack, DarkModeFilter::ElementRole::kBackground));

  cc::PaintFlags flags;
  flags.setColor(SK_ColorWHITE);
  auto flags_or_nullopt = filter.ApplyToFlagsIfNeeded(
      flags, DarkModeFilter::ElementRole::kBackground);
  ASSERT_NE(flags_or_nullopt, base::nullopt);
  EXPECT_EQ(SK_ColorBLACK, flags_or_nullopt.value().getColor());
}

}  // namespace
}  // namespace blink
