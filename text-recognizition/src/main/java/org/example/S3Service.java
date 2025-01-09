
package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * The module containing all dependencies required by the {@link AWSRekognitionService}.
 */
public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    // amazon s3 client object
    private final S3Client s3Client;

    public S3Service() {
        s3Client = S3Client.builder()
        .credentialsProvider(DefaultCredentialsProvider.create())
        .httpClientBuilder(ApacheHttpClient.builder())
        .region(Region.US_EAST_1)
        .build();
    }

    public void getObject(String bucket, String key) {
        try {
            GetObjectRequest request = GetObjectRequest
                    .builder()
                    .key(key)
                    .bucket(bucket)
                    .build();

            ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(request);
            byte[] data = objectBytes.asByteArray();
            File myFile = new File("./" + key);
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            logger.info("Successfully obtained bytes from an S3 object");
            os.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (S3Exception e) {
            String error = e.awsErrorDetails().errorMessage();
            logger.error(error);
            // return error;
        }
    }
}