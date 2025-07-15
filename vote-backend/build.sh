#!/usr/bin/env bash

BASEDIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"

cd $BASEDIR

docker stop tomcat
docker rm tomcat
./gradlew clean build
../docker/tomcat_run.sh
docker ps -a