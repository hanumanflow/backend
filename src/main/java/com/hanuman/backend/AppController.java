package com.hanuman.backend;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
public class AppController {

    @GetMapping()
    public String welcomMessage(){
        return "Welcome to Backend application";
    }
}
