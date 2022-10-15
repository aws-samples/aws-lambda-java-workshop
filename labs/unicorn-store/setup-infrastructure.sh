#bin/sh

# Build the database setup function
./mvnw clean package -f infrastructure/db-setup/pom.xml

# Build the unicorn application
./mvnw clean package -f software/alternatives/unicorn-store-basic/pom.xml
./mvnw clean package -f software/unicorn-store-spring/pom.xml
./mvnw clean package -f software/alternatives/unicorn-store-micronaut/pom.xml
./mvnw clean package -f software/alternatives/unicorn-store-quarkus/pom.xml

# Deploy the infrastructure
cd infrastructure/cdk

cdk bootstrap
cdk deploy UnicornStoreInfrastructure --require-approval never --outputs-file target/output.json

# Execute the DB Setup function to create the table
aws lambda invoke --function-name $(cat target/output.json | jq -r '.UnicornStoreInfrastructure.DbSetupArn') /dev/stdout | cat;
