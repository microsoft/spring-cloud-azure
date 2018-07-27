/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
public class WebController {

    private final MongoTemplate mongoTemplate;

    WebController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostMapping("/insertUser/{name}")
    void insertUser(@PathVariable String name) {
        User user = new User();
        user.setName(name);
        this.mongoTemplate.insert(user, "user");
    }

    @GetMapping("/users")
    List<User> findUsers() {
        return mongoTemplate.findAll(User.class);
    }
}
