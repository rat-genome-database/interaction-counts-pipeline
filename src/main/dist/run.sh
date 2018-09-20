# runs the pipeline

. /etc/profile
APPDIR=/home/rgddata/pipelines/InteractionCounts
SERVER=`hostname -s | tr '[a-z]' '[A-Z]'`
EMAIL_LIST=mtutaj@mcw.edu
if [ "$SERVER" = "REED" ]; then
  EMAIL_LIST=mtutaj@mcw.edu,jthota@mcw.edu,jdepons@mcw.edu
fi

$APPDIR/_run.sh "$@"

mailx -s "[$SERVER] InteractionCounts Pipeline OK" $EMAIL_LIST < $APPDIR/run.log
