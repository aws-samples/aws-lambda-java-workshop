#bin/sh

TOKEN=$(curl -s -X PUT "http://169.254.169.254/latest/api/token" -H "X-aws-ec2-metadata-token-ttl-seconds: 21600")
INSTANCEID=$(curl -s -H "X-aws-ec2-metadata-token: $TOKEN" http://169.254.169.254/latest/dynamic/instance-identity/document | jq -r '.instanceId')
echo INSTANCEID=$INSTANCEID

aws ec2 describe-iam-instance-profile-associations --query "IamInstanceProfileAssociations[?InstanceId=='$INSTANCEID'][IamInstanceProfile.Arn]" --output text
ASSOCIATION_ID=$(aws ec2 describe-iam-instance-profile-associations --query "IamInstanceProfileAssociations[?InstanceId=='$INSTANCEID'][AssociationId]" --output text)
echo ASSOCIATION_ID=$ASSOCIATION_ID

aws ec2 replace-iam-instance-profile-association --iam-instance-profile Arn=arn:aws:iam::$ACCOUNT_ID:instance-profile/java-on-aws-workshop-user,Name=java-on-aws-workshop-user --association-id $ASSOCIATION_ID

aws ec2 describe-iam-instance-profile-associations --query "IamInstanceProfileAssociations[?InstanceId=='$INSTANCEID'][IamInstanceProfile.Arn]" --output text
