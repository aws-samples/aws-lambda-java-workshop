package com.unicorn;

import java.util.HashMap;

import com.unicorn.alternatives.UnicornAuditService;
import com.unicorn.alternatives.UnicornStoreMicronaut;
import com.unicorn.alternatives.UnicornStoreQuarkus;
import com.unicorn.alternatives.UnicornStoreSpringGraalVM;
import com.unicorn.core.InfrastructureStack;

import io.github.cdklabs.cdknag.AwsSolutionsChecks;
import io.github.cdklabs.cdknag.NagPackProps;
import software.amazon.awscdk.*;

public class UnicornStoreApp {

    public static void main(final String[] args) {
        App app = new App();

        var infrastructureStack = new InfrastructureStack(app, "UnicornStoreInfrastructure", StackProps.builder()
                .build());

        new UnicornStoreStack(app, "UnicornStoreSpringApp", StackProps.builder()
                .build(), infrastructureStack);

        new UnicornStoreMicronaut(app, "UnicornStoreMicronautApp", StackProps.builder()
                .build(), infrastructureStack);

        new UnicornStoreSpringGraalVM(app, "UnicornStoreSpringGraalVMApp", StackProps.builder()
                .build(), infrastructureStack);

        new UnicornStoreQuarkus(app, "UnicornStoreQuarkusApp", StackProps.builder()
                .build(), infrastructureStack);

        new UnicornAuditService(app, "UnicornAuditServiceApp", StackProps.builder()
                .build(), infrastructureStack);

        //Add CDK-NAG checks: https://github.com/cdklabs/cdk-nag
        Validations.of(app).addPlugins(new AwsSolutionsChecks(app,
                NagPackProps.builder().writeSuppressionsToCloudFormation(true).build()));

        //Add suppression to exclude certain findings that are not needed for Workshop environment
        Validations.of(app).acknowledge(
                new Acknowledgment.Builder().id("AwsSolutions-APIG4").reason("The workshop environment does not require API-Gateway authorization").build(),
                new Acknowledgment.Builder().id("AwsSolutions-COG4").reason("The workshop environment does not require Cognito User Pool authorization").build(),
                new Acknowledgment.Builder().id("AwsSolutions-RDS3").reason("Workshop environment does not need a Multi-AZ setup to reduce cost").build(),
                new Acknowledgment.Builder().id("AwsSolutions-RDS10").reason("Workshop environment is ephemeral and the database should be deleted by the end of the workshop").build(),
                new Acknowledgment.Builder().id("AwsSolutions-RDS11").reason("Database is in a private subnet and can use the default port").build(),
                new Acknowledgment.Builder().id("AwsSolutions-APIG2").reason("API Gateway request validation is not needed for workshop").build(),
                new Acknowledgment.Builder().id("AwsSolutions-APIG1").reason("API Gateway access logging not needed for workshop setup").build(),
                new Acknowledgment.Builder().id("AwsSolutions-APIG6").reason("API Gateway access logging not needed for workshop setup").build(),
                new Acknowledgment.Builder().id("AwsSolutions-VPC7").reason("Workshop environment does not need VPC flow logs").build(),
                new Acknowledgment.Builder().id("AwsSolutions-SMG4").reason("Ephemeral workshop environment does not need to rotate secrets").build(),
                new Acknowledgment.Builder().id("AwsSolutions-RDS2").reason("Workshop non-sensitive test database does not need encryption at rest").build(),
                new Acknowledgment.Builder().id("AwsSolutions-APIG3").reason("Workshop API Gateways do not need AWS WAF assigned").build(),
                new Acknowledgment.Builder().id("AwsSolutions-EC23").reason("Not needed").build(),
                new Acknowledgment.Builder().id("AwsSolutions-RDS13").reason("Workshop Database does not need backups").build(),
                new Acknowledgment.Builder().id("CdkNagValidationFailure").reason("Suppress warnings see: https://github.com/cdklabs/cdk-nag/issues/817").build()
        );

        // Suppress parameterized IAM findings directly via metadata
        // (Validations.acknowledge() rejects IDs containing '::' due to CDK delimiter validation bug)
        var iamSuppressions = new HashMap<String, String>();
        iamSuppressions.put("AwsSolutions-IAM4", "AWS Managed policies are acceptable for the workshop");
        iamSuppressions.put("AwsSolutions-IAM5", "A wildcard is acceptable for this workshop to allow parallel creation of resources");
        iamSuppressions.put("AwsSolutions-IAM4[Policy::arn:<AWS::Partition>:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole]", "AWS Managed policies are acceptable for the workshop");
        iamSuppressions.put("AwsSolutions-IAM4[Policy::arn:<AWS::Partition>:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole]", "AWS Managed policies are acceptable for the workshop");
        iamSuppressions.put("AwsSolutions-IAM4[Policy::arn:<AWS::Partition>:iam::aws:policy/service-role/AmazonAPIGatewayPushToCloudWatchLogs]", "AWS Managed policies are acceptable for the workshop");
        iamSuppressions.put("AwsSolutions-IAM5[Resource::arn:aws:secretsmanager:*:*:secret:unicornstore-db-secret-*]", "A wildcard is acceptable for this workshop");
        app.getNode().addMetadata(Validations.ACKNOWLEDGED_RULES_METADATA_KEY, iamSuppressions);

        app.synth();
    }
}
