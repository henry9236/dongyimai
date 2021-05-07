package com.dongyimai.shopcontroller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/login")
@RestController
public class LoginController {
    @RequestMapping("name")
    public String name(){
        return  SecurityContextHolder.getContext().getAuthentication().getName();
    }
}

