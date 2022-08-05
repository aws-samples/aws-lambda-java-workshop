#bin/sh

#Copy the native image files for reflection configs
cp -r graal/resources UnicornLocationFunction/src/main

#Create a new folder for build results and copy needed components
mkdir UnicornLocationFunctionGraalVM
cp graal/Makefile UnicornLocationFunctionGraalVM
cp graal/bootstrap UnicornLocationFunctionGraalVM

#Dockerfile for building the native image
cp graal/Dockerfile .
cp graal/build-native-image.sh .