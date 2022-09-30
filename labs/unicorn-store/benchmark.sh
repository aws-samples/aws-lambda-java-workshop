#bin/sh

app=$1

if [ $app == "basic" ]
then
  artillery run -t $(cat infrastructure/cdk/target/output.json | jq -r '.UnicornStoreSpringApp.ApiEndpointBasic') -v '{ "url": "/unicorns" }' infrastructure/loadtest.yaml
  exit 0
fi

if [ $app == "spring" ]
then
  artillery run -t $(cat infrastructure/cdk/target/output.json | jq -r '.UnicornStoreSpringApp.ApiEndpointSpring') -v '{ "url": "/unicorns" }' infrastructure/loadtest.yaml
  exit 0
fi

if [ $app == "micronaut" ]
then
  artillery run -t $(cat infrastructure/cdk/target/output.json | jq -r '.UnicornStoreSpringApp.ApiEndpointMicronaut') -v '{ "url": "/unicorns" }' infrastructure/loadtest.yaml
  exit 0
fi


if [ $app == "spring-native-graalvm" ]
then
  artillery run -t $(cat infrastructure/cdk/target/output.json | jq -r '.UnicornStoreSpringApp.ApiEndpointSpringNative') -v '{ "url": "/unicorns" }' infrastructure/loadtest.yaml
  exit 0
fi


