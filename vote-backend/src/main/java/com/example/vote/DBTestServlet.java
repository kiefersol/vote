package com.example.vote;


import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Connection;


public class DBTestServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
            try (Connection conn = DBUtil.getConnection()) {
                res.setContentType("text/plain");
                res.getWriter().write("DB 연결 성공!");
            } catch (Exception e) {
                res.setStatus(500);
                res.getWriter().write("DB 연결 실패: " + e.getMessage());
            }
    }
}