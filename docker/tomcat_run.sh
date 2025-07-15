docker run -d \
  --name tomcat \
  -p 8080:8080 \
  -v $PWD/../vote-backend/build/libs/vote.war:/usr/local/tomcat/webapps/vote.war \
  -e DB_URL=jdbc:mysql://211.188.58.156:3306/vote?useSSL=true \
  -e DB_USER=sol \
  -e DB_PASS=sol \
  -e REDIS_HOST=211.188.58.156 \
  -e REDIS_PORT=6379 \
  -e REDIS_PASSWORD=sol \
  tomcat:10