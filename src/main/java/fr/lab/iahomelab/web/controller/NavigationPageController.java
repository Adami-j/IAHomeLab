package fr.lab.iahomelab.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app")
public class NavigationPageController {

    @GetMapping("/experiments")
    public String experiments() {
        return "pages/experiments";
    }

    @GetMapping("/findings")
    public String findings() {
        return "pages/findings";
    }
}
