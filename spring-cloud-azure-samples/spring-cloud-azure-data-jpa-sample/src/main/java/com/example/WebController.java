/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class WebController {

    @Autowired
    private UserRepository repository;

    @GetMapping("/users")
    public List getUsers() {
        List<User> foundUser = new ArrayList<>();

        this.repository.findAll().forEach(foundUser::add);

        return foundUser;
    }

    @PostMapping("/user")
    public User postUser(@RequestBody User user) {
        this.repository.save(user);

        return user;
    }
}
