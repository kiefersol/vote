docker run -d \
  --name redis \
  -p 6379:6379 \
  -e REDIS_PASSWORD=sol \
  redis:latest \
  redis-server --requirepass sol