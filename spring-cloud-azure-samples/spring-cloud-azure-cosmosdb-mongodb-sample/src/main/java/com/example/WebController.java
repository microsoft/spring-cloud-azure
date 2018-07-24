package com.example;

import org.springframework.boot.autoconfigure.mongo.MongoClientFactory;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class WebController {

    private final MongoTemplate mongoTemplate;

    WebController(MongoTemplate mongoTemplate){
        this.mongoTemplate=mongoTemplate;
    }

    @PostMapping("/insert")
    void insertUser(){
        User user=new User();
        user.setName("Kate");
        this.mongoTemplate.insert(user,"user");
    }

    @GetMapping("/users")
    List<User> findUsers(){
        return (List<User>) mongoTemplate.findAll(User.class);
    }
}
