package com.unicorn.constructs;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class UnicornStoreSpringNative extends Construct {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreSpringNative(final Construct scope, final String id, final InfrastructureStack infrastructureStack) {
        super(scope, id);
        this.infrastructureStack = infrastructureStack;

        //Spring Native GraalVM function app
        var unicornStoreSpringNative = createUnicornLambdaFunction();
        infrastructureStack.getEventBridge().grantPutEventsTo(unicornStoreSpringNative);

        var restApi = setupRestApi(unicornStoreSpringNative);

        new CfnOutput(scope, "ApiEndpointSpringNative", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());
    }

    private RestApi setupRestApi(Function unicornStoreSpringNative) {
        var api = LambdaRestApi.Builder.create(this, "UnicornStoreSpringNativeApi")
                .restApiName("UnicornStoreSpringNativeApi")
                .handler(unicornStoreSpringNative)
                .build();

        return api;
    }

    private Function createUnicornLambdaFunction() {
        return Function.Builder.create(this, "UnicornStoreSpringNativeFunction")
                .runtime(Runtime.PROVIDED_AL2)
                .functionName("unicorn-store-spring-native-graalvm")
                .memorySize(2048)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/alternatives/unicorn-store-spring-native-graalvm/lambda-spring-native.zip"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(Map.of(
                    "SPRING_DATASOURCE_PASSWORD", infrastructureStack.getDatabaseSecretString(),
                    "SPRING_DATASOURCE_URL", infrastructureStack.getDatabaseJDBCConnectionString(),
                    "SPRING_DATASOURCE_HIKARI_maximumPoolSize", "1")
                )
                .build();
    }
}
