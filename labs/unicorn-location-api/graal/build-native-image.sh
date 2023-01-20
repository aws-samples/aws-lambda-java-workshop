#bin/sh

#Build the native image via Docker
docker build . -t graalvm-lambda-builder --progress=plain

#Extract the resulting zip file with the native image and starting instructions
docker run --rm --entrypoint cat graalvm-lambda-builder unicorn-location-graal.zip > UnicornLocationFunctionGraalVM/unicorn-location-graal.zip
