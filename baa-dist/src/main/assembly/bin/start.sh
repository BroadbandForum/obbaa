#!/usr/bin/env bash
APP_NAME=BAA

#we need this to go back on exit
RUNNING_DIR=`pwd`

echo "Model Abstracter status ${MODEL_ABSTRACTER_STATUS}"
if [ "${MODEL_ABSTRACTER_STATUS}" != "Enable" ]; then
  echo "Disabling Model Abstracter"
  find /baa/baa-dist/system/ -name baa-feature*.xml | xargs -I{} sed -i -E "/<bundle>mvn:org.broadband-forum.obbaa\/model-abstracter\/.+<\/bundle>/d" {}
fi

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
#export EXTRA_JAVA_OPTS="$EXTRA_JAVA_OPTS $JAVA_OPTS "
#export DEFAULT_JAVA_DEBUG_OPTS='-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=5005'

#find out where the script is located
setScriptPath

#goto the directory where the script exists
cd $SCRIPT_PATH

CUR_DIR=`pwd`
RUN_CMD="exec ./karaf debug"
echo "Run command is : $RUN_CMD"
start(){
    $RUN_CMD
}
start

exit 0;
