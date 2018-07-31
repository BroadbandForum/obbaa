#!/usr/bin/env bash

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

mkdir -p /baa/baa-dist/conf/tls

#copy the files
cp -R src/main/assembly/conf/tls/* /baa/baa-dist/conf/tls

chmod -R 777 /baa/baa-dist/conf/tls

cd $RUNNING_DIR