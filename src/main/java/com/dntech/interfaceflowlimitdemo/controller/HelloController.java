package com.dntech.interfaceflowlimitdemo.controller;

import com.dntech.interfaceflowlimitdemo.enums.LimitType;
import com.dntech.interfaceflowlimitdemo.facade.FlowLimiter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class HelloController {
    @GetMapping("/hello")
    @FlowLimiter(time = 5,count = 3,limitType = LimitType.DEFAULT)
    public String hello() {
        return "hello>>>"+new Date();
    }
}