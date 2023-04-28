package com.sunveee.framework.keycloak.oidc.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
public class TestPublicController {

    @RequestMapping("/ping")
    public String ping() {
        return "ok";
    }

}
