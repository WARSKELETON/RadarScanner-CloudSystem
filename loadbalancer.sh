#!/usr/bin/env bash

sudo yum -y update
sudo yum -y install git

echo 'export CLASSPATH="$CLASSPATH:/home/ec2-user/cnv-project/"' | sudo tee -a ~/.bashrc
echo 'export _JAVA_OPTIONS="-XX:-UseSplitVerifier"' | sudo tee -a ~/.bashrc
echo 'export CLASSPATH="$CLASSPATH:/home/ec2-user/aws-java-sdk-1.11.1019/lib/aws-java-sdk-1.11.1019.jar:/home/ec2-user/aws-java-sdk-1.11.1019/third-party/lib/*:."' | sudo tee -a ~/.bashrc
source ~/.bashrc
sudo yum -y install java-1.7.0-openjdk-devel.x86_64

wget http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip
unzip aws-java-sdk.zip
