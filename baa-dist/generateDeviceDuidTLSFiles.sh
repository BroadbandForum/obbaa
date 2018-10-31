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

if [ $# -lt 1 ]
then
    echo "usage: ./generateDeviceDuidTLSFiles.sh <duid string>"
    exit 1;
fi
RUNNING_DIR=`pwd`

setScriptPath () {
    SCRIPT_PATH="${BASH_SOURCE[0]}";
    if ([ -h "${SCRIPT_PATH}" ]) then
        while([ -h "${SCRIPT_PATH}" ]) do
            SCRIPT_PATH=`readlink "${SCRIPT_PATH}"`;
        done
    fi
    pushd . > /dev/null
    cd `dirname ${SCRIPT_PATH}` > /dev/null
    SCRIPT_PATH=`pwd`;
    popd  > /dev/null

}
#find out where the script is located
setScriptPath

#goto the directory where the script exists
cd $SCRIPT_PATH

duid=$1

mkdir -p $duid

echo "TLS files will be created in directory `pwd`/$duid"

cd $duid

echo "Generating private key "
openssl genrsa -out $duid.key 2048

echo "Converting private key to pk8 format"
openssl pkcs8 -topk8 -inform pem -in $duid.key -outform pem -nocrypt -out "$duid"_privatekey.pem

echo "Creating the certificate signing request"
openssl req -new -key "$duid"_privatekey.pem -subj "/C=IN/ST=KA/L=BLR/O=OBBAA/CN=DUID\/$duid" -out $duid.csr

echo "Signing the certificate using BAA's Root CA private key"
openssl x509 -req -in $duid.csr -CA $SCRIPT_PATH/src/main/assembly/conf/tls/rootCA.crt -CAkey $SCRIPT_PATH/src/main/assembly/conf/tls/rootCA.key -CAcreateserial -out "$duid"_certchain.crt -days 500000

echo "Copyign trust chain to be used by the device"
cp $SCRIPT_PATH/src/main/assembly/conf/tls/rootCA.crt trustchain.pem

cd $RUNNING_DIR
