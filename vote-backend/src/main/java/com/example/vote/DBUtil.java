package com.example.vote;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public class DBUtil {
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();

        // 환경변수에서 설정값 읽기 (기본값 포함)
        String url = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/vote?useSSL=true&serverTimezone=UTC");
        String user = System.getenv().getOrDefault("DB_USER", "sol");
        String password = System.getenv().getOrDefault("DB_PASS", "sol");

        config.setJdbcUrl(url);
        config.setUsername(user);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 풀 설정
        config.setMaximumPoolSize(10);        // 동시에 열 수 있는 최대 커넥션
        config.setMinimumIdle(2);             // 최소 유휴 커넥션
        config.setIdleTimeout(30000);         // 유휴 커넥션 최대 유지 시간(ms)
        config.setConnectionTimeout(3000);    // 커넥션 요청 대기 제한 시간(ms)
        config.setLeakDetectionThreshold(5000); // 누수 감지 (선택)

        ds = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}

