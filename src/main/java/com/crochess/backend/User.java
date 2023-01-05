package com.crochess.backend;

import java.security.Principal;

class User implements Principal {
    String name;

    User(String name) {
        System.out.println(name);
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}