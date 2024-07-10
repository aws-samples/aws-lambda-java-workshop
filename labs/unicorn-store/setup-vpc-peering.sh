#bin/sh

ACCOUNT_ID=$(aws sts get-caller-identity --output text --query Account)
TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
AWS_REGION=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/dynamic/instance-identity/document | jq -r '.region')

UNICORN_VPC_ID=$(aws cloudformation describe-stacks --stack-name UnicornStoreInfrastructure --query 'Stacks[0].Outputs[?OutputKey==`UnicornStoreVpcId`].OutputValue' --output text)

IP=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/dynamic/instance-identity/document | jq -r '.privateIp')

# Check if we're on AL2 or AL2023
STR=$(cat /etc/os-release)
SUB2="VERSION_ID=\"2\""
SUB2023="VERSION_ID=\"2023\""
if [[ "$STR" == *"$SUB2"* ]]
    then
        INTERFACE_NAME=$(ip address | grep $IP | awk ' { print $8 } ')
    else
        INTERFACE_NAME=$(ip address | grep $IP | awk ' { print $10 } ')
fi

MAC=$(ip address show dev $INTERFACE_NAME | grep ether | awk ' { print $2 } ')
IDE_VPC_ID=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/meta-data/network/interfaces/macs/$MAC/vpc-id)

VPC_PEERING_ID=$(aws ec2 create-vpc-peering-connection --vpc-id $IDE_VPC_ID \
--peer-vpc-id $UNICORN_VPC_ID \
--query 'VpcPeeringConnection.VpcPeeringConnectionId' --output text)

sleep 5

aws ec2 accept-vpc-peering-connection --vpc-peering-connection-id $VPC_PEERING_ID --output text

IDE_ROUTE_TABLE_ID=$(aws ec2 describe-route-tables \
--filters "Name=vpc-id,Values=$IDE_VPC_ID" "Name=tag:Name,Values=*java-on-aws-lambda-workshop*" \
--query 'RouteTables[0].RouteTableId' --output text)

UNICORN_DB_ROUTE_TABLE_ID_1=$(aws ec2 describe-route-tables \
--filters "Name=vpc-id,Values=$UNICORN_VPC_ID" "Name=tag:Name,Values=UnicornStoreInfrastructure/UnicornVpc/IsolatedSubnet1" \
--query 'RouteTables[0].RouteTableId' --output text)

UNICORN_DB_ROUTE_TABLE_ID_2=$(aws ec2 describe-route-tables \
--filters "Name=vpc-id,Values=$UNICORN_VPC_ID" "Name=tag:Name,Values=UnicornStoreInfrastructure/UnicornVpc/IsolatedSubnet2" \
--query 'RouteTables[0].RouteTableId' --output text)

aws ec2 create-route --route-table-id $IDE_ROUTE_TABLE_ID \
--destination-cidr-block 10.0.0.0/16 --vpc-peering-connection-id $VPC_PEERING_ID

aws ec2 create-route --route-table-id $UNICORN_DB_ROUTE_TABLE_ID_1 \
--destination-cidr-block 10.10.0.0/16 --vpc-peering-connection-id $VPC_PEERING_ID
aws ec2 create-route --route-table-id $UNICORN_DB_ROUTE_TABLE_ID_2 \
--destination-cidr-block 10.10.0.0/16 --vpc-peering-connection-id $VPC_PEERING_ID
