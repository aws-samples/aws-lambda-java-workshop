package com.unicorn.store;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unicorn.store.model.Unicorn;
import com.unicorn.store.model.UnicornEventType;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;
import org.springframework.nativex.hint.TypeAccess;
import org.springframework.nativex.hint.TypeHint;

@SpringBootApplication
@EnableJdbcRepositories
@TypeHint(types = {
		APIGatewayProxyRequestEvent.class
},
		typeNames = {
				"com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent$ProxyRequestContext",
				"com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent$RequestIdentity"
		})
@TypeHint(
		types = { Unicorn.class, UnicornEventType.class}, access = { TypeAccess.PUBLIC_CONSTRUCTORS, TypeAccess.PUBLIC_METHODS }
)
public class StoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(StoreApplication.class, args);
	}

	@Bean
	public ObjectMapper getObjectMapper() {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		return objectMapper;
	}

}
