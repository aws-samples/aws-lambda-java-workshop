package com.unicorn.alternatives;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.Duration;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpoint;
import software.amazon.awscdk.services.ec2.GatewayVpcEndpointAwsService;
import software.amazon.awscdk.services.ec2.IGatewayVpcEndpoint;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.constructs.Construct;

import java.util.List;

public class UnicornAuditService extends Stack {

    public UnicornAuditService(final Construct scope, final String id, final StackProps props, final InfrastructureStack infrastructureStack) {
        super(scope, id, props);

        //** Your code will go here **//
        var auditTable = Table.Builder.create(this, "UnicornStoreAuditServiceTable")
                .tableName("unicorn-audit")
                .partitionKey(Attribute.builder().name("id").type(AttributeType.STRING).build())
                .build();

        //** Add your code for the AWS Lambda function creation below **/
        var unicornAuditServiceFunction = Function.Builder.create(this, "UnicornAuditServiceFunction")
                .runtime(Runtime.JAVA_17)
                .functionName("unicorn-audit-service")
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/alternatives/unicorn-audit-service/target/unicorn-audit-service-1.0.0-aws.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .build();

        auditTable.grantWriteData(unicornAuditServiceFunction);

        //** Trigger the Lambda Function from the existing Amazon EventBridge EventBus **/
        var rule = Rule.Builder.create(this, "EventBridgeRuleAudit")
                .ruleName("audit-service-rule")
                .eventBus(infrastructureStack.getEventBridge())
                .eventPattern(EventPattern.builder().source(List.of("com.unicorn.store")).build())
                .build();

        rule.addTarget(new LambdaFunction(unicornAuditServiceFunction));
    }

}
