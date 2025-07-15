package com.example.vote;

import jakarta.servlet.http.*;
import java.io.IOException;

public class VoteFinalizeServlet extends HttpServlet {
    private final VoteDao voteDao = new VoteDao();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
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

            voteDao.finalizeAndCompareVote(voteId);

            res.setStatus(200);
            res.getWriter().write("{\"message\":\"투표 종료 처리 및 비교 저장 완료\"}");

        } catch (NumberFormatException e) {
            res.setStatus(400);
            res.getWriter().write("{\"error\":\"vote_id는 숫자여야 합니다.\"}");
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }
}

