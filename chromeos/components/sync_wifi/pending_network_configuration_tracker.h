// Copyright 2019 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#ifndef CHROMEOS_COMPONENTS_SYNC_WIFI_PENDING_NETWORK_CONFIGURATION_TRACKER_H_
#define CHROMEOS_COMPONENTS_SYNC_WIFI_PENDING_NETWORK_CONFIGURATION_TRACKER_H_

#include <string>

#include "base/macros.h"
#include "base/optional.h"
#include "chromeos/components/sync_wifi/pending_network_configuration_update.h"
#include "components/sync/protocol/wifi_configuration_specifics.pb.h"

namespace sync_wifi {

// Tracks updates to the local network stack while they are pending, including
// how many attempts have been executed.  Updates are stored persistently.
class PendingNetworkConfigurationTracker {
 public:
  PendingNetworkConfigurationTracker() = default;
  virtual ~PendingNetworkConfigurationTracker() = default;

  // Adds an update to the list of in flight changes.  |change_uuid| is a
  // unique identifier for each update, |ssid| is the identifier for the network
  // which is getting updated, and |specifics| should be nullopt if the network
  // is being deleted.
  virtual void TrackPendingUpdate(
      const std::string& change_guid,
      const std::string& ssid,
      const base::Optional<sync_pb::WifiConfigurationSpecificsData>&
          specifics) = 0;

  // Removes the given change from the list.
  virtual void MarkComplete(const std::string& change_guid,
                            const std::string& ssid) = 0;

  // Returns all pending updates.
  virtual std::vector<PendingNetworkConfigurationUpdate>
  GetPendingUpdates() = 0;

  // Returns the requested pending update, if it exists.
  virtual base::Optional<PendingNetworkConfigurationUpdate> GetPendingUpdate(
      const std::string& change_guid,
      const std::string& ssid) = 0;

  // Increments the number of completed attempts for the given update.
  virtual void IncrementCompletedAttempts(const std::string& change_guid,
                                          const std::string& ssid) = 0;

 private:
  DISALLOW_COPY_AND_ASSIGN(PendingNetworkConfigurationTracker);
};

}  // namespace sync_wifi

#endif  // CHROMEOS_COMPONENTS_SYNC_WIFI_PENDING_NETWORK_CONFIGURATION_TRACKER_H_
