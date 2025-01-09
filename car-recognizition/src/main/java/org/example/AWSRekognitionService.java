package org.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsRequest;
import software.amazon.awssdk.services.rekognition.model.DetectLabelsResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.Label;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.S3Object;

public class AWSRekognitionService {
    private static final Logger logger = LoggerFactory.getLogger(AWSRekognitionService.class);

    /**
     * This function is used to detect provied image exist in s3 bucket is of car or not.
     * @param s3BucketName
     * @param s3ImageName
     * @return
     */
    public boolean detectImage(String s3BucketName, String s3ImageName) {
        try {
            RekognitionClient client = RekognitionClient.builder()
                    .region(Region.US_EAST_1)
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();

            S3Object object = S3Object
                    .builder()
                    .bucket(s3BucketName)
                    .name(s3ImageName)
                    .build();

            Image image = Image.builder().s3Object(object).build();

            DetectLabelsRequest detectLabelsRequest = DetectLabelsRequest.builder().image(image).maxLabels(10).build();

            DetectLabelsResponse detectLabelsResponse = client.detectLabels(detectLabelsRequest);

            client.close();

            List<Label> detectLabels = detectLabelsResponse.labels();

            logger.info("Detected Labels for image: " + s3ImageName);

            for (Label label: detectLabels) {
                // System.out.println(detectLabels);
                if (label.name().equals("Car") && label.confidence() > 90) {
                    return true;
                } else {
                    return false;
                }
            }
            
            return false;
        } catch (RekognitionException e) {
            String error = e.awsErrorDetails().errorMessage();
            logger.error("Detect image error: " + error);
            System.exit(1);
            return false;
        }
    }
}
