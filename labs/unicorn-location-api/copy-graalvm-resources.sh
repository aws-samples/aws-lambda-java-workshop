#bin/sh

#Copy the native image files for reflection configs
cp -r graal/resources UnicornLocationFunction/src/main

#Create a new folder for build results and copy needed components
mkdir UnicornLocationFunctionGraalVM

#Copy the Dockerfile & build script for building the native image
cp graal/Dockerfile .
cp graal/build-native-image.sh .