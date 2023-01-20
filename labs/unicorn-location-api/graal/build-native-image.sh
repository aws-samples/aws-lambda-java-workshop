#bin/sh

#Build the native image via Docker
docker build . -t graalvm-lambda-builder --progress=plain

#Extract the resulting native image
docker run --rm --entrypoint cat graalvm-lambda-builder target/native > UnicornLocationFunctionGraalVM/native

#Provide proper permissions to execute
chmod +x UnicornLocationFunctionGraalVM/native
chmod +x UnicornLocationFunctionGraalVM/bootstrap

#Zip the files
zip UnicornLocationFunctionGraalVM/uncicorn-location-graal.zip UnicornLocationFunctionGraalVM/native UnicornLocationFunctionGraalVM/bootstrap