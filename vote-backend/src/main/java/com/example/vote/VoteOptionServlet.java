package com.example.vote;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;

public class VoteOptionServlet extends HttpServlet {
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

            List<Map<String, Object>> options = voteDao.getOptionsByVoteId(voteId);

            res.getWriter().write(new Gson().toJson(options));

        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }
}