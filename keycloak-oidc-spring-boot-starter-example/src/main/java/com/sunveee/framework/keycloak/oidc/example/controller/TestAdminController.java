package com.sunveee.framework.keycloak.oidc.example.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sunveee.framework.common.utils.json.JSONUtils;
import com.sunveee.framework.keycloak.oidc.utils.KeycloakTokenParser;
import com.sunveee.framework.keycloak.oidc.utils.KeycloakTokenParser.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/admin")
public class TestAdminController {

    @RequestMapping("/ping")
    public String ping() {

        KeycloakTokenDetail keycloakTokenDetail;
        try {
            keycloakTokenDetail = KeycloakTokenParser.keycloakTokenDetail("groups");
        } catch (ParseKeycloakTokenDetailException e) {
            log.warn("Token信息解析失败，请检查token内容", e);
            throw new RuntimeException(e);
        } catch (GetHttpServletRequestException e) {
            log.warn("获取HttpServletRequest失败，请确定当前线程是否为HTTP处理线程", e);
            throw new RuntimeException(e);
        }

        log.info("userId: {}.", keycloakTokenDetail.getUserId());
        log.info("用户角色: {}.", keycloakTokenDetail.getRealmUserRoles());
        log.info("用户所在的用户组: {}.", keycloakTokenDetail.getRealmUserGroups());
        log.info("用户所关联的客户端角色: {}.", keycloakTokenDetail.getClientUserRolesMap());
        log.info("Access Token: {}.", keycloakTokenDetail.getAccessTokenString());
        log.info("解码后的token信息: {}.", JSONUtils.toJSONString(keycloakTokenDetail.getAccessToken()));

        return "Call /admin/ping success.";
    }

}
