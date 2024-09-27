FROM public.ecr.aws/amazonlinux/amazonlinux:2023

#Install dependencies
RUN yum -y update \
    && yum install -y unzip zip tar gzip bzip2-devel ed gcc gcc-c++ gcc-gfortran \
    less libcurl-devel openssl openssl-devel readline-devel xz-devel \
    zlib-devel glibc-static zlib-static \
    && rm -rf /var/cache/yum

# Graal VM
ENV GRAAL_VERSION 21.0.2
ENV GRAAL_FILENAME graalvm-community-jdk-${GRAAL_VERSION}_linux-x64_bin.tar.gz
RUN curl -4 -L https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${GRAAL_VERSION}/${GRAAL_FILENAME} | tar -xvz
RUN mv graalvm-community-openjdk-${GRAAL_VERSION}* /usr/lib/graalvm

# Maven
ENV MVN_VERSION 3.9.9
ENV MVN_FOLDERNAME apache-maven-${MVN_VERSION}
ENV MVN_FILENAME apache-maven-${MVN_VERSION}-bin.tar.gz
RUN curl -4 -L https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/${MVN_FILENAME} | tar -xvz
RUN mv $MVN_FOLDERNAME /usr/lib/maven
RUN ln -s /usr/lib/maven/bin/mvn /usr/bin/mvn

ENV JAVA_HOME /usr/lib/graalvm

#Build Native Image
WORKDIR project
COPY UnicornLocationFunction .
COPY graal/bootstrap bootstrap

RUN mvn install -P native
RUN cp target/native native
RUN cp target/libaws-crt-jni.so libaws-crt-jni.so

RUN chmod +x bootstrap
RUN chmod +x native

RUN zip unicorn-location-graal.zip bootstrap native libaws-crt-jni.so