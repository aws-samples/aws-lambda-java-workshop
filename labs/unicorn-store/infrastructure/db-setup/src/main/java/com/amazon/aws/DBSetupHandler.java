package com.amazon.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.jr.ob.JSON;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.retries.api.BackoffStrategy;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;

public class DBSetupHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final String databaseConnection;
    private final String databaseUser;
    private final String databasePassword;
    private final Logger logger = LoggerFactory.getLogger(DBSetupHandler.class);

    private static final SecretsManagerAsyncClient smClient = SecretsManagerAsyncClient
            .builder()
            .overrideConfiguration(o -> o.retryStrategy(b -> { // fix for 4% of accounts facing "socket operation timed out" during provisioning
                b.maxAttempts(10);
                b.backoffStrategy(BackoffStrategy.exponentialDelay(Duration.ofMillis(150),
                        Duration.ofSeconds(30)));
            }))
            .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
            .httpClientBuilder(AwsCrtAsyncHttpClient.builder())
            .build();

    public DBSetupHandler() {
        try {
            var secretValueRequest = GetSecretValueRequest
                    .builder()
                    .secretId("unicornstore-db-secret")
                    .build();
            var secretValue = smClient.getSecretValue(secretValueRequest).get();
            var secretValueJson = JSON.std.mapFrom(secretValue.secretString());
            var host = secretValueJson.get("host").toString();
            var port = secretValueJson.get("port").toString();
            var dbName = secretValueJson.get("dbname").toString();

            databaseUser = secretValueJson.get("username").toString();
            databasePassword = secretValueJson.get("password").toString();
            databaseConnection = String.format("jdbc:postgresql://%s:%s/%s", host, port, dbName);

            logger.info("JDBC URL retrieved from secret: " + databaseConnection);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new RuntimeException("Error while doing SDK call...", e);
        }
    }

    public APIGatewayProxyResponseEvent handleRequest(final APIGatewayProxyRequestEvent input, final Context context) {
        try(var connection = DriverManager.getConnection(databaseConnection, databaseUser, databasePassword)) {
            try(var statement = connection.createStatement()) {
                try(var sqlFile = getClass().getClassLoader().getResourceAsStream("setup.sql")) {
                    statement.executeUpdate(IOUtils.toString(sqlFile));
                    return new APIGatewayProxyResponseEvent()
                            .withStatusCode(200)
                            .withBody("DB Setup successful");
                }
            }
        } catch (Exception sqlException) {
            logger.error("Error connection to the database:" + sqlException.getMessage());
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withBody("Error initializing the database");
        }
    }
}
