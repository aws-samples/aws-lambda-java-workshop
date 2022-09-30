package com.unicorn.core;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;

public class DatabaseSetupStack extends Stack {

    private final InfrastructureStack infrastructureStack;

    public DatabaseSetupStack(final Construct scope, final String id, final StackProps props,
                              final InfrastructureStack infrastructureStack) {
        super(scope, id, props);
        this.infrastructureStack = infrastructureStack;

        var dbSetupLambdaFunction = createDbSetupLambdaFunction();

        new CfnOutput(this, "DbSetupArn", CfnOutputProps.builder()
                .value(dbSetupLambdaFunction.getFunctionArn())
                .build());
    }

    private Function createDbSetupLambdaFunction() {
        return Function.Builder.create(this, "DBSetupLambdaFunction")
                .runtime(Runtime.JAVA_11)
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../db-setup/target/db-setup.jar"))
                .handler("com.amazon.aws.DBSetupHandler::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .environment(new HashMap<>() {{
                    put("DB_PASSWORD", infrastructureStack.getDatabaseSecretString());
                    put("DB_CONNECTION_URL", infrastructureStack.getDatabaseJDBCConnectionString());
                    put("DB_USER", "postgres");
                }})
                .build();
    }

}
