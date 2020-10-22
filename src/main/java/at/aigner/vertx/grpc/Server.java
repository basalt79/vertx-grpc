package at.aigner.vertx.grpc;

import io.grpc.ServerInterceptors;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.grpc.VertxServerBuilder;

import java.util.HashSet;
import java.util.Set;

public class Server extends AbstractVerticle {

    static final Set<Integer> PORTS = new HashSet<>();
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    @Override
    public void start(Promise<Void> promise) throws Exception {
        var host = "localhost";
        var service = ServerInterceptors.intercept(new EchoService(vertx), new SessionIdInterceptor());
        var server = VertxServerBuilder.forAddress(vertx, host, 0).addService(service).build();
        server.start(r -> {
            if (r.succeeded()) {
                PORTS.add(server.getPort());
                logger.info("Server started @ " + host + ":" + server.getPort());
                promise.complete();
            } else {
                logger.error("Could not start server " + r.cause().getMessage(), r.cause());
                promise.fail(r.cause());
            }
        });
    }

}
