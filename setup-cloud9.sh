## go to tmp directory
cd /tmp

## Ensure AWS CLI v2 is installed
sudo yum -y remove aws-cli
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -q awscliv2.zip
sudo ./aws/install
rm awscliv2.zip

## Install Maven
export MVN_VERSION=3.9.4
export MVN_FOLDERNAME=apache-maven-${MVN_VERSION}
export MVN_FILENAME=apache-maven-${MVN_VERSION}-bin.tar.gz
curl -4 -L https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/${MVN_FILENAME} | tar -xvz
sudo mv $MVN_FOLDERNAME /usr/lib/maven
export M2_HOME=/usr/lib/maven
export PATH=${PATH}:${M2_HOME}/bin
sudo ln -s /usr/lib/maven/bin/mvn /usr/local/bin 

# Install newer version of AWS SAM CLI
wget -q https://github.com/aws/aws-sam-cli/releases/latest/download/aws-sam-cli-linux-x86_64.zip
unzip -q aws-sam-cli-linux-x86_64.zip -d sam-installation
sudo ./sam-installation/install --update
rm -rf ./sam-installation/
rm ./aws-sam-cli-linux-x86_64.zip

## Install additional dependencies
sudo yum install -y jq
npm install -g aws-cdk --force
npm install -g artillery

## Resize disk
/home/ec2-user/environment/aws-lambda-java-workshop/resize-cloud9.sh 30

## Set JDK 17 as default
sudo yum install java-17-amazon-corretto-headless
sudo yum -y install java-17-amazon-corretto-devel
sudo update-alternatives --set java /usr/lib/jvm/java-17-amazon-corretto.x86_64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-17-amazon-corretto.x86_64/bin/javac
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64


## Pre-Download Maven dependencies for Unicorn Store
cd ~/environment/aws-lambda-java-workshop/labs/unicorn-store
./mvnw dependency:go-offline -f infrastructure/db-setup/pom.xml
./mvnw dependency:go-offline -f software/alternatives/unicorn-store-basic/pom.xml
./mvnw dependency:go-offline -f software/unicorn-store-spring/pom.xml
./mvnw dependency:go-offline -f software/alternatives/unicorn-store-micronaut/pom.xml

## temporary hide warnings for deprecated Node version (requires updated Cloud9 AMI to be fixed)
export JSII_SILENCE_WARNING_DEPRECATED_NODE_VERSION=1