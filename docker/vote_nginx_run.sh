#!/usr/bin/env bash
docker run -d \
  --name vote-nginx \
  -p 80:80 \
  -e TOMCAT_HOST=211.188.58.156 \
  -e TOMCAT_PORT=8080 \
  nginx-vote:1.0