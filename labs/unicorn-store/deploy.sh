#bin/sh

app=$1

if [ $app == "spring" ]
then
  ./mvnw clean package -f software/unicorn-store-spring/pom.xml
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringApp --outputs-file target/output.json --require-approval never
  exit 0
fi

if [ $app == "micronaut" ]
then
  ./mvnw clean package -f software/alternatives/unicorn-store-micronaut/pom.xml
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringApp --outputs-file target/output.json --require-approval never
  exit 0
fi

if [ $app == "basic" ]
then
  ./mvnw clean package -f software/alternatives/unicorn-store-basic/pom.xml
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringApp --outputs-file target/output.json --require-approval never
  exit 0
fi

