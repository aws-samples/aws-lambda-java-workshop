package com.unicorn.alternatives;

import com.unicorn.core.InfrastructureStack;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.apigateway.LambdaRestApi;
import software.amazon.awscdk.services.apigateway.RestApi;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.events.EventPattern;
import software.amazon.awscdk.services.events.Rule;
import software.amazon.awscdk.services.events.targets.LambdaFunction;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;

public class UnicornAuditService extends Stack {

    private final InfrastructureStack infrastructureStack;

    public UnicornAuditService(final Construct scope, final String id, final StackProps props, final InfrastructureStack infrastructureStack) {
        super(scope, id, props);
        this.infrastructureStack = infrastructureStack;


        //Micronaut app
        var unicornAuditServiceFunction = createUnicornAuditServiceFunction();
        var auditTable = createDynamoDBTable();

        auditTable.grantWriteData(unicornAuditServiceFunction);

        var rule = Rule.Builder.create(this, "EventBridgeRuleAudit")
                .ruleName("my-rule")
                .eventBus(infrastructureStack.getEventBridge())
                .eventPattern(EventPattern.builder().source(List.of("com.unicorn.store")).build())
                .build();

        rule.addTarget(new LambdaFunction(unicornAuditServiceFunction));
    }

    private Table createDynamoDBTable(){
        return Table.Builder.create(this, "UnicornStoreAuditServiceTable")
                .tableName("unicorn-audits")
                .build();
    }

    private Function createUnicornAuditServiceFunction() {
       return Function.Builder.create(this, "UnicornAuditServiceFunction")
                .runtime(Runtime.JAVA_17)
                .functionName("unicorn-audit-service")
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../../software/unicorn-audit-service/target/unicorn-audit-service-2.0.0.jar"))
                .handler("org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest")
                .vpc(infrastructureStack.getVpc())
                .securityGroups(List.of(infrastructureStack.getApplicationSecurityGroup()))
                .build();

    }
}
