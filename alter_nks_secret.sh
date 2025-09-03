#!/bin/bash

if [ "$#" -ne 2 ]; then
  echo "사용법: $0 <ncloud_access_key_id> <ncloud_secret_access_key>"
  exit 1
fi

ACCESS_KEY=$1
SECRET_KEY=$2

kubectl delete secret ncloud-registry-secret --namespace vote

kubectl create secret docker-registry ncloud-registry-secret \
  --docker-server=hansol-container-registry.kr.ncr.ntruss.com \
  --docker-username=$ACCESS_KEY \
  --docker-password=$SECRET_KEY \
  --namespace vote