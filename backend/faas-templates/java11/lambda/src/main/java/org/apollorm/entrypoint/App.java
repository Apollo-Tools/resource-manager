package org.apollorm.entrypoint;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apollorm.model.FunctionHandler;
import org.apollorm.model.MonitoringData;
import org.apollorm.model.RequestType;
import org.apollorm.model.ResponseWithMonitoring;
import org.apollorm.model.exception.AWSErrorResponse;
import org.apollorm.model.exception.FunctionException;
import org.apollorm.function.Main;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Represents the request handler for an AWS Lambda function.
 *
 * @author matthi-g
 */
public class App implements RequestStreamHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        long startTimeStamp = System.currentTimeMillis();
        long startTime = System.nanoTime();
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Request.class, new RequestDeserializer());
        objectMapper.registerModule(module);
        // Parse input
        byte[] requestBytes = inputStream.readAllBytes();
        String requestBody = new String(requestBytes, StandardCharsets.UTF_8);
        Request request = objectMapper.readValue(requestBody, Request.class);
        try {
            // Call function implementation
            FunctionHandler functionHandler = new Main();
            String result = functionHandler.main(request.getBody());
            // Add monitoring data
            if (request.getRequestType().equals(RequestType.RM)) {
                MonitoringData monitoringData = new MonitoringData();
                monitoringData.setStartTimestamp(startTimeStamp);
                long endTime = System.nanoTime();
                monitoringData.setExecutionTimeMs((endTime - startTime) / 1_000_000.0);
                ResponseWithMonitoring response = new ResponseWithMonitoring();
                response.setMonitoringData(monitoringData);
                response.setBody(result);
                result = objectMapper.writeValueAsString(response);
            }
            // Return value
            outputStream.write(result.getBytes());
        } catch (FunctionException ex) {
            AWSErrorResponse response = new AWSErrorResponse();
            response.setBody(ex.getMessage());
            response.setStatusCode(400);
            objectMapper.writeValue(outputStream, response);
        }
    }
}
