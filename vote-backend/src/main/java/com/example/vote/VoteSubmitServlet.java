package com.example.vote;

import jakarta.servlet.http.*;
import java.io.IOException;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class VoteSubmitServlet extends HttpServlet {
    private final VoteDao voteDao = new VoteDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        req.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");

        String voteIdParam = req.getParameter("vote_id");
        String optionIdParam = req.getParameter("option_id");

        Map<String, Object> response = new HashMap<>();

        if (voteIdParam == null || optionIdParam == null) {
            res.setStatus(400);
            response.put("error", "vote_id, option_id는 필수입니다.");
            res.getWriter().write(new Gson().toJson(response));
            return;
        }

        try {
            int voteId = Integer.parseInt(voteIdParam);
            int optionId = Integer.parseInt(optionIdParam);

            boolean success = voteDao.saveVote(voteId, optionId);
            if (success) {
                response.put("message", "✅ 투표가 저장되었습니다.");
            } else {
                response.put("error", "❌ 저장 실패");
            }

            res.getWriter().write(new Gson().toJson(response));
        } catch (Exception e) {
            res.setStatus(500);
            response.put("error", "서버 오류: " + e.getMessage());
            res.getWriter().write(new Gson().toJson(response));
        }
    }
}

