// Copyright (c) OpenFaaS Author(s) 2018. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.
package org.apollorm.entrypoint;

import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.apollorm.model.exception.FunctionException;

import java.util.Optional;

public class App {
  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    int httpPort = Integer.parseInt(Optional.ofNullable(System.getenv("PORT")).orElse("8082"));
    HttpServer server = vertx.createHttpServer();
    Router router = Router.router(vertx);

    Handler<RoutingContext> handler = new RequestHandler();
    router.route()
            .failureHandler(rc -> {
              String message  = "internal server error";
              int statusCode = 500;
              if (rc.failure() != null && rc.failure() instanceof FunctionException){
                message = rc.failure().getMessage();
                statusCode = 400;
              }
              rc.response()
                      .putHeader("Content-type", "text/plain; charset=utf-8")
                      .setStatusCode(statusCode)
                      .end(message);
            })
            .handler(BodyHandler.create())
            .handler(handler);

    server.requestHandler(router).listen(httpPort, result -> {
      if(result.succeeded()) {
        System.out.println("Listening on port " + httpPort);
      } else {
        System.out.println("Unable to start server: " + result.cause().getMessage());
      }
    });
  }
}
