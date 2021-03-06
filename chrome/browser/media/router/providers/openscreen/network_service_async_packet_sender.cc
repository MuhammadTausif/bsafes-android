// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "chrome/browser/media/router/providers/openscreen/network_service_async_packet_sender.h"

#include <utility>

#include "net/base/completion_once_callback.h"
#include "net/traffic_annotation/network_traffic_annotation.h"

#include "mojo/public/cpp/bindings/interface_request.h"

namespace media_router {
NetworkServiceAsyncPacketSender::NetworkServiceAsyncPacketSender(
    network::mojom::NetworkContext* network_context) {
  network::mojom::UDPSocketRequest socket_request(mojo::MakeRequest(&socket_));
  network_context->CreateUDPSocket(std::move(socket_request), nullptr);
}

NetworkServiceAsyncPacketSender::NetworkServiceAsyncPacketSender(
    NetworkServiceAsyncPacketSender&&) = default;
NetworkServiceAsyncPacketSender::~NetworkServiceAsyncPacketSender() = default;

net::Error NetworkServiceAsyncPacketSender::SendTo(
    const net::IPEndPoint& dest_addr,
    base::span<const uint8_t> data,
    base::OnceCallback<void(int32_t)> callback) {
  // The socket may still be initializing, or just broken: no way to tell here.
  if (!socket_.is_bound()) {
    return net::Error::ERR_SOCKET_NOT_CONNECTED;
  }

  // Socket send errors are handled by Mojo setting the int32_t parameter
  // of the callback function to a negative value.
  net::MutableNetworkTrafficAnnotationTag annotation{};
  socket_->SendTo(dest_addr, data, annotation, std::move(callback));

  return net::Error::OK;
}

}  // namespace media_router
