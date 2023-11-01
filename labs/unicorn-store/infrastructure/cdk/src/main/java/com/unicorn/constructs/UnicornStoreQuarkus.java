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

import java.util.HashMap;
import java.util.List;

public class UnicornStoreQuarkus extends Construct {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreQuarkus(final Construct scope, final String id, final InfrastructureStack infrastructureStack) {
        super(scope, id);
        this.infrastructureStack = infrastructureStack;

        //Quarkus app
        var unicornStoreQuarkus = createUnicornLambdaFunction();
        infrastructureStack.getEventBridge().grantPutEventsTo(unicornStoreQuarkus);

        var restApi = setupRestApi(unicornStoreQuarkus);

        new CfnOutput(scope, "ApiEndpointQuarkus", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());
    }

    private RestApi setupRestApi(Function unicornStoreLambdaContainer) {
        return LambdaRestApi.Builder.create(this, "UnicornStoreQuarkusApi")
                .restApiName("UnicornStoreQuarkusApi")
                .handler(unicornStoreLambdaContainer)
                .build();
    }

    private Function createUnicornLambdaFunction() {
        return Function.Builder.create(this, "UnicornStoreQuarkusFunction")
                .runtime(Runtime.JAVA_17)
                .functionName("unicorn-store-quarkus")
                .memorySize(2048)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/alternatives/unicorn-store-quarkus/target/function.zip"))
                .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(new HashMap<>() {{
                    put("QUARKUS_DATASOURCE_PASSWORD", infrastructureStack.getDatabaseSecretString());
                    put("QUARKUS_DATASOURCE_JDBC_URL", infrastructureStack.getDatabaseJDBCConnectionString());
                    put("QUARKUS_DATASOURCE_JDBC_INITIAL_SIZE", "1");
                    put("QUARKUS_DATASOURCE_JDBC_MIN_SIZE", "1");
                    put("QUARKUS_DATASOURCE_JDBC_MAX_SIZE", "1");
                    put("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
                }})
                .build();
    }
}
