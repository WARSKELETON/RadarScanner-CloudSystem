# RadarScanner@Cloud System
_Simulated radar scanning service hosted in an elastic cluster of web servers._

## WebServer Node - AWS Configuration

### EC2 Instance

1. Amazon Linux 2 AMI (Amazon Machine Image)
2. t2.micro (Instance Type)
3. Select subnet and enable CloudWatch monitoring
4. 8 GB storage
5. Security Group
    * HTTP port 80
    * Port 8000
    * SSH Port 22
6. Use our existing key pair .pem file

### Inside the machine

1. Check and install all updates
2. Install Java 7 SDK
3. Copy project directory to the instance's home directory
4. Define required CLASSPATH and disable java stackmap utilization
5. Edit rc.local for boot initialization of the WebServer

When the steps above are completed create an image.

## Load Balancer - AWS Configuration

1. Classic Load Balancer
2. Enable VPC configuration with existing security group, port 80 open
3. HTTP 80 -> HTTP 8000
4. Basic Health Check with ping path /healthcheck,
    * 30 seconds interval
    * 60 seconds period
    * 5 Unhealthy threshold
    * 10 Healthy threshold

## Launch Configuration - AWS Configuration

1. Give name -> cnv-launchconfig
2. WebServer AMI and instance type (t2.micro)
3. Existing security group (CNV-SSH-HTTP)
4. Existing key pair

## Auto Scaling group - AWS Configuration

1. Give name -> cnv-autoscalinggroup
2. Use previously create launch config
3. Select existing VPC and subnet
4. Attach to the previously created load balancer
5. ELB Health Check with 60 seconds grace period
6. Enable group metrics collection withing CloudWatch
7. Desired capactiy -> 1 | Minimum capactiy -> 1 | Maximum capactiy -> 2
8. No scaling policy

## CloudWatch Alarms - AWS Configuration

1. CPUUtilization for AutoScalingGroup
2. Statistic Average
3. Period 1 minute
4. Threshold Greater than 50%
5. No notifications
6. cpu-greater-than-50 -> alarm name

Repeat the previous steps for lower than 25%

## Scaling Policies - AWS Configuration

For each alarm created previously, associate it with the autoscaling group:

* Increase group size by 1 whenever the above metric surpasses 50%
* Decrease group size by 1 whenever the above metric falls bellow 25%

----
## Authors

**Group 15**

### Team members

| Number | Name              | User                                 | Email                                       |
| -------|-------------------|--------------------------------------|---------------------------------------------|
| 90704  | Andre Augusto     | <https://github.com/AndreAugusto11>  | <mailto:andre.augusto@tecnico.ulisboa.pt>   |
| 90744  | Lucas Vicente     | <https://github.com/WARSKELETON>     | <mailto:lucasvicente@tecnico.ulisboa.pt>    |
| 90751  | Manuel Mascarenhas     | <https://github.com/zemfoliveira>    | <mailto:jose.f.oliveira@tecnico.ulisboa.pt> |