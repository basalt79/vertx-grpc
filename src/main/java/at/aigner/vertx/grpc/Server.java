package at.aigner.vertx.grpc;

import io.grpc.ServerInterceptors;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.grpc.VertxServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class Server extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(Server.class);
  static final Set<Integer> PORTS = new HashSet<>();

  @Override
  public void start(Promise<Void> promise) {
    var host = "localhost";
    var service = ServerInterceptors.intercept(new EchoService(vertx), new SessionIdInterceptor());
    var server = VertxServerBuilder.forAddress(vertx, host, 0).addService(service).build();
    server.start(r -> {
      if (r.succeeded()) {
        PORTS.add(server.getPort());
        logger.info("Server started @ {}:{}", host, server.getPort());
        promise.complete();
      } else {
        logger.error("Could not start server {}", r.cause().getMessage(), r.cause());
        promise.fail(r.cause());
      }
    });
  }

}
