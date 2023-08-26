package me.cozo.api.config;

import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway(defaultRequestChannel = "integrationFlow.input")
public interface CommandGateway {

	void send(Object command);
}
