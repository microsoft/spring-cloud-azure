package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    private final TestProperties properties;

    public HelloController(TestProperties properties) {
        this.properties = properties;
    }

    @GetMapping
    public String getColor() {
        return "Configured color: " + this.properties.getBackgroundColor();
    }
}
