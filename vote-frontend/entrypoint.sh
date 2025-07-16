#!/bin/sh

# 환경변수 치환해서 nginx 설정 파일 생성
envsubst '$TOMCAT_HOST $TOMCAT_PORT' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Nginx 실행
exec nginx -g 'daemon off;'
