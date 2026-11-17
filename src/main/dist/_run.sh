#!/usr/bin/env bash
#
# Goal: rebuild interaction counts
#
. /etc/profile
APPNAME="interaction-counts-pipeline"

APPDIR=/home/rgddata/pipelines/$APPNAME
cd $APPDIR

# pipefail: exit status of the pipe is the java exit status, not tee's
set -o pipefail

java -Dspring.config=$APPDIR/../properties/default_db2.xml \
    -Dlog4j.configurationFile=file://$APPDIR/properties/log4j2.xml \
    -jar lib/$APPNAME.jar "$@" 2>&1 | tee run.log
