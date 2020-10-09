/*
 * Copyright 2020 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.onu;

import org.broadband_forum.obbaa.onu.notification.ONUNotification;
import org.json.JSONObject;

/**
 * <p>
 * Provides APIs to manage vOMCI based ONUs such as CRUD operations on ONUs and notification handling
 * </p>
 * Created by Ranjitha.B.R (Nokia) on 22/06/2020.
 */
public interface VOLTManagement {

    void deviceAdded(String deviceName);

    void deviceRemoved(String deviceName);

    void onuNotificationProcess(ONUNotification onuNotification, String oltDeviceName);

    void processResponse(JSONObject jsonResponse);
}
