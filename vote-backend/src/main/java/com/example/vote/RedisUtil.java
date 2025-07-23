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

    // 문자열을 UTF-8로 인코딩하여 저장
    public static void setString(String key, String value) {
        try (Jedis jedis = getClient()) {
            // UTF-8 인코딩하여 저장
            jedis.set(key.getBytes(StandardCharsets.UTF_8), value.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [Redis] 값 저장 성공: " + value);
        }
    }

    // Redis에서 UTF-8로 디코딩하여 값 조회
    public static String getString(String key) {
        try (Jedis jedis = getClient()) {
            byte[] value = jedis.get(key.getBytes(StandardCharsets.UTF_8));
            if (value != null) {
                // UTF-8로 디코딩하여 반환
                return new String(value, StandardCharsets.UTF_8);
            } else {
                return null;  // 값이 없을 경우
            }
        }
    }
}
