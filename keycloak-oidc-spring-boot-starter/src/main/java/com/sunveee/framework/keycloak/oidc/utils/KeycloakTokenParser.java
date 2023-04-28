package com.sunveee.framework.keycloak.oidc.utils;

import java.util.*;

import javax.servlet.http.HttpServletRequest;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * KeycloakTokenParser.java
 *
 * @author  SunVeee
 * @version 2021-09-10 11:35:15
 */
@Slf4j
public class KeycloakTokenParser {

    /**
     * 获取Keycloak Token详情
     * 
     * @param  groupsTokenClaimName              用户组信息对应的TokenClaimName
     * @return
     * @throws ParseKeycloakTokenDetailException
     * @throws GetHttpServletRequestException
     */
    @SuppressWarnings("unchecked")
    public static KeycloakTokenDetail keycloakTokenDetail(String groupsTokenClaimName)
            throws ParseKeycloakTokenDetailException, GetHttpServletRequestException {
        // 获取HttpRequest
        HttpServletRequest request = httpRequest();

        // 解析得到以下值
        final String userId; // 用户唯一标识
        final String accessTokenString; // 用户本次请求的accessToken字符串
        final AccessToken accessToken; // 用户本次请求的accessToken
        final Set<String> realmUserRoles; // 用户roles
        final Set<String> realmUserGroups; // 用户groups
        final Map<String/* clientId */, Set<String>/* roles */> clientUserRolesMap; // 用户clientRoles

        try {
            KeycloakPrincipal<?> keycloakPrincipal = (KeycloakPrincipal<?>) request.getUserPrincipal();
            if (Objects.isNull(keycloakPrincipal)) {
                throw new ParseKeycloakTokenDetailException("validated keycloak principal not found");
            }
            userId = keycloakPrincipal.getName();

            KeycloakSecurityContext keycloakSecurityContext = keycloakPrincipal.getKeycloakSecurityContext();
            accessTokenString = keycloakSecurityContext.getTokenString();
            accessToken = keycloakSecurityContext.getToken();

            // 解析realmUserRoles
            if (null == accessToken.getRealmAccess() || null == accessToken.getRealmAccess().getRoles()) {
                realmUserRoles = new HashSet<>();
            } else {
                realmUserRoles = accessToken.getRealmAccess().getRoles();
            }

            // 解析realmUserGroups
            Map<String, Object> claims = accessToken.getOtherClaims();
            if (null != claims && claims.containsKey(groupsTokenClaimName)) {
                realmUserGroups = new HashSet<>((ArrayList<String>) claims.get(groupsTokenClaimName));
            } else {
                realmUserGroups = new HashSet<>();
            }

            // 解析clientUserRolesMap
            Map<String, Access> resourceAccess = accessToken.getResourceAccess();
            if (null == resourceAccess || resourceAccess.isEmpty()) {
                clientUserRolesMap = new HashMap<>();
            } else {
                clientUserRolesMap = new HashMap<>();
                for (String clientId : resourceAccess.keySet()) {
                    clientUserRolesMap.put(clientId, resourceAccess.get(clientId).getRoles());
                }
            }
        } catch (ParseKeycloakTokenDetailException pktde) {
            throw pktde;
        } catch (Exception e) {
            throw new ParseKeycloakTokenDetailException("parse keycloak accessToken failed", e);
        }

        KeycloakTokenDetail result = KeycloakTokenDetail.builder()
                .userId(userId)
                .accessTokenString(accessTokenString)
                .accessToken(accessToken)
                .realmUserRoles(realmUserRoles)
                .realmUserGroups(realmUserGroups)
                .clientUserRolesMap(clientUserRolesMap)
                .build();
        result.selfValidate();

        log.debug("KeycloakTokenDetail: {}.", result);
        return result;
    }

    private static HttpServletRequest httpRequest() throws GetHttpServletRequestException {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            return request;
        } catch (Throwable e) {
            throw new GetHttpServletRequestException("get javax.servlet.http.HttpServletRequest failed", e);
        }
    }

    @Getter
    @Builder
    public static class KeycloakTokenDetail {
        /** 用户唯一标识 */
        private String userId;

        /** 用户本次请求的accessToken字符串 */
        private String accessTokenString;

        /** 用户本次请求的accessToken */
        private AccessToken accessToken;

        /** 用户roles */
        private Set<String> realmUserRoles;

        /** 用户groups */
        private Set<String> realmUserGroups;

        /** 用户clientRoles */
        private Map<String/* clientId */, Set<String>/* roles */> clientUserRolesMap;

        public void selfValidate() throws ParseKeycloakTokenDetailException {
            if (StringUtils.isEmpty(userId)) {
                throw new ParseKeycloakTokenDetailException("empty userId");
            }
            if (StringUtils.isEmpty(accessTokenString)) {
                throw new ParseKeycloakTokenDetailException("empty accessTokenString");
            }
            if (Objects.isNull(accessToken)) {
                throw new ParseKeycloakTokenDetailException("null accessToken");
            }
            if (Objects.isNull(realmUserRoles)) {
                throw new ParseKeycloakTokenDetailException("null realmUserRoles");
            }
            if (Objects.isNull(realmUserGroups)) {
                throw new ParseKeycloakTokenDetailException("null realmUserGroups");
            }
            if (Objects.isNull(clientUserRolesMap)) {
                throw new ParseKeycloakTokenDetailException("null clientUserRolesMap");
            }
        }
    }

    public static class ParseKeycloakTokenDetailException extends Exception {
        public ParseKeycloakTokenDetailException(String message) {
            super(message);
        }

        public ParseKeycloakTokenDetailException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class GetHttpServletRequestException extends Exception {
        public GetHttpServletRequestException(String message) {
            super(message);
        }

        public GetHttpServletRequestException(String message, Throwable cause) {
            super(message, cause);
        }
    }

}
