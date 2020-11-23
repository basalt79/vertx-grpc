package at.aigner.vertx.grpc;

import io.grpc.*;
import io.vertx.grpc.ContextServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class SessionIdInterceptor extends ContextServerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(SessionIdInterceptor.class);
  public static final String SESSION_ID = "sessionId";

  @Override
  public void bind(Metadata metadata, ConcurrentMap<String, String> concurrentMap) {
    logger.info("Extract session id from metadata");
    concurrentMap.put(SESSION_ID, metadata.get(Metadata.Key.of(SESSION_ID, ASCII_STRING_MARSHALLER)));
  }

}
