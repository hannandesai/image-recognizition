package org.example;

import java.util.List;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.sqs.model.Message;

public class App {
    private static String BUCKET_NAME = "njit-cs-643";
    private static String QUEUE_NAME = "ImageQueue.fifo";
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static SQSService sqsService = new SQSService();
    private static AWSRekognitionService awsRekognizitionService = new AWSRekognitionService();

    public static void main(String... args) {
        logger.info("Text Recognition starts");

        runConsumer("");

        logger.info("Text Recognition ends");
    }

    private static void runConsumer(String fileContent) {
        // get messages from queue
        List<Message> messages = sqsService.getMeesages(QUEUE_NAME);
        // flag to check if all images has been processed or not.
        Boolean allImagesProcessed = false;

        // iterate over messages for text detection
        for (Message message : messages) {
            if (message.body().equals("-1")) {
                allImagesProcessed = true;
            } else {
                String textDetected = awsRekognizitionService.detectTextFromImage(BUCKET_NAME, message.body());
                if (!textDetected.isEmpty()) {
                    fileContent = fileContent + "Image: " + message.body() + ", Text: " + textDetected + "\n";
                }
            }

            // once message processed delete the message frorm queue
            sqsService.deleteMessage(QUEUE_NAME, message);
        }

        try {
            if (!allImagesProcessed) {
                // wait for two seconds and then again check for messages
                Thread.sleep(2000);
                runConsumer(fileContent);
            } else {
                logger.info("===============All Images Processed=============");
                System.out.println(fileContent);
                // store output to EBS storage
                storeOutput(fileContent);
            }
        } catch (InterruptedException e) {
            logger.error("Interuupt error: {}", e.getMessage());
            System.exit(1);
        }
    }

    private static void storeOutput(String outputText) {
        try {
            File outputFile = new File("/ebs-data/image-recognitition/output.txt");
            if (!outputFile.exists()) {
                outputFile.createNewFile();
            }
            FileWriter myWriter = new FileWriter("/ebs-data/image-recognitition/output.txt");
            myWriter.write(outputText);
            myWriter.close();
            logger.info("============== Process Finished output saved to /ebs-data/image-recognitition/output.txt =================");
        } catch (IOException e) {
            logger.error("Error ocurred while writing output: {}", e.getMessage());
            System.exit(1);
        }
    }
}
