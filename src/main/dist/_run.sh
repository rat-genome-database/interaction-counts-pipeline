#!/usr/bin/env bash
#
# Goal: rebuild interaction counts
#
. /etc/profile
APPNAME="interaction-counts-pipeline"

APPDIR=/home/rgddata/pipelines/$APPNAME
cd $APPDIR

java -Dspring.config=$APPDIR/../properties/default_db2.xml \
    -Dlog4j.configurationFile=file://$APPDIR/properties/log4j2.xml \
    -jar lib/$APPNAME.jar "$@" | tee run.log 2>&1