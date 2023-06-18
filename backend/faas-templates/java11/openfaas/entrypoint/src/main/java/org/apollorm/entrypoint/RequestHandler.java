package org.apollorm.entrypoint;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apollorm.model.FunctionHandler;
import org.apollorm.model.function.Main;

public class RequestHandler implements Handler<RoutingContext> {

  public void handle(RoutingContext rc) {
    String input = rc.body().asString();
    String result;
    try {
      FunctionHandler handler = new Main();
      result = handler.main(input);
    } catch (JsonProcessingException e) {
      throw new RuntimeException("error in result serialization");
    }
    rc.response()
      .putHeader("content-type", "application/json;charset=UTF-8")
      .end(result);
  }
}
