/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Warren Zhu
 */
@RestController
public class WebController {

    private final JdbcTemplate jdbcTemplate;

    public WebController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/users")
    public List getUsers() {
        return this.jdbcTemplate.queryForList("SELECT * FROM users").stream().map(Map::values)
                                .collect(Collectors.toList());
    }
}
