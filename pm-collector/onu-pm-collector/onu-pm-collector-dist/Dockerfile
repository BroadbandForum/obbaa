FROM openjdk:8u131-jdk-alpine
###########################################################################
# Copyright 2023 Broadband Forum
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

#workaround for secure random jdk bug
RUN sed -i 's@securerandom.source=file:/dev/random@securerandom.source=file:/dev/./urandom@g' $JAVA_HOME/jre/lib/security/java.security

#Setup proxy if container is run on behind proxy host
ARG http_proxy
ARG https_proxy

ENV http_proxy $http_proxy
ENV https_proxy $https_proxy

RUN apk add --no-cache bash

RUN mkdir /ipfix
ARG version=6.0.0
ADD target/onu-pm-collector-dist*.tar.gz /onu-pm-collector

RUN ln -s /onu-pm-collector/onu-pm-collector-dist-${version} /onu-pm-collector/onu-pm-collector-dist

ENV ver $version
CMD /onu-pm-collector/onu-pm-collector-dist/bin/start.sh
