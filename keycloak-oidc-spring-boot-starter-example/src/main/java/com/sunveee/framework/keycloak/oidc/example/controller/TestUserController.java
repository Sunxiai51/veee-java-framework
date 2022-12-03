package com.sunveee.framework.keycloak.oidc.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class TestUserController {

    @RequestMapping("/ping")
    public String ping() {
        return "ok";
    }

}
