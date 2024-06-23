package com.handson.com.search_engine.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AppController {

    @GetMapping(value = "helloWorld")
    public String helloWorld(){
        return "Hello, World!";
    }

}
