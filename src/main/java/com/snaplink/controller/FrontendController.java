package com.snaplink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/** Cho phép mở trực tiếp các đường dẫn do React Router quản lý. */
@Controller
public class FrontendController {
    @GetMapping({"/login", "/register", "/dashboard", "/analytics/{id}"})
    public String openFrontend() {
        return "forward:/index.html";
    }
}
