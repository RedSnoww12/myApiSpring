package com.etna.myapi.listeners;

import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Async
@Component
@Log4j2
public class OnRabbitMqReady implements ConnectionListener {
    public static final String RABBITMQ_READY = "rabbitmq ok : ";

    @Override
    public void onCreate(Connection connection) {
        log.info(RABBITMQ_READY + connection.toString());
    }

    @Override
    public void onClose(Connection connection) {
        log.info(RABBITMQ_READY + connection.toString());
    }

    @Override
    public void onShutDown(ShutdownSignalException signal) {
        log.info(RABBITMQ_READY + signal.toString());
    }

    @Override
    public void onFailed(Exception exception) {
        log.info(RABBITMQ_READY + exception.toString());
    }
}
