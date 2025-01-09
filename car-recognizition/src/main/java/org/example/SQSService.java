package org.example;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
// import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class SQSService {
    private static final Logger logger = LoggerFactory.getLogger(SQSService.class);
    private final SqsClient sqsClient;

    public SQSService() {
        sqsClient = SqsClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_EAST_1).build();
    }

    private String getQeueueUrl(String queueName) {
        try {
            GetQueueUrlRequest request = GetQueueUrlRequest.builder().queueName(queueName).build();
            GetQueueUrlResponse response = sqsClient.getQueueUrl(request);
            return response.queueUrl();
        } catch (SqsException e) {
            String code = e.awsErrorDetails().errorCode();
            if (code.equals("AWS.SimpleQueueService.NonExistentQueue")) {
                return createFIFOQueue(queueName);
            } else {
                logger.error("Get queue url error: " + e.awsErrorDetails().errorMessage());
                System.exit(1);
            }
        }
        return "";
    }

    private String createFIFOQueue(String queueName) {
        try {
            Map <QueueAttributeName, String> attrMap = new HashMap<QueueAttributeName, String>();
            attrMap.put(QueueAttributeName.FIFO_QUEUE, "true");

            CreateQueueRequest createQueueRequest = CreateQueueRequest.builder().queueName(queueName).attributes(attrMap).build();

            sqsClient.createQueue(createQueueRequest);

            return getQeueueUrl(queueName);
        } catch (SqsException e) {
            logger.error("Create queue error: "  + e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

    public void send(String queueName, String message) {
        try {
            String uniqueId = UUID.randomUUID().toString();
            SendMessageRequest request = SendMessageRequest.builder()
                    .queueUrl(getQeueueUrl(queueName))
                    .delaySeconds(0)
                    .messageGroupId("1")
                    .messageBody(message)
                    .messageDeduplicationId(uniqueId)
                    .build();
            sqsClient.sendMessage(request);
        } catch (SqsException e) {
            logger.error("Send message error: " + e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
