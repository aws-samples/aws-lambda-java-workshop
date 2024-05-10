package com.unicorn.alternatives;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;
import java.util.Map;

public class UnicornStoreSpringGraalVM extends Stack {

    private final InfrastructureStack infrastructureStack;

    public UnicornStoreSpringGraalVM(final Construct scope, final String id, final StackProps props, final InfrastructureStack infrastructureStack) {
        super(scope, id, props);
        this.infrastructureStack = infrastructureStack;

        var unicornStoreSpringGraalVM = createUnicornLambdaFunction();
        infrastructureStack.getEventBridge().grantPutEventsTo(unicornStoreSpringGraalVM);

        var restApi = setupRestApi(unicornStoreSpringGraalVM);

        new CfnOutput(this, "ApiEndpointSpringGraalVM", CfnOutputProps.builder()
                .value(restApi.getUrl())
                .build());

        //Create output values for later reference
        new CfnOutput(this, "unicorn-store-spring-graalvm-function-arn", CfnOutputProps.builder()
                .value(unicornStoreSpringGraalVM.getFunctionArn())
                .build());
    }

    private RestApi setupRestApi(Function unicornStoreSpringGraalVM) {
        return LambdaRestApi.Builder.create(this, "UnicornStoreSpringGraalVMApi")
                .restApiName("UnicornStoreSpringGraalVMApi")
                .handler(unicornStoreSpringGraalVM)
                .build();
    }

    private Function createUnicornLambdaFunction() {
        return Function.Builder.create(this, "UnicornStoreSpringGraalVMFunction")
                .runtime(Runtime.PROVIDED_AL2023)
                .functionName("unicorn-store-spring-graalvm")
                .memorySize(2048)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/alternatives/unicorn-store-spring-graalvm/lambda-spring-graalvm.zip"))
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
