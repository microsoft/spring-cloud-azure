package com.example;

import java.io.Serializable;

public class User implements Serializable {
    private String name;

    User(String name) {
        setName(name);
    }

    public String getName() { return name; }

    public void setName(String name) {
        this.name = name;
    }

}
