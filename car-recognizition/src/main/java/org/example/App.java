package org.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class App {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    private static String BUCKET_NAME = "njit-cs-643";
    private static String QUEUE_NAME = "ImageQueue.fifo";

    public static void main(String... args) {
        logger.info("Application starts");

        startProducer();

        logger.info("Application ends");
    }

    private static void startProducer() {
        S3Service handler = new S3Service();
        SQSService sqsService = new SQSService();
        AWSRekognitionService awsRekognizitionService = new AWSRekognitionService();

        // get list of objects from given bucket
        List<String> images = handler.listBucketObjects(BUCKET_NAME);

        // run car detection logic on all images
        for (String key : images) {
            // detect image is car or not
            Boolean isCar = awsRekognizitionService.detectImage(BUCKET_NAME, key);
            if (isCar.equals(true)) {
                sqsService.send(QUEUE_NAME, key);
                logger.info("Image pushed in queue: " + key);
            }
        }

        // push -1 to queue once all images are processed
        sqsService.send(QUEUE_NAME, "-1");
    }
}
