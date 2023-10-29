package org.apollorm.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apollorm.model.FunctionHandler;
import org.apollorm.function.Main;
import org.apollorm.model.MonitoringData;
import org.apollorm.model.RequestType;
import org.apollorm.model.ResponseWithMonitoring;

/**
 * Represents the request handler for an Openfaas function.
 *
 * @author matthi-g
 */
public class RequestHandler implements Handler<RoutingContext> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  public void handle(RoutingContext rc) {
    long startTimeStamp = System.currentTimeMillis();
    long startTime = System.nanoTime();
    String input = rc.body().asString();
    String result;
    try {
      FunctionHandler handler = new Main();
      result = handler.main(input);
      if (rc.request().getHeader("apollo-request-type").equals(RequestType.RM.getValue())) {
        MonitoringData monitoringData = new MonitoringData();
        monitoringData.setStartTimestamp(startTimeStamp);
        long endTime = System.nanoTime();
        monitoringData.setExecutionTimeMs((endTime - startTime) / 1_000_000.0);
        ResponseWithMonitoring response = new ResponseWithMonitoring();
        response.setMonitoringData(monitoringData);
        response.setBody(objectMapper.readValue(result, Object.class));
        result = objectMapper.writeValueAsString(response);
      }
    } catch (JsonProcessingException e) {
      throw new RuntimeException("error in result serialization");
    }
    rc.response()
      .putHeader("content-type", "application/json;charset=UTF-8")
      .end(result);
  }
}
