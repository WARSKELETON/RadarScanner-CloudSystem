#!/usr/bin/env bash

sudo yum -y update
sudo yum -y install git

echo 'export CLASSPATH="$CLASSPATH:/home/ec2-user/cnv-project/"' | sudo tee -a ~/.bashrc
echo 'export _JAVA_OPTIONS="-XX:-UseSplitVerifier"' | sudo tee -a ~/.bashrc
echo 'export CLASSPATH="$CLASSPATH:/home/ec2-user/aws-java-sdk-1.11.1019/lib/aws-java-sdk-1.11.1019.jar:/home/ec2-user/aws-java-sdk-1.11.1019/third-party/lib/*:."' | sudo tee -a ~/.bashrc

source ~/.bashrc

sudo yum -y install java-1.7.0-openjdk-devel.x86_64

echo 'sudo su - ec2-user -c "java pt.ulisboa.tecnico.cnv.server.WebServer -address 0.0.0.0 -port 8000 -maps /home/ec2-user/cnv-project/datasets"' | sudo tee -a /etc/rc.local
sudo chmod +x /etc/rc.d/rc.local

wget http://sdk-for-java.amazonwebservices.com/latest/aws-java-sdk.zip
unzip aws-java-sdk.zip

# git clone https://github.com/WARSKELETON/cnv-project.git
# sudo scp -r -i CNV-lab-AWS.pem ~/.aws/ ec2-user@3.80.163.1:~/

# sudo scp -r -i CNV-lab-AWS.pem workerNode.sh ec2-user@3.80.163.1:~/
