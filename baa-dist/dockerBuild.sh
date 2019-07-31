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
#this script is used to run the task from bamboo build server to build,tag & push the docker image


if [ "${bamboo_shortPlanName}" == "OBBAA-Develop" ]
then
    echo "current branch is ${bamboo_repository_git_branch}, proceeding to push the docker image to artifactory"
    docker login -u${bamboo_artifactory_userName} -p${bamboo_artifactory_password} obbaa-develop.registry.broadband-forum.org
    docker build -t obbaa-develop.registry.broadband-forum.org/baa:${bamboo_obbaa_current_release}-${bamboo_repository_git_branch}_${bamboo_buildNumber} .
    docker push obbaa-develop.registry.broadband-forum.org/baa:${bamboo_obbaa_current_release}-${bamboo_repository_git_branch}_${bamboo_buildNumber}
    docker tag obbaa-develop.registry.broadband-forum.org/baa:${bamboo_obbaa_current_release}-${bamboo_repository_git_branch}_${bamboo_buildNumber} obbaa-develop.registry.broadband-forum.org/baa:latest
    docker push obbaa-develop.registry.broadband-forum.org/baa:latest

elif [ "${bamboo_shortPlanName}" == "OBBAA-master" ]
then
    echo "current branch is ${bamboo_repository_git_branch}, proceeding to push the docker image to artifactory"
    docker login -u${bamboo_artifactory_userName} -p${bamboo_artifactory_password} obbaa-master.registry.broadband-forum.org
    docker build -t obbaa-master.registry.broadband-forum.org/baa:${bamboo_obbaa_current_release}-${bamboo_repository_git_branch}_${bamboo_buildNumber} .
    docker push obbaa-master.registry.broadband-forum.org/baa:${bamboo_obbaa_current_release}-${bamboo_repository_git_branch}_${bamboo_buildNumber}
    docker tag obbaa-master.registry.broadband-forum.org/baa:${bamboo_obbaa_current_release}-${bamboo_repository_git_branch}_${bamboo_buildNumber} obbaa-master.registry.broadband-forum.org/baa:latest
    docker push obbaa-master.registry.broadband-forum.org/baa:latest
else
    docker build -t obbaa-develop.registry.broadband-forum.org/baa:${bamboo_shortPlanName}_${bamboo_buildNumber} .
    echo "current branch ${bamboo_repository_git_branch} is a feature branch, ignoring to push the resources to artifactory"
fi