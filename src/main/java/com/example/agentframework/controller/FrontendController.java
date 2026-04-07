package com.example.agentframework.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

    // 处理所有前端路由请求，重定向到index.html
    @RequestMapping(value = {"/", "/login", "/dashboard", "/agents", "/student-agents", "/chat/**", 
                           "/knowledge", "/students", "/assignments", "/assignment-list", 
                           "/assignment-submit/**", "/assignment-review/**", "/analytics", 
                           "/theme-settings", "/profile", "/teachers-info"})
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
