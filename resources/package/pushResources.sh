#!/usr/bin/env bash
###########################################################################
# Copyright 2018 Broadband Forum
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
###########################################################################
#this script is used to run the task from bamboo build server to push resources zip file to artifactory

if [ "${bamboo_shortPlanName}" == "OBBAA-Develop" ]
then
    echo "current branch is ${bamboo_repository_git_branch} pushing the resources to artifactory"
    curl --insecure -X PUT -u ${bamboo_artifactory_userName}:${bamboo_artifactory_password} -T target/obbaa-resources-1.0.0-SNAPSHOT.zip "https://registry.broadband-forum.org/artifactory/obbaa-resources-develop/obbaa-resources-${bamboo.obbaa.current.release}-${bamboo.repository.git.branch}_${bamboo.buildNumber}.zip"
elif [ "${bamboo_shortPlanName}" == "OBBAA-master" ]
then
    echo "current branch is ${bamboo_repository_git_branch} pushing the resources to artifactory"
    curl --insecure -X PUT -u ${bamboo_artifactory_userName}:${bamboo_artifactory_password} -T target/obbaa-resources-${bamboo.obbaa.current.release}.zip "https://registry.broadband-forum.org/artifactory/obbaa-resources-master/obbaa-resources-${bamboo.obbaa.current.release}-${bamboo.repository.git.branch}_${bamboo.buildNumber}.zip"
else
    echo "current branch ${bamboo_repository_git_branch} is a feature branch, ignoring artifacts to be pushed to artifactory"
fi