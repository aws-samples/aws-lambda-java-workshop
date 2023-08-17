package com.unicorn.constructs;

import java.util.HashMap;
import java.util.List;

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

public class UnicornStoreMicronaut extends Construct {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreMicronaut(final Construct scope, final String id, final InfrastructureStack infrastructureStack) {
        super(scope, id);
        this.infrastructureStack = infrastructureStack;

        //Micronaut app
        var unicornStoreMicronaut = createUnicornLambdaFunction();
        infrastructureStack.getEventBridge().grantPutEventsTo(unicornStoreMicronaut);

        var restApi = setupRestApi(unicornStoreMicronaut);

        new CfnOutput(scope, "ApiEndpointMicronaut", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());
    }

    private RestApi setupRestApi(Function unicornStoreLambdaContainer) {
        return LambdaRestApi.Builder.create(this, "UnicornStoreMicronautApi")
                .restApiName("UnicornStoreMicronautApi")
                .handler(unicornStoreLambdaContainer)
                .build();
    }

    private Function createUnicornLambdaFunction() {
        return Function.Builder.create(this, "UnicornStoreMicronautFunction")
                .runtime(Runtime.JAVA_17)
                .functionName("unicorn-store-micronaut")
                .memorySize(2048)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../..//software/alternatives/unicorn-store-micronaut/target/store-micronaut-1.0.0.jar"))
                .handler("com.unicorn.store.handler.UnicornPostRequestHandler")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(new HashMap<>() {{
                    put("DATASOURCES_DEFAULT_PASSWORD", infrastructureStack.getDatabaseSecretString());
                    put("DATASOURCES_DEFAULT_URL", infrastructureStack.getDatabaseJDBCConnectionString());
                    put("DATASOURCES_DEFAULT_maxPoolSize", "1");
                }})
                .build();
    }
}
