package com.github.printer.core.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class TestController {

    @RequestMapping("test")
    public Object test() {
        return Map.of("time", System.currentTimeMillis());
    }


}
