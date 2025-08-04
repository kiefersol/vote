#!/bin/bash

# 사용법 안내
if [ "$#" -ne 2 ]; then
  echo "사용법: $0 <ncloud_access_key_id> <ncloud_secret_access_key>"
  exit 1
fi

ACCESS_KEY=$1
SECRET_KEY=$2

CONFIG_FILE="/home/sol/.ncloud/configure"
OUTPUT_FILE="/home/sol/ahnlab/vote/kubeconfig.yaml"

# 디렉토리 생성 (없으면)
mkdir -p "$(dirname "$CONFIG_FILE")"

# 설정 파일 작성
cat > "$CONFIG_FILE" <<EOF
[DEFAULT]
ncloud_access_key_id = $ACCESS_KEY
ncloud_secret_access_key = $SECRET_KEY
ncloud_api_url = https://ncloud.apigw.ntruss.com
EOF

echo "$CONFIG_FILE 파일 생성 완료"

# kubeconfig 생성 명령 실행
ncp-iam-authenticator create-kubeconfig \
  --region KR \
  --clusterUuid 745e243f-2bf7-4682-bca9-16e186a83e66 \
  --output "$OUTPUT_FILE"

if [ $? -eq 0 ]; then
  echo "kubeconfig 생성 완료: $OUTPUT_FILE"
else
  echo "kubeconfig 생성 실패"
  exit 2
fi
