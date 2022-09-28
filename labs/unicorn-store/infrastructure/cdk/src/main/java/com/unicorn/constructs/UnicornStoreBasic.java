package com.unicorn.constructs;

import com.unicorn.InfrastructureStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.apigateway.LambdaIntegration;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.Resource;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;

public class UnicornStoreBasic extends Construct {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreBasic(final Construct scope, final String id, InfrastructureStack infrastructureStack) {
        super(scope, id);
        this.infrastructureStack = infrastructureStack;

        var unicornStoreBasicPostLambda= createUnicornBasicLambdaFunction(
        		"unicorn-store-basic-post",
        		"com.unicorn.store.handler.UnicornPostRequestHandler::handleRequest"
        );
        
        var unicornStoreBasicPutLambda= createUnicornBasicLambdaFunction(
        		"unicorn-store-basic-put",
        		"com.unicorn.store.handler.UnicornPutRequestHandler::handleRequest"
        );
        
        var unicornStoreBasicGetLambda= createUnicornBasicLambdaFunction(
        		"unicorn-store-basic-get",
        		"com.unicorn.store.handler.UnicornGetRequestHandler::handleRequest"
        );
        
        var unicornStoreBasicDeleteLambda= createUnicornBasicLambdaFunction(
        		"unicorn-store-basic-delete",
        		"com.unicorn.store.handler.UnicornDeleteRequestHandler::handleRequest"
        );
        
        var restBasicApi = setupRestBasicApi(unicornStoreBasicPostLambda, unicornStoreBasicPutLambda, 
        		unicornStoreBasicGetLambda, unicornStoreBasicDeleteLambda);

        new CfnOutput(scope, "ApiEndpointBasic", CfnOutputProps.builder()
                .value(restBasicApi.getUrl())
                .build());
    }
    

    private RestApi setupRestBasicApi(Function postLambda, Function putLambda, Function getLambda, Function deleteLambda) {
        var restApi = LambdaRestApi.Builder.create(this, "UnicornStoreBasicApi")
                .restApiName("UnicornStoreBasicApi")
                .handler(postLambda)
                .proxy(false)
                .build();

        Resource unicornResource = restApi.getRoot().addResource("unicorns");
        unicornResource.addMethod("POST", new LambdaIntegration(postLambda));
        
        Resource unicornResourceById = unicornResource.addResource("{id}");
        unicornResourceById.addMethod("GET", new LambdaIntegration(getLambda));
        unicornResourceById.addMethod("PUT", new LambdaIntegration(putLambda));
        unicornResourceById.addMethod("DELETE", new LambdaIntegration(deleteLambda));
        return restApi;
    }

    private Function createUnicornBasicLambdaFunction(String name, String handler) {
    	Function lambda = Function.Builder.create(this, name)
                .runtime(Runtime.JAVA_11)
                .memorySize(2048)
                .functionName(name)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../..//software/alternatives/unicorn-store-basic/target/store-basic-1.0.0.jar"))
                .handler(handler)
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(new HashMap<>() {{
                    put("DB_PASSWORD", infrastructureStack.getDatabaseSecretString());
                    put("DB_CONNECTION_URL", infrastructureStack.getDatabaseJDBCConnectionString());
                    put("DB_USER", "postgres");
                    put("JAVA_TOOL_OPTIONS", "-XX:+TieredCompilation -XX:TieredStopAtLevel=1");
                }})
                .build();

        infrastructureStack.getEventBridge().grantPutEventsTo(lambda);
        
        return lambda;
    }
}
