// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#import "chrome/browser/ui/cocoa/color_chooser_mac.h"

#include "base/logging.h"
#include "chrome/browser/ui/color_chooser.h"
#include "components/remote_cocoa/app_shim/color_panel_bridge.h"
#include "components/remote_cocoa/browser/application_host.h"
#include "components/remote_cocoa/browser/window.h"
#include "mojo/public/cpp/bindings/interface_request.h"
#include "mojo/public/cpp/bindings/strong_binding.h"
#include "skia/ext/skia_utils_mac.h"

namespace {
// The currently active color chooser.
ColorChooserMac* g_current_color_chooser = nullptr;
}  // namespace

// static
ColorChooserMac* ColorChooserMac::Open(content::WebContents* web_contents,
                                       SkColor initial_color) {
  if (g_current_color_chooser)
    g_current_color_chooser->End();
  DCHECK(!g_current_color_chooser);
  // Note that WebContentsImpl::ColorChooser ultimately takes ownership (and
  // deletes) the returned pointer.
  g_current_color_chooser = new ColorChooserMac(web_contents, initial_color);
  return g_current_color_chooser;
}

ColorChooserMac::ColorChooserMac(content::WebContents* web_contents,
                                 SkColor initial_color)
    : web_contents_(web_contents), mojo_host_binding_(this) {
  remote_cocoa::mojom::ColorPanelHostPtr mojo_host_ptr;
  mojo_host_binding_.Bind(mojo::MakeRequest(&mojo_host_ptr));
  auto* application_host = remote_cocoa::ApplicationHost::GetForNativeView(
      web_contents ? web_contents->GetNativeView() : gfx::NativeView());
  if (application_host) {
    application_host->GetApplication()->ShowColorPanel(
        mojo::MakeRequest(&mojo_panel_ptr_), std::move(mojo_host_ptr));
  } else {
    mojo::MakeStrongBinding(std::make_unique<remote_cocoa::ColorPanelBridge>(
                                std::move(mojo_host_ptr)),
                            mojo::MakeRequest(&mojo_panel_ptr_));
  }
  mojo_panel_ptr_->Show(initial_color);
}

ColorChooserMac::~ColorChooserMac() {
  // Always call End() before destroying.
  DCHECK_NE(g_current_color_chooser, this);
}

void ColorChooserMac::DidChooseColorInColorPanel(SkColor color) {
  DCHECK_EQ(g_current_color_chooser, this);
  if (web_contents_)
    web_contents_->DidChooseColorInColorChooser(color);
}

void ColorChooserMac::DidCloseColorPanel() {
  DCHECK_EQ(g_current_color_chooser, this);
  End();
}

void ColorChooserMac::End() {
  if (g_current_color_chooser == this) {
    g_current_color_chooser = nullptr;
    if (web_contents_)
      web_contents_->DidEndColorChooser();
  }
}

void ColorChooserMac::SetSelectedColor(SkColor color) {
  mojo_panel_ptr_->SetSelectedColor(color);
}

namespace chrome {
content::ColorChooser* ShowColorChooser(content::WebContents* web_contents,
                                        SkColor initial_color) {
  return ColorChooserMac::Open(web_contents, initial_color);
}
}  // namepace chrome
