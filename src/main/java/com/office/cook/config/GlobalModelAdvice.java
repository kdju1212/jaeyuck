package com.office.cook.config;

import com.office.cook.member.MemberVo;
import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {

    @ModelAttribute
    public void addSessionUserToModel(HttpSession session, Model model) {
        MemberVo member = (MemberVo) session.getAttribute("loginedMemberVo");
        if (member != null) {
            model.addAttribute("loginedMemberVo", member);
        }
    }
}
