#!/usr/bin/env bash

BASEDIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
cd $BASEDIR

docker stop nginx
docker rm nginx
../docker/nginx_run.sh
docker ps -a