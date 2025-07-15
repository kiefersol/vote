package com.example.vote;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    private static JedisPool pool;

    static {
        String redisHost = System.getenv().getOrDefault("REDIS_HOST", "localhost");
        int redisPort = Integer.parseInt(System.getenv().getOrDefault("REDIS_PORT", "6379"));
        String redisPassword = System.getenv("REDIS_PASSWORD");  // 비밀번호 선택적

        System.out.println(" [Redis] 초기화 시도 중: " + redisHost + ":" + redisPort);

        try {
            if (redisPassword != null && !redisPassword.isEmpty()) {
                pool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 2000, redisPassword);
            } else {
                pool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort, 2000);
            }
            // 연결 테스트
            try (Jedis jedis = pool.getResource()) {
                String pong = jedis.ping();
                System.out.println(" [Redis] 연결 성공: " + pong);
            }
        } catch (Exception e) {
            System.err.println(" [Redis] 연결 실패: " + e.getMessage());
        }
    }

    public static Jedis getClient() {
        try {
            Jedis jedis = pool.getResource();
            System.out.println(" [Redis] 커넥션 획득 성공");
            return jedis;
        } catch (Exception e) {
            System.err.println(" [Redis] 커넥션 획득 실패: " + e.getMessage());
            throw e;
        }
    }
}
