#bin/sh

rm store-spring-graal
rm lambda-spring-native.zip
#Build the native image via Docker
docker build . -t graalvm-lambda-builder --progress=plain

#Extract the resulting native image
docker run --rm --entrypoint cat graalvm-lambda-builder target/store-spring-final > store-spring-graal

zip lambda-spring-native store-spring-graal bootstrap

