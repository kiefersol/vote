FROM nginx:latest

# 정적 파일 복사
COPY ./ /usr/share/nginx/html

# 템플릿 및 진입 스크립트 복사
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

ENTRYPOINT ["/entrypoint.sh"]
