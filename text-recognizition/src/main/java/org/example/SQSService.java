package org.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SqsException;

public class SQSService {
    private static final Logger logger = LoggerFactory.getLogger(SQSService.class);
    private final SqsClient sqsClient;

    public SQSService() {
        sqsClient = SqsClient.builder()
                .region(Region.US_EAST_1).build();
    }

    private String getQueueUrl(String queueName) {
        try {
            GetQueueUrlRequest request = GetQueueUrlRequest.builder().queueName(queueName).build();
            GetQueueUrlResponse response = sqsClient.getQueueUrl(request);
            return response.queueUrl();
        } catch (SqsException e) {
            logger.error("Get queue url error: {}", e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

    public List<Message> getMeesages(String queueName) {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    // .queueUrl("https://sqs.us-east-1.amazonaws.com/905418077858/CarImages")
                    .queueUrl(getQueueUrl(queueName))
                    .maxNumberOfMessages(10)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();
            logger.info("No of messages received from queue: {}", messages.size());
            return messages;
        } catch (SqsException e) {
            logger.error("Get queue message error: {}", e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return null;
    }

    public void deleteMessage(String queueName, Message message) {
        try {
            DeleteMessageRequest request = DeleteMessageRequest.builder()
                    .queueUrl(getQueueUrl(queueName))
                    .receiptHandle(message.receiptHandle())
                    .build();

            sqsClient.deleteMessage(request);
            logger.info("Message deleted from queue: {}", message.body());
        } catch (SqsException e) {
            logger.error("Delete queue message error: {}", e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
    }
}
