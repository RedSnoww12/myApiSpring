package com.etna.myapi.listeners;


import lombok.extern.log4j.Log4j2;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Async
@Component
@Log4j2
public class OnApplicationReadyEvent implements ApplicationListener<ApplicationReadyEvent> {
	public static final String APPLICATION_READY = "application ok";

	@Override
	public void onApplicationEvent(ApplicationReadyEvent input) {
		log.info(APPLICATION_READY);
	}
}
