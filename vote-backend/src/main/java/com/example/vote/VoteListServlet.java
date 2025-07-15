package com.example.vote;

import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.*;
import com.google.gson.Gson;

public class VoteListServlet extends HttpServlet {
    private final VoteDao voteDao = new VoteDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        try {
            List<Map<String, Object>> list = voteDao.getVoteList();
            res.getWriter().write(new Gson().toJson(list));
        } catch (Exception e) {
            res.setStatus(500);
            res.getWriter().write("{\"error\":\"서버 오류: " + e.getMessage() + "\"}");
        }
    }
}
