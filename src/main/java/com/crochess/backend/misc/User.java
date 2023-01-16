package com.crochess.backend.misc;

import java.security.Principal;

public class User implements Principal {
    String name;

    public User(String name) {
        System.out.println(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}