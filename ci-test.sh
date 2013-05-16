set -x

export xsbt="$(pwd)/sbt -Dsbt.log.noformat=true"
chmod a+x sbt sbtwrapper/sbt-launch.jar
$xsbt clean test