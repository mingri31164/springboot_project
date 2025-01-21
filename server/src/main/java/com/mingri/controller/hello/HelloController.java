package com.mingri.controller.hello;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 35238
 * @date 2023/7/10 0010 22:25
 */
@RestController
public class HelloController {

    @RequestMapping("/hello")
    @PreAuthorize("hasAnyAuthority('test')")
    public String hello(){
        return "欢迎，开始你新的学习旅程吧";
    }

}