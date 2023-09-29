package com.etna.myapi.publisher;

import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class RabbitMQProducer {

    @Value("${rabbitmq.exchange.name}")
    private String topicExchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(Object object) {
        log.info("Message sent -> " + object.toString());
        rabbitTemplate.convertAndSend(topicExchangeName, routingKey, object);
    }
}
