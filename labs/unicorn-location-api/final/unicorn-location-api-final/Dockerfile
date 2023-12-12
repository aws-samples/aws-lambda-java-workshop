FROM public.ecr.aws/amazonlinux/amazonlinux:2023

#Install depencencies
RUN yum -y update \
    && yum install -y unzip zip tar gzip bzip2-devel ed gcc gcc-c++ gcc-gfortran \
    less libcurl-devel openssl openssl-devel readline-devel xz-devel \
    zlib-devel glibc-static zlib-static \
    && rm -rf /var/cache/yum

# Graal VM
ENV JAVA_VERSION 17.0.9
ENV GRAAL_FOLDERNAME graalvm-community-openjdk-${JAVA_VERSION}+9.1
ENV GRAAL_FILENAME graalvm-community-jdk-${JAVA_VERSION}_linux-x64_bin.tar.gz
RUN curl -4 -L https://github.com/graalvm/graalvm-ce-builds/releases/download/jdk-${JAVA_VERSION}/${GRAAL_FILENAME} | tar -xvz
RUN mv $GRAAL_FOLDERNAME /usr/lib/graalvm

# Maven
ENV MVN_VERSION 3.9.5
ENV MVN_FOLDERNAME apache-maven-${MVN_VERSION}
ENV MVN_FILENAME apache-maven-${MVN_VERSION}-bin.tar.gz
RUN curl -4 -L https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/${MVN_FILENAME} | tar -xvz
RUN mv $MVN_FOLDERNAME /usr/lib/maven

#Native Image dependencies
RUN /usr/lib/graalvm/bin/gu install native-image
RUN ln -s /usr/lib/graalvm/bin/native-image /usr/bin/native-image
RUN ln -s /usr/lib/maven/bin/mvn /usr/bin/mvn

ENV JAVA_HOME /usr/lib/graalvm

#Build Native Image
WORKDIR project
COPY UnicornLocationFunction .
COPY graal/bootstrap bootstrap

RUN mvn install -P native
RUN cp target/native native

RUN chmod +x bootstrap
RUN chmod +x native

RUN zip unicorn-location-graal.zip bootstrap native