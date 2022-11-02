#bin/sh

#Remove older versions
rm unicorn-store-spring-native
rm lambda-spring-native.zip

#Build the native image via Docker
docker build . -t graalvm-lambda-builder --progress=plain

#Extract the resulting native image
docker run --rm --entrypoint cat graalvm-lambda-builder lambda-spring-native.zip > lambda-spring-native.zip



