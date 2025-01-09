package org.example;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3Service {
    private static final Logger logger = LoggerFactory.getLogger(S3Service.class);
    private final S3Client s3Client;

    public S3Service() {
        s3Client = S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .region(Region.US_EAST_1)
                .httpClientBuilder(ApacheHttpClient.builder())
                .build();
    }

    public List<String> listBucketObjects(String bucket) {
        List<String> objectList = new ArrayList<String>();
        try {
            ListObjectsRequest listObjects = ListObjectsRequest
                    .builder()
                    .bucket(bucket)
                    .build();

            ListObjectsResponse res = s3Client.listObjects(listObjects);
            List<S3Object> objects = res.contents();
            for (S3Object myValue : objects) {
                // System.out.print("\n The name of the key is " + myValue.key());
                objectList.add(myValue.key());
            }
            return objectList;
        } catch (S3Exception e) {
            logger.error("Get bucket objects error: " + e.awsErrorDetails().errorMessage());
            System.exit(1);
        }
        return objectList;
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
            System.exit(1);
        } catch (S3Exception e) {
            String error = e.awsErrorDetails().errorMessage();
            logger.error(error);
            System.exit(1);
            // return error;
        }
    }
}
