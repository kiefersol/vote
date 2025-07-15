package com.example.vote;

import jakarta.servlet.http.*;
import java.io.IOException;

public class HealthCheck extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setStatus(HttpServletResponse.SC_OK);
        res.getWriter().write("OK");
    }
}
