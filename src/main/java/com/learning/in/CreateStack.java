package com.learning.in;


import com.learning.in.utils.Constants;
import software.amazon.awscdk.core.Construct;
import software.amazon.awscdk.core.Duration;
import software.amazon.awscdk.core.Stack;
import software.amazon.awscdk.core.StackProps;
import software.amazon.awscdk.services.ec2.IVpc;
import software.amazon.awscdk.services.ec2.SecurityGroup;
import software.amazon.awscdk.services.ec2.SubnetSelection;
import software.amazon.awscdk.services.iam.PolicyStatement;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.sqs.DeadLetterQueue;
import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.sqs.QueueEncryption;

import java.util.Collections;


public class CreateStack extends Stack {
    private final MainStackBuild.Args args;
//    private SecurityGroup sg;
//    private IVpc vpc;
//    private SubnetSelection subnetSelection;
    public CreateStack(final Construct parent, final String id, final StackProps props, final MainStackBuild.Args args) {
        super(parent, id, props);
        this.args = args;
        // create kms key
//        final Key stackKey = Key.Builder.create(this, args.getPrefixedName("key"))
//                .enableKeyRotation(true)
//                .alias(args.getPrefixedName("alias/key"))
//                .policy(getPolicyDocument())
//                .build();
        // create a sqs queue
        final String bucketName = args.getPrefixedName("bucket");
        final  Bucket bucket = Bucket.Builder.create(this,bucketName)
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .build();
        //Create DLQ
        final Queue dlq = Queue.Builder.create(this, args.getPrefixedName("dlq.fifo"))
                .queueName(args.getPrefixedName("dlq.fifo"))
                .fifo(true)
                .encryption(QueueEncryption.KMS_MANAGED)
                .visibilityTimeout(Duration.minutes(6))
                .build();
        //create  a sqs fifo queue
        final String queueName = args.getPrefixedName("queue.fifo");
        final Queue queue = Queue.Builder.create(this, queueName)
                .queueName(queueName)
                .retentionPeriod(Duration.days(7))
                .fifo(true)
                .deadLetterQueue(DeadLetterQueue.builder()
                        .maxReceiveCount(3)
                        .queue(dlq)
                        .build())
                .encryption(QueueEncryption.KMS_MANAGED)
                .visibilityTimeout(Duration.minutes(6))
                .build();
        queue.addToResourcePolicy(getQueueResourcePolicy());

}
// ps for queue
    public PolicyStatement getQueueResourcePolicy() {

        final PolicyStatement policyStatement = new PolicyStatement();
        policyStatement.addActions("sqs:SendMessage");
        policyStatement.addAnyPrincipal();
        policyStatement.addCondition(
                "StringLike", Collections.singletonMap("aws:PrincipalArn", "arn:aws:lambda:" + args.getRegion() + ":" + args.getAccountId() + ":function:"
                        + args.getProgram()
                        + "-"
                        + Constants.PROJECT_NAME
                        + "-"
                        + "*"));
        return policyStatement;
    }


}
