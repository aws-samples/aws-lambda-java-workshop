#bin/sh

app=$1
build=$2

if [ $app == "spring" ]
then
  if [[ $build == "--build" ]]
  then
    ./mvnw clean package -f software/unicorn-store-spring/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringApp --outputs-file target/output.json --require-approval never
  exit 0
fi

if [ $app == "micronaut" ]
then
  if [[ $build == "--build" ]]
  then
    ./mvnw clean package -f software/alternatives/unicorn-store-micronaut/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornStoreMicronautApp --outputs-file target/output-micronaut.json --require-approval never
  exit 0
fi

if [ $app == "quarkus" ]
then
  if [[ $build == "--build" ]]
  then
    ./mvnw clean package -f software/alternatives/unicorn-store-quarkus/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornStoreQuarkusApp --outputs-file target/output-quarkus.json --require-approval never
  exit 0
fi

if [ $app == "spring-graalvm" ]
then
  if [[ $build == "--build" ]]
  then
    ./mvnw clean package -f software/alternatives/unicorn-store-basic/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringGraalVMApp --outputs-file target/output-spring-graalvm.json --require-approval never
  exit 0
fi

if [ $app == "audit-service" ]
then
  if [[ $build == "--build" ]]
  then
    ./mvnw clean package -f software/alternatives/unicorn-audit-service/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornAuditServiceApp --outputs-file target/output-audit-service.json --require-approval never
  exit 0
fi

