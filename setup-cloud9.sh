## Install Maven
sudo wget http://repos.fedorapeople.org/repos/dchen/apache-maven/epel-apache-maven.repo -O /etc/yum.repos.d/epel-apache-maven.repo
sudo sed -i s/\$releasever/6/g /etc/yum.repos.d/epel-apache-maven.repo
sudo yum install -y apache-maven

# Install newer version of AWS SAM CLI
cd /tmp
wget https://github.com/aws/aws-sam-cli/releases/latest/download/aws-sam-cli-linux-x86_64.zip
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

## Set JDK 11 as default
sudo update-alternatives --set java /usr/lib/jvm/java-11-amazon-corretto.x86_64/bin/java
sudo update-alternatives --set javac /usr/lib/jvm/java-11-amazon-corretto.x86_64/bin/javac
export JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto.x86_64