package com.unicorn.core;

import com.unicorn.constructs.DatabaseSetupConstruct;
import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.events.EventBus;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;
import software.amazon.awscdk.Aspects;
import software.amazon.awscdk.IAspect;
import software.constructs.IConstruct;

import java.io.IOException;
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
        createEventBridgeVpcEndpoint();
        createDynamoDBVpcEndpoint();
        new DatabaseSetupConstruct(this, "UnicornDatabaseConstruct");
        
        Aspects.of(this).add(new PreDestroyHook());
    }

    private EventBus createEventBus() {
        return EventBus.Builder.create(this, "UnicornEventBus")
                .eventBusName("unicorns")
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

        databaseSecurityGroup.addIngressRule(
                Peer.ipv4("192.168.0.0/16"),
                Port.tcp(5432),
                "Allow Database Traffic from IDE network");

        return databaseSecurityGroup;
    }

    private DatabaseInstance createRDSPostgresInstance(IVpc vpc, DatabaseSecret databaseSecret) {

        var databaseSecurityGroup = createDatabaseSecurityGroup(vpc);
        var engine = DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder().version(PostgresEngineVersion.VER_16).build());

        return DatabaseInstance.Builder.create(this, "UnicornInstance")
                .engine(engine)
                .vpc(vpc)
                .allowMajorVersionUpgrade(true)
                .backupRetention(Duration.days(0))
                .databaseName("unicorns")
                .instanceIdentifier("UnicornInstance")
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MEDIUM))
                .vpcSubnets(SubnetSelection.builder()
                        .subnetType(SubnetType.PRIVATE_ISOLATED)
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
         IVpc vpc = Vpc.Builder.create(this, "UnicornVpc")
                .vpcName("UnicornVPC")
                .natGateways(0)
                .build();
        new CfnOutput(this, "UnicornStoreVpcId", CfnOutputProps.builder().value(vpc.getVpcId()).build());
        return vpc;
    }

    public EventBus getEventBridge() {
        return eventBridge;
    }

    public IVpc getVpc() {
        return vpc;
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

    private IInterfaceVpcEndpoint createEventBridgeVpcEndpoint() {
        return InterfaceVpcEndpoint.Builder.create(this, "EventBridgeEndpoint")
                .service(InterfaceVpcEndpointAwsService.EVENTBRIDGE)
                .vpc(this.getVpc())
                .build();
    }

    private IGatewayVpcEndpoint createDynamoDBVpcEndpoint() {
        return GatewayVpcEndpoint.Builder.create(this, "DynamoDBVpcEndpoint")
                .service(GatewayVpcEndpointAwsService.DYNAMODB)
                .vpc(this.getVpc())
                .build();
    }

    private void runCustomScript(String scriptPath) {
        try {
            Process process = Runtime.getRuntime().exec(scriptPath);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Error running custom script. Exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error running custom script: " + e.getMessage());
        }
    }
    
    class PreDestroyHook implements IAspect {
        @Override
        public void visit(IConstruct node) {
            if (node instanceof software.amazon.awscdk.Stack) {
                runCustomScript("/home/ec2-user/environment/aws-lambda-java-workshop/labs/unicorn-store/delete-vpc-peering.sh");
            }
        }
    }

}
