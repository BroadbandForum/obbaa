#!/usr/bin/env bash
APP_NAME=BAA

#we need this to go back on exit
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
export IS_DOCKER="true"
export EXTRA_JAVA_OPTS="$EXTRA_JAVA_OPTS $JAVA_OPTS "

#find out where the script is located
setScriptPath

#goto the directory where the script exists
cd $SCRIPT_PATH

CUR_DIR=`pwd`
RUN_CMD="exec java $EXTRA_JAVA_OPTS -Dlog4j.configuration=file:$CUR_DIR/../conf/log4j.xml  -cp ../lib/*:. org.broadband_forum.obbaa.sa.pac.StandaloneApp"
echo "Run command is : $RUN_CMD"
start(){
    $RUN_CMD
}
start

exit 0;
