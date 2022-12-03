package com.sunveee.framework.keycloak.oidc.httpclient;

import okhttp3.OkHttpClient;

public interface OkHttpClientFactory {

    OkHttpClient okHttpClient();
}
