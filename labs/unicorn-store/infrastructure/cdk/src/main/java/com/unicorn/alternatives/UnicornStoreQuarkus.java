package com.unicorn.alternatives;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.*;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;



public class UnicornStoreQuarkus extends Stack {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreQuarkus(final Construct scope, final String id, final StackProps props, final InfrastructureStack infrastructureStack) {
        super(scope, id, props);
        this.infrastructureStack = infrastructureStack;

        //Quarkus app
        var unicornStoreQuarkus = createUnicornLambdaFunction();
        infrastructureStack.getEventBridge().grantPutEventsTo(unicornStoreQuarkus);

        var restApi = setupRestApi(unicornStoreQuarkus);

        new CfnOutput(this, "ApiEndpointQuarkus", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());
    }

    private RestApi setupRestApi(Version unicornStoreLambdaContainer) {
        return LambdaRestApi.Builder.create(this, "UnicornStoreQuarkusApi")
                .restApiName("UnicornStoreQuarkusApi")
                .handler(unicornStoreLambdaContainer)
                .build();
    }

    private Version createUnicornLambdaFunction() {
        var lambda =  Function.Builder.create(this, "UnicornStoreQuarkusFunction")
                .runtime(Runtime.JAVA_21)
                .functionName("unicorn-store-quarkus")
                .memorySize(2048)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/alternatives/unicorn-store-quarkus/target/function.zip"))
                .handler("io.quarkus.amazon.lambda.runtime.QuarkusStreamHandler::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .snapStart(SnapStartConf.ON_PUBLISHED_VERSIONS)
                .environment(new HashMap<>() {{
                    put("QUARKUS_DATASOURCE_PASSWORD", infrastructureStack.getDatabaseSecretString());
                    put("QUARKUS_DATASOURCE_JDBC_URL", infrastructureStack.getDatabaseJDBCConnectionString());
                    put("QUARKUS_DATASOURCE_JDBC_INITIAL_SIZE", "1");
                    put("QUARKUS_DATASOURCE_JDBC_MIN_SIZE", "0");
                    put("QUARKUS_DATASOURCE_JDBC_MAX_SIZE", "1");
                }})
                .build();
        return lambda.getCurrentVersion();
    }
}
