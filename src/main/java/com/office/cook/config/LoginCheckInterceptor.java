package com.office.cook.config;

import com.office.cook.member.MemberVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession(false);
        MemberVo member = (session != null) ? (MemberVo) session.getAttribute("loginedMemberVo") : null;

        if (member == null) {
            response.sendRedirect("/member/loginForm");
            return false;
        }

        return true;
    }
}
