package com.example.vote;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;

public class VoteFinalResultServlet extends HttpServlet {
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
            List<Map<String, Object>> result = voteDao.getFinalResult(voteId);

            if (result.isEmpty()) {
                res.setStatus(404);
                res.getWriter().write("{\"error\":\"해당 투표 결과가 없습니다.\"}");
            } else {
                String json = JsonUtil.toJson(result); // Jackson 또는 Gson 사용
                res.getWriter().write(json);
            }

        } catch (IllegalStateException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }
}

