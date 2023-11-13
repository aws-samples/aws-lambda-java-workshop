#bin/sh

#Remove older versions
rm lambda-spring-graalvm.zip

#Build the native image via Docker
docker build . -t graalvm-lambda-builder --progress=plain

#Extract the resulting native image
docker run --rm --entrypoint cat graalvm-lambda-builder lambda-spring-graalvm.zip > lambda-spring-graalvm.zip



