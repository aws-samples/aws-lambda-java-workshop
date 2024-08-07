#bin/sh

## go to tmp directory
cd /tmp

sudo yum update
sudo yum install -y npm

## Ensure AWS CLI v2 is installed
sudo yum -y remove aws-cli
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip -o awscliv2.zip
sudo ./aws/install
rm awscliv2.zip
aws --version

## Install Maven
MVN_VERSION=3.9.6
MVN_FOLDERNAME=apache-maven-${MVN_VERSION}
MVN_FILENAME=apache-maven-${MVN_VERSION}-bin.tar.gz
curl -4 -L https://archive.apache.org/dist/maven/maven-3/${MVN_VERSION}/binaries/${MVN_FILENAME} | tar -xvz
sudo mv $MVN_FOLDERNAME /usr/lib/maven
export M2_HOME=/usr/lib/maven
export PATH=${PATH}:${M2_HOME}/bin
sudo ln -s /usr/lib/maven/bin/mvn /usr/local/bin
/usr/lib/maven/bin/mvn --version

# Install newer version of AWS SAM CLI
wget -q https://github.com/aws/aws-sam-cli/releases/latest/download/aws-sam-cli-linux-x86_64.zip
unzip -q aws-sam-cli-linux-x86_64.zip -d sam-installation
sudo ./sam-installation/install --update
rm -rf ./sam-installation/
rm ./aws-sam-cli-linux-x86_64.zip
/usr/local/bin/sam --version

## Install additional dependencies
sudo npm install -g aws-cdk --force
cdk version
sudo npm install -g artillery

wget https://github.com/mikefarah/yq/releases/download/v4.43.1/yq_linux_amd64.tar.gz -O - |\
  tar xz && sudo mv yq_linux_amd64 /usr/bin/yq
yq --version

## Set JDK 17 as default
sudo yum -y install java-17-amazon-corretto-devel
sudo update-alternatives --set java /usr/lib/jvm/java-17-amazon-corretto.x86_64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-17-amazon-corretto.x86_64/bin/javac
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto.x86_64
echo "export JAVA_HOME=${JAVA_HOME}" | tee -a ~/.bash_profile
echo "export JAVA_HOME=${JAVA_HOME}" | tee -a ~/.bashrc
java -version

TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
export AWS_REGION=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/dynamic/instance-identity/document | jq -r '.region')
export ACCOUNT_ID=$(aws sts get-caller-identity --output text --query Account --region $AWS_REGION)
echo "export ACCOUNT_ID=${ACCOUNT_ID}" | tee -a ~/.bash_profile
echo "export ACCOUNT_ID=${ACCOUNT_ID}" | tee -a ~/.bashrc
echo "export CDK_DEFAULT_ACCOUNT=${ACCOUNT_ID}" | tee -a ~/.bash_profile
echo "export CDK_DEFAULT_ACCOUNT=${ACCOUNT_ID}" | tee -a ~/.bashrc
echo "export AWS_REGION=${AWS_REGION}" | tee -a ~/.bash_profile
echo "export AWS_REGION=${AWS_REGION}" | tee -a ~/.bashrc
echo "export AWS_DEFAULT_REGION=${AWS_REGION}" | tee -a ~/.bash_profile
echo "export AWS_DEFAULT_REGION=${AWS_REGION}" | tee -a ~/.bashrc
aws configure set default.region ${AWS_REGION}
aws configure get default.region
test -n "$AWS_REGION" && echo AWS_REGION is "$AWS_REGION" || echo AWS_REGION is not set

## Pre-Download Maven dependencies for Unicorn Store
cd ~/environment/aws-lambda-java-workshop/labs/unicorn-store
./mvnw dependency:go-offline -f infrastructure/db-setup/pom.xml
./mvnw dependency:go-offline -f software/unicorn-store-spring/pom.xml
./mvnw dependency:go-offline -f software/alternatives/unicorn-store-micronaut/pom.xml
./mvnw dependency:go-offline -f software/alternatives/unicorn-store-quarkus/pom.xml

##  Download & install Session Manager plugin
curl "https://s3.amazonaws.com/session-manager-downloads/plugin/latest/linux_64bit/session-manager-plugin.rpm" -o "session-manager-plugin.rpm"
sudo yum install -y session-manager-plugin.rpm
## Test Session Manager plugin Installation
session-manager-plugin
rm session-manager-plugin.rpm
