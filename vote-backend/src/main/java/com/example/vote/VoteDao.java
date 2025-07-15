package com.example.vote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

public class VoteDao {
    //투표 리스트가져오기 => 완료 : /list
    public List<Map<String, Object>> getVoteList() throws SQLException {
        String sql = """
            SELECT vote_id, title, description, start_time, end_time, is_active
            FROM vote
            ORDER BY start_time DESC
        """;

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("vote_id", rs.getInt("vote_id"));
                row.put("title", rs.getString("title"));
                row.put("description", rs.getString("description"));
                row.put("is_active", rs.getBoolean("is_active"));
                row.put("start_time", rs.getTimestamp("start_time").toString());
                row.put("end_time", rs.getTimestamp("end_time").toString());
                list.add(row);
            }
        }

        return list;
    }

    //투표 상세 정보만 가져오기 => 완료 : /detail 
    public Map<String, Object> getVoteInfo(int voteId) throws SQLException {
        String sql = "SELECT vote_id, title, is_active, description, start_time, end_time FROM vote WHERE vote_id = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> voteInfo = new HashMap<>();
                    voteInfo.put("vote_id", rs.getInt("vote_id"));
                    voteInfo.put("title", rs.getString("title"));
                    voteInfo.put("is_active", rs.getBoolean("is_active"));
                    voteInfo.put("description", rs.getString("description"));
                    voteInfo.put("start_time", rs.getTimestamp("start_time").toString());
                    voteInfo.put("end_time", rs.getTimestamp("end_time").toString());
                    return voteInfo;
                } else {
                    return null;
                }
            }
        }
    }

    // 투표 옵션 정보 가져오기 => 완료 : /options
    public List<Map<String, Object>> getOptionsByVoteId(int voteId) throws SQLException {
        String sql = "SELECT option_id, option_text FROM vote_option WHERE vote_id = ? ORDER BY option_id ASC";

        List<Map<String, Object>> options = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("option_id", rs.getInt("option_id"));
                    row.put("option_text", rs.getString("option_text"));
                    options.add(row);
                }
            }
        }

        return options;
    }



    // 투표 상세 정보 + 옵션 목록 반환 => 현재 사용안함
    public Map<String, Object> getVoteDetailWithOptions(int voteId) throws SQLException {
        Map<String, Object> result = new HashMap<>();

        String voteSql = "SELECT vote_id, title, description, start_time, end_time FROM vote WHERE vote_id = ?";
        String optionSql = "SELECT option_id, option_text FROM vote_option WHERE vote_id = ?";

        try (Connection conn = DBUtil.getConnection()) {
            // 1. 투표 기본 정보
            try (PreparedStatement stmt = conn.prepareStatement(voteSql)) {
                stmt.setInt(1, voteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        result.put("vote_id", rs.getInt("vote_id"));
                        result.put("title", rs.getString("title"));
                        result.put("description", rs.getString("description"));
                        result.put("start_time", rs.getTimestamp("start_time").toString());
                        result.put("end_time", rs.getTimestamp("end_time").toString());
                    } else {
                        return null; // 투표 ID가 존재하지 않음
                    }
                }
            }

            // 2. 옵션 리스트
            List<Map<String, Object>> options = new ArrayList<>();
            try (PreparedStatement stmt = conn.prepareStatement(optionSql)) {
                stmt.setInt(1, voteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> opt = new HashMap<>();
                        opt.put("option_id", rs.getInt("option_id"));
                        opt.put("option_text", rs.getString("option_text"));
                        options.add(opt);
                    }
                }
            }

            result.put("options", options);
        }

        return result;
    }

    // 투표 하기 : Redis와 MySQL 동시 저장 => 완료 /submit
    public boolean saveVote(int voteId, int optionId) throws SQLException {
    // 1. 먼저 해당 투표가 진행 중인지 확인
        String checkSql = "SELECT is_active FROM vote WHERE vote_id = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setInt(1, voteId);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    boolean isActive = rs.getBoolean("is_active");
                    if (!isActive) {
                        System.out.println("이미 종료된 투표입니다 (vote_id=" + voteId + ")");
                        return false;
                    }
                } else {
                    System.out.println("투표 ID가 존재하지 않음");
                    return false;
                }
            }
        }

        System.out.println("------------save----------------투표진행사항 파악");

        // 2. Redis에 집계
        String redisKey = "vote:" + voteId + ":results";
        try (Jedis jedis = RedisUtil.getClient()) {
            jedis.zincrby(redisKey, 1, String.valueOf(optionId));
            System.out.println("-----------------save-----------redis 연결 성공");
        } catch (Exception e) {
            System.err.println("Redis 저장 실패: " + e.getMessage());
            return false;
        }


        System.out.println("-----------------save-----------redis 연결 성공");

        // 3. MySQL 기록
        String sql = "INSERT INTO vote_record (vote_id, option_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, voteId);
            stmt.setInt(2, optionId);
            System.out.println("-------------save---------------mysql 연결 성공");
            return stmt.executeUpdate() == 1;
            
        }

        
    }

    // DB에서 투표결과 가져오기 => 투표종료 시 DB값 검진할 때 사용
    public List<Map<String, Object>> getVoteResult(int voteId) throws SQLException {
        String sql = """
            SELECT o.option_id, o.option_text, COUNT(r.option_id) AS vote_count
            FROM vote_option o
            LEFT JOIN vote_record r ON o.option_id = r.option_id
            WHERE o.vote_id = ?
            GROUP BY o.option_id, o.option_text
            ORDER BY vote_count DESC
        """;

        List<Map<String, Object>> results = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("option_id", rs.getInt("option_id"));
                    row.put("option_text", rs.getString("option_text"));
                    row.put("vote_count", rs.getInt("vote_count"));
                    results.add(row);
                }
            }
        }
        return results;
    }

    // DB에서 투표정보와 투표결과 가져오기 => 사용 안함
    public Map<String, Object> getVoteResultWithInfo(int voteId) throws SQLException {
        Map<String, Object> response = new HashMap<>();

        // 1. 투표 정보
        String voteSql = "SELECT vote_id, title, description, start_time, end_time FROM vote WHERE vote_id = ?";
            try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt1 = conn.prepareStatement(voteSql)) {
                
                stmt1.setInt(1, voteId);
                try (ResultSet rs = stmt1.executeQuery()) {
                    if (rs.next()) {
                        Map<String, Object> voteInfo = new HashMap<>();
                        voteInfo.put("vote_id", rs.getInt("vote_id"));
                        voteInfo.put("title", rs.getString("title"));
                        voteInfo.put("description", rs.getString("description"));
                        voteInfo.put("start_time", rs.getTimestamp("start_time").toString());
                        voteInfo.put("end_time", rs.getTimestamp("end_time").toString());
                        response.put("vote", voteInfo);
                    } else {
                        return null; // 해당 vote_id 없음
                    }
                }

                // 2. 득표 수 결과
                String resultSql = """
                    SELECT o.option_id, o.option_text, COUNT(r.option_id) AS vote_count
                    FROM vote_option o
                    LEFT JOIN vote_record r ON o.option_id = r.option_id
                    WHERE o.vote_id = ?
                    GROUP BY o.option_id, o.option_text
                    ORDER BY vote_count DESC
                """;

                List<Map<String, Object>> results = new ArrayList<>();
                try (PreparedStatement stmt2 = conn.prepareStatement(resultSql)) {
                    stmt2.setInt(1, voteId);
                    try (ResultSet rs = stmt2.executeQuery()) {
                        while (rs.next()) {
                            Map<String, Object> row = new HashMap<>();
                            row.put("option_id", rs.getInt("option_id"));
                            row.put("option_text", rs.getString("option_text"));
                            row.put("vote_count", rs.getInt("vote_count"));
                            results.add(row);
                        }
                    }
                }

                response.put("results", results);
            }

        return response;
    }

    // Redis에서 투표 결과 가져오기
    public List<Map<String, Object>> getVoteResultFromRedis(int voteId) {
        String zsetKey = "vote:" + voteId + ":results";
        String hashKey = "vote:" + voteId + ":option_texts";
        List<Map<String, Object>> results = new ArrayList<>();

        try (Jedis jedis = RedisUtil.getClient()) {
            // 1. Redis에 ZSET 없으면 
            if (jedis.hlen(hashKey) == 0) {
                cacheOptionTextsToRedis(voteId, jedis, hashKey);
                System.out.println("-------------get---------------redis에 set없음");
            }

            // 2. 득표 수 ZSET 없으면 복구
            if (jedis.zcard(zsetKey) == 0) {
                restoreVoteCountsToRedis(voteId, jedis, zsetKey);
                System.out.println("-------------get---------------redis에 count없음");
            }

            System.out.println("---------------정상으로 투표 존재");

            // 3. 결과 구성
            Set<String> optionIds = jedis.hkeys(hashKey);
            for (String optionId : optionIds) {
                String optionText = jedis.hget(hashKey, optionId);
                double score = jedis.zscore(zsetKey, optionId) != null ? jedis.zscore(zsetKey, optionId) : 0;

                Map<String, Object> row = new HashMap<>();
                row.put("option_id", Integer.parseInt(optionId));
                row.put("option_text", optionText);
                row.put("vote_count", (int) score);
                results.add(row);
            }

            System.out.println("---------------redis 정보가져오기 성공: get");

        } catch (Exception e) {
            System.err.println("Redis 조회 중 오류 발생: " + e.getMessage());
        }

        return results;
    }
 
    // getVoteResultFromRedis에서 사용 => Redis에 ZSET 없으면 복구
    private void cacheOptionTextsToRedis(int voteId, Jedis jedis, String hashKey) {
        String sql = "SELECT option_id, option_text FROM vote_option WHERE vote_id = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, String> redisMap = new HashMap<>();

                while (rs.next()) {
                    String optionId = String.valueOf(rs.getInt("option_id"));
                    String optionText = rs.getString("option_text");
                    redisMap.put(optionId, optionText);
                }

                if (!redisMap.isEmpty()) {
                    jedis.hset(hashKey, redisMap);
                    System.out.println("Redis 옵션 캐시 완료 (vote_id=" + voteId + ")");
                }

            } catch (Exception e) {
                System.err.println("옵션 Redis 캐시 실패: " + e.getMessage());
            }
        } catch (Exception e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
        }
    }

    // getVoteResultFromRedis에서 사용 => 득표 수 ZSET 없으면 복구 → DB에서 득표 수 복구
    private void restoreVoteCountsToRedis(int voteId, Jedis jedis, String zsetKey) {
        String sql = """
            SELECT option_id, COUNT(*) as cnt
            FROM vote_record
            WHERE vote_id = ?
            GROUP BY option_id
        """;

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String optionId = String.valueOf(rs.getInt("option_id"));
                    int count = rs.getInt("cnt");
                    jedis.zadd(zsetKey, count, optionId);
                }
                System.out.println("Redis 득표 복구 완료 (vote_id=" + voteId + ")");
            }

        } catch (Exception e) {
            System.err.println("득표 Redis 복구 실패: " + e.getMessage());
        }
    }

    // 투표 종료 
    public void finalizeAndCompareVote(int voteId) {
        String zsetKey = "vote:" + voteId + ":results";
        String hashKey = "vote:" + voteId + ":option_texts";

        try (Jedis jedis = RedisUtil.getClient()) {

            // 1. Redis 결과 불러오기
            Map<String, Integer> redisMap = new HashMap<>();
            List<Tuple> zset = jedis.zrangeWithScores(zsetKey, 0, -1);
            for (Tuple t : zset) {
                redisMap.put(t.getElement(), (int) t.getScore());
            }

            // 2. DB 결과 불러오기
            Map<String, Integer> dbMap = new HashMap<>();
            String dbCountSql = """
                SELECT option_id, COUNT(*) AS cnt
                FROM vote_record
                WHERE vote_id = ?
                GROUP BY option_id
            """;

            try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(dbCountSql)) {

                stmt.setInt(1, voteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String optionId = String.valueOf(rs.getInt("option_id"));
                        dbMap.put(optionId, rs.getInt("cnt"));
                    }
                }

            } catch (Exception e) {
                System.err.println("vote_record 집계 실패: " + e.getMessage());
                return;
            }

            // 3. vote_option 기준으로 전체 옵션 목록 조회
            Map<String, String> allOptions = new HashMap<>();
            String optionSql = "SELECT option_id, option_text FROM vote_option WHERE vote_id = ?";

            try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(optionSql)) {

                stmt.setInt(1, voteId);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        allOptions.put(String.valueOf(rs.getInt("option_id")), rs.getString("option_text"));
                    }
                }

            } catch (Exception e) {
                System.err.println("vote_option 조회 실패: " + e.getMessage());
                return;
            }

            // 4. vote_result 테이블 저장
            String insertSql = """
                INSERT INTO vote_result (vote_id, option_id, redis_count, db_count, is_matched)
                VALUES (?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    redis_count = VALUES(redis_count),
                    db_count = VALUES(db_count),
                    is_matched = VALUES(is_matched)
            """;

            try (Connection conn = DBUtil.getConnection();
                PreparedStatement stmt = conn.prepareStatement(insertSql)) {

                for (String optionId : allOptions.keySet()) {
                    int redisCount = redisMap.getOrDefault(optionId, 0);
                    int dbCount = dbMap.getOrDefault(optionId, 0);
                    boolean matched = (redisCount == dbCount);

                    stmt.setInt(1, voteId);
                    stmt.setInt(2, Integer.parseInt(optionId));
                    stmt.setInt(3, redisCount);
                    stmt.setInt(4, dbCount);
                    stmt.setBoolean(5, matched);
                    stmt.addBatch();
                }

                stmt.executeBatch();
                System.out.println("vote_result 저장 완료 (vote_id=" + voteId + ")");

            } catch (Exception e) {
                System.err.println("vote_result 저장 오류: " + e.getMessage());
            }

            // 5. Redis 키 정리
            jedis.del(zsetKey);
            jedis.del(hashKey);
            System.out.println("🧹 Redis 키 삭제 완료");

        } // 여기서 jedis가 자동 close됨

        // 6. vote 테이블 종료 처리
        String updateVoteSql = "UPDATE vote SET is_active = FALSE WHERE vote_id = ?";

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(updateVoteSql)) {

            stmt.setInt(1, voteId);
            stmt.executeUpdate();
            System.out.println("vote 상태 종료 처리 완료 (is_active = FALSE)");

        } catch (Exception e) {
            System.err.println("vote 종료 상태 업데이트 실패: " + e.getMessage());
        }
    }



    // 최종 투표 결과 확인
    public List<Map<String, Object>> getFinalResult(int voteId) throws SQLException {
        List<Map<String, Object>> results = new ArrayList<>();

        // 1. 종료된 투표인지 확인
        if (isVoteActive(voteId)) {
            throw new IllegalStateException("아직 종료되지 않은 투표입니다.");
        }

        // 2. vote_result 결과 가져오기
        String sql = """
            SELECT vr.option_id, vo.option_text, vr.redis_count, vr.db_count, vr.is_matched
            FROM vote_result vr
            JOIN vote_option vo ON vr.option_id = vo.option_id
            WHERE vr.vote_id = ?
            ORDER BY vr.db_count DESC
        """;

        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("option_id", rs.getInt("option_id"));
                    row.put("option_text", rs.getString("option_text"));
                    row.put("redis_count", rs.getInt("redis_count"));
                    row.put("db_count", rs.getInt("db_count"));
                    row.put("is_matched", rs.getBoolean("is_matched"));
                    results.add(row);
                }
            }
        }

        return results;
    }

    // 내부 확인용
    private boolean isVoteActive(int voteId) {
        String sql = "SELECT is_active FROM vote WHERE vote_id = ?";
        try (Connection conn = DBUtil.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, voteId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getBoolean("is_active");
            }
        } catch (Exception e) {
            System.err.println("is_active 확인 실패: " + e.getMessage());
        }
        return true;
    }


}

