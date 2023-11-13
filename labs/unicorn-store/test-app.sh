#bin/sh

app=$1

if [ $app == "spring" ]
then
  curl --location --request POST $(cat infrastructure/cdk/target/output.json | jq -r '.UnicornStoreSpringApp.ApiEndpointSpring')'/unicorns' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "name": "Something",
    "age": "Older",
    "type": "Animal",
    "size": "Very big"
}' | jq

  exit 0
fi

if [ $app == "micronaut" ]
then
    curl --location --request POST $(cat infrastructure/cdk/target/output-micronaut.json | jq -r '.UnicornStoreMicronautApp.ApiEndpointMicronaut')'/unicorns' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "name": "Something",
    "age": "Older",
    "type": "Animal",
    "size": "Very big"
}' | jq

  exit 0
fi


if [ $app == "quarkus" ]
then
    curl --location --request POST $(cat infrastructure/cdk/target/output-quarkus.json | jq -r '.UnicornStoreQuarkusApp.ApiEndpointQuarkus')'/unicorns' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "name": "Something",
    "age": "Older",
    "type": "Animal",
    "size": "Very big"
}' | jq

  exit 0
fi


if [ $app == "spring-graalvm" ]
then
    curl --location --request POST $(cat infrastructure/cdk/target/output-spring-graalvm.json | jq -r '.UnicornStoreSpringGraalVMApp.ApiEndpointSpringGraalVM')'/unicorns' \
  --header 'Content-Type: application/json' \
  --data-raw '{
    "name": "Something",
    "age": "Older",
    "type": "Animal",
    "size": "Very big"
}' | jq

  exit 0
fi



