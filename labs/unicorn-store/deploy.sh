#bin/sh

app=$1
build=$2

if [ $app == "spring" ]
then
  if [ $build == "--build" ]
  then
    ./mvnw clean package -f software/unicorn-store-spring/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringApp --outputs-file target/output.json --require-approval never
  exit 0
fi

if [ $app == "micronaut" ]
then
  if [ $build == "--build" ]
  then
    ./mvnw clean package -f software/alternatives/unicorn-store-micronaut/pom.xml
  fi
  cd infrastructure/cdk
  cdk deploy UnicornStoreSpringApp --outputs-file target/output.json --require-approval never
  exit 0
fi

if [ $app == "quarkus" ]
then
  ./mvnw clean package -f software/alternatives/unicorn-store-quarkus/pom.xml
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

