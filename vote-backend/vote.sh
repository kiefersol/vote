#!/usr/bin/env bash

VOTE_ID=2
ENDPOINT="http://localhost:8080/vote/submit"

OPTION_MIN=7
OPTION_MAX=12

for i in {1..10}
do
  OPTION_ID=$(( RANDOM % (OPTION_MAX - OPTION_MIN + 1) + OPTION_MIN ))

  echo "🔄 [$i] 요청: vote_id=$VOTE_ID, option_id=$OPTION_ID"

  # curl 응답을 변수에 저장
  RESPONSE=$(curl -s -w "%{http_code}" -o response.txt -X POST "$ENDPOINT" \
    -d "vote_id=$VOTE_ID" \
    -d "option_id=$OPTION_ID")

  BODY=$(cat response.txt)

  # 로그 출력
  echo "🔁 응답 코드: $RESPONSE"
  echo "📦 응답 본문: $BODY"
  echo "---------------------------------------"

  sleep 0.3
done

echo "✅ 10회 랜덤 투표 완료 (vote_id=$VOTE_ID)"


