#bin/sh

UNICORN_VPC_ID=$(aws cloudformation describe-stacks --stack-name UnicornStoreInfrastructure --query 'Stacks[0].Outputs[?OutputKey==`UnicornStoreVpcId`].OutputValue' --output text)

VPC_PEERING_CONNECTION_ID=$(aws ec2 describe-vpc-peering-connections --filters "Name=accepter-vpc-info.vpc-id,Values=$UNICORN_VPC_ID" --query 'VpcPeeringConnections[0].VpcPeeringConnectionId' --output text)

aws ec2 delete-vpc-peering-connection --vpc-peering-connection-id $VPC_PEERING_CONNECTION_ID
