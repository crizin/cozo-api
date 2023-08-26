package me.cozo.api.application.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.messaging.MessageHandlingException;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ErrorCommandHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger("integration");

	@ServiceActivator(inputChannel = "errorChannel")
	public void loggingError(ErrorMessage message) {
		MessageHandlingException payload = (MessageHandlingException) message.getPayload();
		LOGGER.error("Command error [payload={}]", Objects.requireNonNull(payload.getFailedMessage()).getPayload());
		LOGGER.error(payload.getCause().getMessage(), payload.getCause());
	}
}
