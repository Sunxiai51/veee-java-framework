package com.sunveee.framework.rabbitmq.autoconfigure;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionNameStrategy;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sunveee.framework.rabbitmq.simple.consumer.SimpleConsumerContainerFactory;
import com.sunveee.framework.rabbitmq.simple.consumer.aspect.SimpleConsumerLogPrintAspect;
import com.sunveee.framework.rabbitmq.simple.producer.SimpleCachingConnectionFactory;
import com.sunveee.framework.rabbitmq.simple.producer.SimpleRabbitTemplate;
import com.sunveee.framework.rabbitmq.simple.properties.*;

@Configuration
@ConditionalOnProperty(SimpleProperties.SIMPLE_ENABLED)
@EnableConfigurationProperties({
        SimpleProperties.class,
        SimpleProducerProperties.class,
        SimpleConsumerProperties.class,
        RabbitProperties.class })
public class RabbitSimpleAutoConfiguration {

    @Bean
    @ConditionalOnProperty(SimpleProducerProperties.SIMPLE_PRODUCER_ENABLED)
    @ConditionalOnMissingBean
    public SimpleRabbitTemplate simpleRabbitTemplate(SimpleCachingConnectionFactory connectionFactory, SimpleProducerProperties simpleProducerProperties) {
        return new SimpleRabbitTemplate(connectionFactory, simpleProducerProperties);
    }

    @Bean
    @ConditionalOnProperty(SimpleProducerProperties.SIMPLE_PRODUCER_ENABLED)
    @ConditionalOnMissingBean
    public SimpleCachingConnectionFactory simpleCachingConnectionFactory(RabbitProperties properties,
            ObjectProvider<ConnectionNameStrategy> connectionNameStrategy) throws Exception {
        return SimpleCachingConnectionFactory.instance(properties, connectionNameStrategy);
    }

    // 死信队列
    public static final String RECOVERY_EXCHANGE = "recovery_exchange";
    public static final String RECOVERY_QUEUE = "recovery_queue";
    public static final String RECOVERY_ROUTINGKEY = "recovery";

    @Bean
    @ConditionalOnProperty(SimpleConsumerProperties.SIMPLE_CONSUMER_ENABLED)
    public SimpleConsumerContainerFactory simpleConsumerContainerFactory(SimpleRabbitListenerContainerFactoryConfigurer configurer, ConnectionFactory connectionFactory) {
        return new SimpleConsumerContainerFactory(configurer, connectionFactory);
    }

    @Bean
    @ConditionalOnProperty(SimpleConsumerProperties.SIMPLE_CONSUMER_ENABLED)
    public DirectExchange recoveryExchange() {
        return new DirectExchange(RECOVERY_EXCHANGE, true, false);
    }

    @Bean
    @ConditionalOnProperty(SimpleConsumerProperties.SIMPLE_CONSUMER_ENABLED)
    public Queue recoveryQueue() {
        return new Queue(RECOVERY_QUEUE, true);
    }

    @Bean
    @ConditionalOnProperty(SimpleConsumerProperties.SIMPLE_CONSUMER_ENABLED)
    public Binding errorBinding(Queue recoveryQueue, DirectExchange recoveryExchange) {
        return BindingBuilder.bind(recoveryQueue).to(recoveryExchange).with(RECOVERY_ROUTINGKEY);
    }

    @Bean
    @ConditionalOnProperty(SimpleConsumerProperties.SIMPLE_CONSUMER_ENABLED)
    public MessageRecoverer messageRecoverer(SimpleRabbitTemplate rabbitTemplate) {
        return new RepublishMessageRecoverer(rabbitTemplate, RECOVERY_EXCHANGE, RECOVERY_ROUTINGKEY);
    }

    @Bean
    @ConditionalOnProperty(SimpleConsumerProperties.SIMPLE_CONSUMER_ENABLED)
    @ConditionalOnMissingBean
    public SimpleConsumerLogPrintAspect simpleConsumerLogPrintAspect() {
        return new SimpleConsumerLogPrintAspect();
    }

}
