package com.office.cook.test;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/test")
public class TestController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public String testDB(Model model) {
        String sql = "SELECT * FROM playing_with_neon";
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
        model.addAttribute("neonData", result);
        return "test/neon"; // templates/test/neon.html
    }
}
