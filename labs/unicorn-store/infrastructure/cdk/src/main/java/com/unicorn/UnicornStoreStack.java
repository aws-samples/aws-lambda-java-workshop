package com.unicorn;

import com.unicorn.constructs.UnicornStoreBasic;
import com.unicorn.constructs.UnicornStoreMicronaut;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnicornStoreStack extends Stack {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreStack(final Construct scope, final String id, final StackProps props,
                             final InfrastructureStack infrastructureStack) {
        super(scope, id, props);
        this.infrastructureStack = infrastructureStack;
        var database = infrastructureStack.getDatabase();
        var eventBridge = infrastructureStack.getEventBridge();


        //SpringBoot app
        var unicornStoreLambdaContainer = createUnicornLambdaFunction();

        //Permission for Lambda Function
        eventBridge.grantPutEventsTo(unicornStoreLambdaContainer);

        var restApi = setupRestApi(unicornStoreLambdaContainer);
        new UnicornStoreMicronaut(this, "UnicornStoreMicronaut", infrastructureStack);
        new UnicornStoreBasic(this, "UnicornStoreBasic", infrastructureStack);

        new CfnOutput(this, "unicorn-store-spring-function-arn", CfnOutputProps.builder()
                .value(unicornStoreLambdaContainer.getFunctionArn())
                .build());

        new CfnOutput(this, "ApiEndpointSpring", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());
    }

    private RestApi setupRestApi(Function unicornStoreLambdaContainer) {
        return LambdaRestApi.Builder.create(this, "UnicornStoreSpringApi")
                .restApiName("UnicornStoreSpringApi")
                .handler(unicornStoreLambdaContainer)
                .build();
    }

    private Function createUnicornLambdaFunction() {
        return Function.Builder.create(this, "UnicornStoreSpringFunction")
                .runtime(Runtime.JAVA_11)
                .functionName("unicorn-store-spring")
                .memorySize(1024)
                .timeout(Duration.seconds(29))
                .logRetention(RetentionDays.FIVE_DAYS)
                .code(Code.fromAsset("../../software/unicorn-store-spring/target/store-spring-1.0.0.jar"))
                .handler("com.unicorn.store.StreamLambdaHandler::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(Map.of(
                    "SPRING_DATASOURCE_PASSWORD", infrastructureStack.getDatabaseSecretString(),
                    "SPRING_DATASOURCE_URL", infrastructureStack.getDatabaseJDBCConnectionString(),
                    "SPRING_DATASOURCE_HIKARI_maximumPoolSize", "1",
                    "AWS_SERVERLESS_JAVA_CONTAINER_INIT_GRACE_TIME", "500"
                    //,"JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1"
                ))
                .build();
    }
}
