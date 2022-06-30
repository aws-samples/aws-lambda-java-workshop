package com.unicorn.constructs;

import com.unicorn.InfrastructureStack;
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
                .runtime(Runtime.JAVA_11)
                .functionName("unicorn-store-micronaut")
                .memorySize(2048)
                .timeout(Duration.seconds(29))
                .logRetention(RetentionDays.FIVE_DAYS)
                .code(Code.fromAsset("../..//software/unicorn-store-micronaut/target/store-micronaut-1.0.0.jar"))
                .handler("com.unicorn.store.handler.UnicornPostRequestHandler::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(new HashMap<>() {{
                    put("DATASOURCES_DEFAULT_PASSWORD", infrastructureStack.getDatabaseSecretString());
                    put("DATASOURCES_DEFAULT_URL", infrastructureStack.getDatabaseJDBCConnectionString());
                    put("DATASOURCES_DEFAULT_maxPoolSize", "1");
                    put("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
                }})
                .build();
    }
}
