// Copyright 2018 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

Polymer({
  is: 'discards-main',

  properties: {
    selected: {
      type: Number,
      value: 0,
    },

    tabs: {
      type: Array,
      value: () => ['Discards', 'Database', 'Graph'],
    },
  },
});
