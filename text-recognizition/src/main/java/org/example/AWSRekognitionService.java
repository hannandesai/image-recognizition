package org.example;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.RekognitionException;
import software.amazon.awssdk.services.rekognition.model.S3Object;
import software.amazon.awssdk.services.rekognition.model.TextDetection;

public class AWSRekognitionService {
    private static final Logger logger = LoggerFactory.getLogger(AWSRekognitionService.class);
    private final RekognitionClient rekognitionClient;

    public AWSRekognitionService() {
        rekognitionClient = RekognitionClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .build();
    }

    public String detectTextFromImage(String s3BucketName, String s3ImageKey) {
        try {
            S3Object object = S3Object.builder()
                    .bucket(s3BucketName)
                    .name(s3ImageKey)
                    .build();

            Image image = Image.builder().s3Object(object).build();

            DetectTextRequest textRequest = DetectTextRequest.builder().image(image).build();

            DetectTextResponse textResponse = rekognitionClient.detectText(textRequest);

            List<TextDetection> detectedText = textResponse.textDetections();

            logger.info("No of text detected in Image: " + s3ImageKey + " are: " + detectedText.size());

            String textInImage = "";
            for (TextDetection text : detectedText) {
                textInImage = textInImage + text.detectedText();
            }

            return textInImage;
        } catch (RekognitionException e) {
            logger.error("Error occurred while detecting image text: {}", e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return "";
    }

}