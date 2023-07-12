package org.apollorm.function;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apollorm.model.FunctionHandler;
import org.apollorm.model.exception.FunctionException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This function expects a FASTQ sequence file that is located at an AWS S3 bucket located at
 * AWS US East 1 and the size of the subsequences and then uploads the subsequences into the
 * same bucket. The response contains the file names of the uploaded subsequences.
 * <p>
 * Note: This function needs to be modified to work outside of AWS Lambda, e.g. provide credentials
 * in request body.
 *
 * @author matthi-g
 */
public class Main implements FunctionHandler {
    public String main(String requestBody) throws JsonProcessingException {
        long start = System.currentTimeMillis();
        // Parse input
        ObjectMapper objectMapper = new ObjectMapper();
        Input input;
        try {
            input = objectMapper.readValue(requestBody, Input.class);
        } catch (Exception ex) {
            throw new FunctionException("bad input");
        }
        // Clear temp directory
        File tempDir = Path.of(Tempdir.getTempDir()).toFile();
        File[] tempDirContent = tempDir.listFiles();
        if (tempDirContent == null) {
            throw new FunctionException("tmp directory not available");
        }
        for(File file: tempDirContent)
            if (!file.isDirectory())
                file.delete();
        // Processing
        S3Client client = S3Client.builder()
            .credentialsProvider(DefaultCredentialsProvider.create())
            .region(Region.US_EAST_1)
            .build();
        // Download file
        downloadFile(client, input.getBucket(), input.getSeqs());

        long inputSize = 0;
        try {
            inputSize = Files.size(Path.of(Tempdir.getTempDir(), input.getSeqs()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Main processing
        FastqSplitter splitter = new FastqSplitter(input.getSeqsPerFile(), input.getSeqs());
        List<String> splittedSeq = splitter.split();

        // Upload file
        for (String seq: splittedSeq) {
            uploadFile(client, input.getBucket(), seq);
        }

        // Return the result
        Result result = new Result();
        result.setInputSizeMb(inputSize / 1000000.0);
        result.setSplitted(splittedSeq);
        long end = System.currentTimeMillis();
        result.setActualMs((end - start) / 1000.0);
        return objectMapper.writeValueAsString(result);
    }

    private void downloadFile(S3Client client, String bucket, String fileName) {
        GetObjectRequest getRequest = GetObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .build();
        client.getObject(getRequest, ResponseTransformer.toFile(Path.of(Tempdir.getTempDir(), fileName)));
    }

    private void uploadFile(S3Client client, String bucket, String fileName) {
        PutObjectRequest putRequest = PutObjectRequest.builder()
            .bucket(bucket)
            .key(fileName)
            .build();
        client.putObject(putRequest, RequestBody.fromFile(Path.of(Tempdir.getTempDir(), fileName)));
    }

    public static void main(String[] args) throws JsonProcessingException {
        FunctionHandler fh = new Main();
        String result = fh.main("{" +
            "\"bucket\": \"<BUCKET-NAME>\"," +
            "\"seqsPerFile\": 12000," +
            "\"seqs\": \"<FILE_NAME>\"" +
            "}");
        System.out.println(result);
    }
}
