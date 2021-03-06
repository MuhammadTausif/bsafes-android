// Copyright (c) 2012 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "net/cert/x509_certificate_net_log_param.h"

#include <memory>
#include <string>
#include <utility>
#include <vector>

#include "base/values.h"
#include "net/cert/x509_certificate.h"
#include "net/log/net_log_capture_mode.h"

namespace net {

base::Value NetLogX509CertificateParams(const X509Certificate* certificate) {
  base::Value dict(base::Value::Type::DICTIONARY);
  base::Value certs(base::Value::Type::LIST);
  std::vector<std::string> encoded_chain;
  certificate->GetPEMEncodedChain(&encoded_chain);
  for (auto& pem : encoded_chain)
    certs.GetList().emplace_back(std::move(pem));
  dict.SetKey("certificates", std::move(certs));
  return dict;
}

}  // namespace net
