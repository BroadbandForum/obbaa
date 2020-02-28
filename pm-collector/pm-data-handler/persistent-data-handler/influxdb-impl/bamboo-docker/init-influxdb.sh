#!/bin/bash

# set -x

INFLUX_PATH=/usr/bin

INFLUX_USER=${INFLUX_USER:=influxdb}
INFLUX_PW=${INFLUX_PW:=influxdb}
INFLUX_ORG=${INFLUX_ORG:=broadband_forum}
INFLUX_BUCKET=${INFLUX_BUCKET:=pm-collection}
INFLUX_RETENTION=${INFLUX_RETENTION:=720}
INFLUX_PORT=${INFLUX_PORT:=9999}

timeout=10
while [ ${timeout} -gt 0 ]; do
  netstat -ant | grep ${INFLUX_PORT}
  if [ $? -eq 0 ]; then
    break
  fi
  echo "Waiting for influxd to be online"
  sleep 1
  timeout=$((timeout - 1))
done
if [ ${timeout} -eq 0 ]; then
  echo "influxd not online"
  exit 1
fi

${INFLUX_PATH}/influx setup -f -o ${INFLUX_ORG} -b ${INFLUX_BUCKET} -u ${INFLUX_USER} -p ${INFLUX_PW} -r ${INFLUX_RETENTION}
if [ $? -ne 0 ]; then
  echo "Failed to setup influxdb"
  exit 1
fi

sleep 10
sync
exit 0
