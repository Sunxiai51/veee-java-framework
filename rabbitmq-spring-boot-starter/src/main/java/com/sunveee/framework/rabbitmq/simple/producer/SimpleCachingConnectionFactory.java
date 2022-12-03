package com.sunveee.framework.rabbitmq.simple.producer;

import java.time.Duration;

import org.springframework.amqp.rabbit.connection.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.context.properties.PropertyMapper;

import com.rabbitmq.client.ConnectionFactory;

/**
 * SimpleCachingConnectionFactory.java
 * <p>
 * 开启confirm和return的CachingConnectionFactory
 *
 * @author SunVeee
 * @version 2021-01-12 10:44:12
 */
public class SimpleCachingConnectionFactory extends CachingConnectionFactory {

    private SimpleCachingConnectionFactory() {
        super();
    }

    private SimpleCachingConnectionFactory(ConnectionFactory object) {
        super(object);
    }

    /**
     * 复制{@link RabbitAutoConfiguration.RabbitConnectionFactoryCreator#rabbitConnectionFactory(RabbitProperties, ObjectProvider)}内容，并使用自定义的配置覆盖
     * 
     * @param properties
     * @param connectionNameStrategy
     * @return
     * @throws Exception
     */
    public static SimpleCachingConnectionFactory instance(RabbitProperties properties, ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) throws Exception {
        // 与spring原生connectionFactory使用同一套配置
        PropertyMapper map = PropertyMapper.get();
        SimpleCachingConnectionFactory factory = new SimpleCachingConnectionFactory(
                getRabbitConnectionFactoryBean(properties).getObject());
        map.from(properties::determineAddresses).to(factory::setAddresses);
        map.from(properties::isPublisherReturns).to(factory::setPublisherReturns);
        map.from(properties::getPublisherConfirmType).whenNonNull().to(factory::setPublisherConfirmType);
        RabbitProperties.Cache.Channel channel = properties.getCache().getChannel();
        map.from(channel::getSize).whenNonNull().to(factory::setChannelCacheSize);
        map.from(channel::getCheckoutTimeout).whenNonNull().as(Duration::toMillis)
                .to(factory::setChannelCheckoutTimeout);
        RabbitProperties.Cache.Connection connection = properties.getCache().getConnection();
        map.from(connection::getMode).whenNonNull().to(factory::setCacheMode);
        map.from(connection::getSize).whenNonNull().to(factory::setConnectionCacheSize);
        map.from(connectionNameStrategy::getIfUnique).whenNonNull().to(factory::setConnectionNameStrategy);

        // 自定义配置覆盖
        factory.setPublisherConfirmType(ConfirmType.CORRELATED); // 开启confirm
        factory.setPublisherReturns(true); // 开启return
        return factory;
    }

    /**
     * copy from
     * {@link RabbitAutoConfiguration.RabbitConnectionFactoryCreator#getRabbitConnectionFactoryBean(RabbitProperties)}
     * 
     * @param properties
     * @return
     * @throws Exception
     */
    private static RabbitConnectionFactoryBean getRabbitConnectionFactoryBean(RabbitProperties properties)
            throws Exception {
        PropertyMapper map = PropertyMapper.get();
        RabbitConnectionFactoryBean factory = new RabbitConnectionFactoryBean();
        map.from(properties::determineHost).whenNonNull().to(factory::setHost);
        map.from(properties::determinePort).to(factory::setPort);
        map.from(properties::determineUsername).whenNonNull().to(factory::setUsername);
        map.from(properties::determinePassword).whenNonNull().to(factory::setPassword);
        map.from(properties::determineVirtualHost).whenNonNull().to(factory::setVirtualHost);
        map.from(properties::getRequestedHeartbeat).whenNonNull().asInt(Duration::getSeconds)
                .to(factory::setRequestedHeartbeat);
        RabbitProperties.Ssl ssl = properties.getSsl();
        if (ssl.determineEnabled()) {
            factory.setUseSSL(true);
            map.from(ssl::getAlgorithm).whenNonNull().to(factory::setSslAlgorithm);
            map.from(ssl::getKeyStoreType).to(factory::setKeyStoreType);
            map.from(ssl::getKeyStore).to(factory::setKeyStore);
            map.from(ssl::getKeyStorePassword).to(factory::setKeyStorePassphrase);
            map.from(ssl::getTrustStoreType).to(factory::setTrustStoreType);
            map.from(ssl::getTrustStore).to(factory::setTrustStore);
            map.from(ssl::getTrustStorePassword).to(factory::setTrustStorePassphrase);
            map.from(ssl::isValidateServerCertificate)
                    .to((validate) -> factory.setSkipServerCertificateValidation(!validate));
            map.from(ssl::getVerifyHostname).to(factory::setEnableHostnameVerification);
        }
        map.from(properties::getConnectionTimeout).whenNonNull().asInt(Duration::toMillis)
                .to(factory::setConnectionTimeout);
        factory.afterPropertiesSet();
        return factory;
    }
}
