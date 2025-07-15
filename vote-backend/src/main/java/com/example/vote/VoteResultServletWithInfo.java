package com.example.vote;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;

// mysql에서 투표 정보 가져오기 + Redis에서 투표결과 가져오기 => 사용 안함
public class VoteResultServletWithInfo extends HttpServlet {
    private final VoteDao voteDao = new VoteDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        String voteIdParam = req.getParameter("vote_id");

        if (voteIdParam == null) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"vote_id 파라미터가 필요합니다.\"}");
            return;
        }

        try {
            int voteId = Integer.parseInt(voteIdParam);

            // 1. 투표 정보 가져오기 (MySQL)
            Map<String, Object> voteInfo = voteDao.getVoteInfo(voteId);
            if (voteInfo == null) {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"해당 투표가 존재하지 않습니다.\"}");
                return;
            }

            // 2. Redis에서 결과 가져오기 (내림차순 정렬 포함)
            List<Map<String, Object>> results = voteDao.getVoteResultFromRedis(voteId);

            // 3. 응답 JSON 조립
            Map<String, Object> response = new HashMap<>();
            response.put("vote", voteInfo);
            response.put("results", results);

            res.getWriter().write(new Gson().toJson(response));

        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }
}
