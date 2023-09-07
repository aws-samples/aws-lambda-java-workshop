## go to tmp directory
cd /tmp

## Ensure AWS CLI v2 is installed
sudo rm `which aws`
sudo rm `which aws_completer`
sudo rm -Rf /usr/local/aws-cli/
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

## Resize disk
/home/ubuntu/environment/aws-lambda-java-workshop/resize-cloud9.sh 30

## Install additional dependencies
sudo apt update
# Suppress the information popup about the kernel
sudo sed -i "s/#\$nrconf{kernelhints} = -1;/\$nrconf{kernelhints} = -1;/g" /etc/needrestart/needrestart.conf
sudo apt install -y jq
npm install -g aws-cdk --force
npm install -g artillery

## Set JDK 17 as default
wget -O- https://apt.corretto.aws/corretto.key | sudo apt-key add - 
sudo add-apt-repository 'deb https://apt.corretto.aws stable main' -y
sudo apt-get install -y java-17-amazon-corretto-jdk

sudo update-alternatives --set java /usr/lib/jvm/java-17-amazon-corretto/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-17-amazon-corretto/bin/javac
export JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto

## Pre-Download Maven dependencies for Unicorn Store
cd ~/environment/aws-lambda-java-workshop/labs/unicorn-store
./mvnw dependency:go-offline -f infrastructure/db-setup/pom.xml
./mvnw dependency:go-offline -f software/alternatives/unicorn-store-basic/pom.xml
./mvnw dependency:go-offline -f software/unicorn-store-spring/pom.xml
./mvnw dependency:go-offline -f software/alternatives/unicorn-store-micronaut/pom.xml

## temporary hide warnings for deprecated Node version (requires updated Cloud9 AMI to be fixed)
export JSII_SILENCE_WARNING_DEPRECATED_NODE_VERSION=1
