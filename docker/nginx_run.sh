docker run -d \
  --name nginx \
  -p 80:80 \
  -v $PWD/../vote-frontend/:/usr/share/nginx/html:ro\
  -v $PWD/../vote-frontend/nginx:/etc/nginx/ \
  nginx:latest
