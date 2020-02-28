#!/bin/bash

INFLUX_BIN=/usr/bin
INFLUX_ORG="broadband_forum"
INFLUX_BUCKET="pm-collection"

${INFLUX_BIN}/influx delete -o ${INFLUX_ORG} -b ${INFLUX_BUCKET} --start 2020-01-30T12:00:00Z --stop 2020-01-30T13:00:00Z
