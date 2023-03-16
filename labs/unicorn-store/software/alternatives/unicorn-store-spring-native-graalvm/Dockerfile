FROM public.ecr.aws/amazonlinux/amazonlinux:2

#Install depencencies
RUN yum -y update \
    && yum install -y unzip zip tar gzip bzip2-devel ed gcc gcc-c++ gcc-gfortran \
    less libcurl-devel openssl openssl-devel readline-devel xz-devel \
    zlib-devel glibc-static libcxx libcxx-devel llvm-toolset-7 zlib-static \
    && rm -rf /var/cache/yum

# Graal VM
ENV JAVA_VERSION java11
ENV GRAAL_VERSION 22.2.0
ENV GRAAL_FOLDERNAME graalvm-ce-${JAVA_VERSION}-${GRAAL_VERSION}
ENV GRAAL_FILENAME graalvm-ce-${JAVA_VERSION}-linux-amd64-${GRAAL_VERSION}.tar.gz
RUN curl -4 -L https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-${GRAAL_VERSION}/${GRAAL_FILENAME} | tar -xvz
RUN mv $GRAAL_FOLDERNAME /usr/lib/graalvm
RUN rm -rf $GRAAL_FOLDERNAME

# Maven
ENV MVN_VERSION 3.8.8
ENV MVN_FOLDERNAME apache-maven-${MVN_VERSION}
ENV MVN_FILENAME apache-maven-${MVN_VERSION}-bin.tar.gz
RUN curl -4 -L https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/${MVN_FILENAME} | tar -xvz
RUN mv $MVN_FOLDERNAME /usr/lib/maven
RUN rm -rf $MVN_FOLDERNAME

#Native Image dependencies
RUN /usr/lib/graalvm/bin/gu install native-image
RUN ln -s /usr/lib/graalvm/bin/native-image /usr/bin/native-image
RUN ln -s /usr/lib/maven/bin/mvn /usr/bin/mvn

ENV JAVA_HOME /usr/lib/graalvm

#Copy pom.xml to image & cache Maven dependencies
WORKDIR project
COPY pom.xml pom.xml
RUN mvn verify clean --fail-never -B

#Copy project and build native image
COPY src src
RUN mvn -Pnative -DskipTests package

#Copy bootstrap file (needed for AWS Lambda custom runtime) to image, set file permissions and bundle as zip
WORKDIR target
COPY bootstrap bootstrap
RUN chmod +x unicorn-store-spring-native
RUN chmod +x bootstrap
RUN zip lambda-spring-native unicorn-store-spring-native bootstrap


