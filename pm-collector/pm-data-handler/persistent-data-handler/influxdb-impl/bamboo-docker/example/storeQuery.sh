#!/bin/bash

INFLUX_BIN=/usr/bin
INFLUX_ORG="broadband_forum"
INFLUX_BUCKET="pm-collection"

stopTime=`date +%s`
startTime=`expr ${stopTime} - 10`
${INFLUX_BIN}/influx write -o ${INFLUX_ORG} -b ${INFLUX_BUCKET} -p s 'measurement,hostName=dpu0,templateId=267 if:inerrors=7i,if:outerrors=564i '${startTime}

${INFLUX_BIN}/influx query -o ${INFLUX_ORG} 'from(bucket: "'${INFLUX_BUCKET}'") |> range(start: '${startTime}', stop: '${stopTime}') |> filter(fn: (r) => r._measurement == "measurement")|> filter(fn: (r) => r.hostName == "dpu0")'
