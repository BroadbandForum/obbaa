#!/bin/bash
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
  
#This linux shell script are used to load the yang model to the netopeer2.
#This script can be used like this:
#    ./simulatorYangInstall.sh [docker_name] [yangModelFilePathInDocker]
# such as:
#    ./simulatorYangInstall.sh sysrepo /tmp/deviceYangModules
  
#param $1 docker name
#param $2 path of yang model
#param $3 yang model's name
function installYang()
{
                dockerName=$1
                addr=$2
                docker exec $dockerName sysrepoctl --install --yang=$addr
}
  
#param $1 docker name
#param $2 application's name
function applicationYang()
{
                dockerName=$1
                path=$2
                baseName="${path##*/}";
                yangModel=$(rev <<< $baseName | cut -d . -f2- | rev)
                docker exec -d $dockerName nohup /opt/dev/sysrepo/build/examples/application_example $yangModel &
                echo load $yangModel
}
  
#param $1 yang model file name
#return return 0 when $1 is a yang model
function checkIsYang()
{
                extension=$(rev <<< $1 | cut -d . -f1 | rev)
                if [ "$extension"x = "yang"x ];then
                                return 0
                fi
                                return 1
}
  
docker_name=$1
yangModelPath=$2
if [ -z $1 ]&&[ -z $2];then
                docker_name="sysrepo"
                yangModelPath="/tmp/deviceYangModules"
                echo $docker_name,$yangModelPath
fi
if [ -z $docker_name ]||[ -z $yangModelPath ];then
                echo This shell script has two usage:
                echo 1.provide two param,docker name and yang model path
                echo 2.provide no param
                exit 2
fi
filelist=`docker exec $docker_name find $yangModelPath -name "*yang" -exec grep -l ^module {} \;`
for fileName in $filelist
do
                checkIsYang $fileName
                if [ $? -eq "0" ];then
                                installYang $docker_name $fileName
                                applicationYang $docker_name $fileName
                fi
done
  
docker exec $docker_name sysrepoctl -l
