#!/bin/bash
set -e

clean_up()
{
    if [ "${pid}" != "" ]; then
        kill ${pid}
        exit 1
    fi
}

if [ "${INFLUX_INIT}" = "" ]; then
    INFLUX_INIT=false
fi
if [ ${INFLUX_INIT} = "true" -o ! -f ${INFLUX_ROOT}/influxd.bolt -o ! -d ${INFLUX_ROOT}/engine ]; then
    rm -rf ${INFLUX_ROOT}/influxd.bolt ${INFLUX_ROOT}/engine
    if [ -f /etc/influxdb/influxdbfiles.tgz ]; then
        tar -S -C /var/opt -xf /etc/influxdb/influxdbfiles.tgz
    else
        pid=

        trap clean_up SIGHUP SIGINT SIGQUIT SIGTERM

        echo "Starting influxd"
        if [ ! -x /usr/bin/influxd ]; then
            echo "/usr/bin/influxd not found or not executable"
            exit 1
        fi

        /usr/bin/influxd run $@ >/var/log/influxdb.log 2>&1 &
        pid=$!
        if [ $? -ne 0 ]; then
            echo "Can't start influxd"
            exit 1
        fi
        sleep 10

        echo "Setup influxdb"
        /etc/influxdb/init-influxdb.sh
        if [ $? -ne 0 ]; then
            echo "influxdb initialization failed"
            es=1
        else
            echo "influxdb initialized"
            es=0
        fi

        echo "Stopping influxdb"
        kill ${pid}
        kes=$?
        wait
        if [ ${es} -eq 0 ]; then
            if [ ${kes} -ne 0 ]; then
                echo "Can't stop influxd"
                exit 1
            fi
        else
            if [ ${kes} -ne 0 ]; then
                echo "Can't stop influxd"
            fi
            exit 1
        fi
        trap - SIGHUP SIGINT SIGQUIT SIGTERM
        exit 0
    fi
fi

if [ "${1:0:1}" = '-' ]; then
    set -- influxd "$@"
fi

exec "$@" >>/var/log/influxdb.log 2>&1
