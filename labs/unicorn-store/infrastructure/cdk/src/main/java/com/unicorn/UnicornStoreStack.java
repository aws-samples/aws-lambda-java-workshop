package com.unicorn;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class UnicornStoreStack extends Stack {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreStack(final Construct scope, final String id, final StackProps props,
                             final InfrastructureStack infrastructureStack) {
        super(scope, id, props);

        //Get previously created infrastructure stack
        this.infrastructureStack = infrastructureStack;
        var eventBridge = infrastructureStack.getEventBridge();

        //Create Spring Lambda function
        var unicornStoreSpringLambda = createUnicornLambdaFunction();

        //Permission for Spring Boot Lambda Function
        eventBridge.grantPutEventsTo(unicornStoreSpringLambda);

        //Setup a Proxy-Rest API to access the Spring Lambda function
        var restApi = setupRestApi(unicornStoreSpringLambda);

        //Create output values for later reference
        new CfnOutput(this, "unicorn-store-spring-function-arn", CfnOutputProps.builder()
                .value(unicornStoreSpringLambda.getFunctionArn())
                .build());

        new CfnOutput(this, "ApiEndpointSpring", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());
    }

    private RestApi setupRestApi(Function unicornStoreSpringLambda) {
        return LambdaRestApi.Builder.create(this, "UnicornStoreSpringApi")
                .restApiName("UnicornStoreSpringApi")
                .handler(unicornStoreSpringLambda)
                .build();
    }

    private Function createUnicornLambdaFunction() {
        return Function.Builder.create(this, "UnicornStoreSpringFunction")
                .runtime(Runtime.JAVA_21)
                .functionName("unicorn-store-spring")
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/unicorn-store-spring/target/store-spring-1.0.0.jar"))
                .handler("com.amazonaws.serverless.proxy.spring.SpringDelegatingLambdaContainerHandler")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(Map.of(
                    "MAIN_CLASS", "com.unicorn.store.StoreApplication",
                    "SPRING_DATASOURCE_PASSWORD", infrastructureStack.getDatabaseSecretString(),
                    "SPRING_DATASOURCE_URL", infrastructureStack.getDatabaseJDBCConnectionString(),
                    "SPRING_DATASOURCE_HIKARI_maximumPoolSize", "1",
                    "AWS_SERVERLESS_JAVA_CONTAINER_INIT_GRACE_TIME", "500"
                ))
                .build();
    }

}
