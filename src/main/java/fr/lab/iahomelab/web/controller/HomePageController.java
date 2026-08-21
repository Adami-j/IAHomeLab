package fr.lab.iahomelab.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/app")
public class HomePageController {

    @GetMapping
    public String home() {
        return "pages/home";
    }
}
