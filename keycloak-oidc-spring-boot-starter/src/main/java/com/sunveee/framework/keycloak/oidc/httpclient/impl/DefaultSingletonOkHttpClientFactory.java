package com.sunveee.framework.keycloak.oidc.httpclient.impl;

import com.sunveee.framework.keycloak.oidc.httpclient.OkHttpClientFactory;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;

import java.util.concurrent.TimeUnit;

public class DefaultSingletonOkHttpClientFactory implements OkHttpClientFactory {

    private static DefaultSingletonOkHttpClientFactory instance = new DefaultSingletonOkHttpClientFactory();

    private static OkHttpClient client = null;

    private DefaultSingletonOkHttpClientFactory() {
    }

    public static DefaultSingletonOkHttpClientFactory getInstance() {
        return instance;
    }

    @Override
    public OkHttpClient okHttpClient() {
        if (null == client) {
            // 默认client配置
            client = new Builder()
                    .connectTimeout(5L, TimeUnit.SECONDS)
                    .build();
        }
        return client;
    }

}
