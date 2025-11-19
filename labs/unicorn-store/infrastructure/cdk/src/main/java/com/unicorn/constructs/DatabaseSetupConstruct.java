package com.unicorn.constructs;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.CfnOutput;
import software.amazon.awscdk.CfnOutputProps;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.services.ec2.IInterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpoint;
import software.amazon.awscdk.services.ec2.InterfaceVpcEndpointAwsService;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;

public class DatabaseSetupConstruct extends Construct{

    private final InfrastructureStack infrastructureStack;

    public DatabaseSetupConstruct(final Construct scope, final String id) {
        super(scope, id);

        this.infrastructureStack = (InfrastructureStack) scope;

        var dbSetupLambdaFunction = createDbSetupLambdaFunction();
        dbSetupLambdaFunction.addToRolePolicy(PolicyStatement
                .Builder
                .create()
                .resources(List.of("arn:aws:secretsmanager:*:*:secret:unicornstore-db-secret-*"))
                .actions(List.of("secretsmanager:GetSecretValue"))
                .build());
        createSecretsManagerVpcEndpoint();

        new CfnOutput(scope, "DbSetupArn", CfnOutputProps.builder()
                .value(dbSetupLambdaFunction.getFunctionArn())
                .build());
    }

    private Function createDbSetupLambdaFunction() {
        return Function.Builder.create(this, "DBSetupLambdaFunction")
                .runtime(Runtime.JAVA_25)
                .memorySize(1024)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../db-setup/target/db-setup.jar"))
                .handler("com.amazon.aws.DBSetupHandler::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .build();
    }

    private IInterfaceVpcEndpoint createSecretsManagerVpcEndpoint() {
        return InterfaceVpcEndpoint.Builder.create(this, "SecretsManagerEndpoint")
                .service(InterfaceVpcEndpointAwsService.SECRETS_MANAGER)
                .vpc(infrastructureStack.getVpc())
                .build();
    }
}
