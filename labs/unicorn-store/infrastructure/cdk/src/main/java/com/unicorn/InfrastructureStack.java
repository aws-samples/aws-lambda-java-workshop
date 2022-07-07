package com.unicorn;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.logs.LogRetention;
import software.amazon.awscdk.services.logs.RetentionDays;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.List;

public class InfrastructureStack extends Stack {

    private final DatabaseSecret databaseSecret;
    private final DatabaseInstance database;
    private final EventBus eventBridge;
    private final IVpc vpc;
    private final ISecurityGroup applicationSecurityGroup;


    public InfrastructureStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        vpc = createUnicornVpc();
        databaseSecret = createDatabaseSecret();
        database = createRDSPostgresInstance(vpc, databaseSecret);
        eventBridge = createEventBus();
        applicationSecurityGroup = new SecurityGroup(this, "ApplicationSecurityGroup",
                SecurityGroupProps
                        .builder()
                        .securityGroupName("applicationSG")
                        .vpc(vpc)
                        .allowAllOutbound(true)
                        .build());

        var dbSetupLambdaFunction = createDbSetupLambdaFunction();

        new CfnOutput(this, "DbSetupArn", CfnOutputProps.builder()
                .value(dbSetupLambdaFunction.getFunctionArn())
                .build());
    }


    private EventBus createEventBus() {
        return EventBus.Builder.create(this, "UnicornEventBus")
                .eventBusName("unicorns")
                .build();
    }

    private Function createDbSetupLambdaFunction() {
        return Function.Builder.create(this, "DBSetupLambdaFunction")
                .runtime(Runtime.JAVA_11)
                .memorySize(512)
                .timeout(Duration.seconds(29))
                .code(Code.fromAsset("../db-setup/target/db-setup.jar"))
                .handler("com.amazon.aws.DBSetupHandler::handleRequest")
                .vpc(vpc)
                .securityGroups(List.of(applicationSecurityGroup))
                .environment(new HashMap<>() {{
                    put("DB_PASSWORD", databaseSecret.secretValueFromJson("password").toString());
                    put("DB_CONNECTION_URL", "jdbc:postgresql://" + database.getDbInstanceEndpointAddress() + ":5432/unicorns");
                    put("DB_USER", "postgres");
                }})
                .build();
    }

    private SecurityGroup createDatabaseSecurityGroup(IVpc vpc) {
        var databaseSecurityGroup = SecurityGroup.Builder.create(this, "DatabaseSG")
                .securityGroupName("DatabaseSG")
                .allowAllOutbound(false)
                .vpc(vpc)
                .build();

        databaseSecurityGroup.addIngressRule(
                Peer.ipv4("10.0.0.0/16"),
                Port.tcp(5432),
                "Allow Database Traffic from local network");

        return databaseSecurityGroup;
    }

    private DatabaseInstance createRDSPostgresInstance(IVpc vpc, DatabaseSecret databaseSecret) {

        var databaseSecurityGroup = createDatabaseSecurityGroup(vpc);
        var engine = DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder().version(PostgresEngineVersion.VER_13_4).build());

        return DatabaseInstance.Builder.create(this, "UnicornInstance")
                .engine(engine)
                .vpc(vpc)
                .allowMajorVersionUpgrade(true)
                .databaseName("unicorns")
                .instanceIdentifier("UnicornInstance")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_WITH_NAT)
                        .build())
                .securityGroups(List.of(databaseSecurityGroup))
                .credentials(Credentials.fromSecret(databaseSecret))
                .build();
    }

    private DatabaseSecret createDatabaseSecret() {
        return DatabaseSecret.Builder
                .create(this, "postgres")
                .secretName("unicornstore-db-secret")
                .username("postgres").build();
    }

    private IVpc createUnicornVpc() {
        return Vpc.Builder.create(this, "UnicornVpc")
                .vpcName("UnicornVPC")
                .build();
    }

    public DatabaseSecret getDatabaseSecret() {
        return databaseSecret;
    }

    public EventBus getEventBridge() {
        return eventBridge;
    }

    public IVpc getVpc() {
        return vpc;
    }

    public DatabaseInstance getDatabase() {
        return database;
    }

    public ISecurityGroup getApplicationSecurityGroup() {
        return applicationSecurityGroup;
    }

    public String getDatabaseSecretString(){
        return databaseSecret.secretValueFromJson("password").toString();
    }

    public String getDatabaseJDBCConnectionString(){
        return "jdbc:postgresql://" + database.getDbInstanceEndpointAddress() + ":5432/unicorns";
    }


}
