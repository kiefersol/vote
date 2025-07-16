#!/usr/bin/env bash
docker run -d \
  --name mysql \
  -e MYSQL_ROOT_PASSWORD=sol \
  -e MYSQL_DATABASE=vote \
  -e MYSQL_USER=sol \
  -e MYSQL_PASSWORD=sol \
  -p 3306:3306 \
  mysql:8.0