#!/usr/bin/env bash
#############################################################################################################################################
# Copyright 2020 Broadband Forum
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
################################################################################################################################################
#whenever the bbf-network-manager.yang module revision is updated, run this script to replace the old revision with the newly updated revision in the project files.

#Usage : ./updateNetworkManagerRevision.sh <old-revision> <new-revision>
#Example : ./updateNetworkManagerRevision.sh 2020-02-19 2020-05-19

#this will update the below mentioned files where the revision is being hardcoded. as of now only three places this revision is being hardcoded.
#after running this script, please ensure to commit the changes into the current working branch.
#################################################################################################################################################

find ../../device-manager -type f -name "descriptor.xml" -exec sed -i s/$1/$2/g {} +
find ../../device-manager -type f -name "test-application-context.xml" -exec sed -i s/$1/$2/g {} +
find ../../device-manager-entities -type f -name "DeviceManagerNSConstants.java" -exec sed -i s/$1/$2/g {} +

