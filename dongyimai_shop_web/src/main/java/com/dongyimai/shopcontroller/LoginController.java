package com.dongyimai.shopcontroller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/login")
@RestController
public class LoginController {
    /**
     * 返回登录的用户名
     *
     * @return
     */
    @RequestMapping("name")
    public Map name() {
        //从SecurityContextHolder中获取登陆的姓名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName", name);
        return map;
    }
}