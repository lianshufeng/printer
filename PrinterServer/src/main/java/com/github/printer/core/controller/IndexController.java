package com.github.printer.core.controller;

import com.github.microservice.core.helper.ViewHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {


    @RequestMapping({"/", ""})
    public ModelAndView index() {
        return new ModelAndView("index");
    }

}
