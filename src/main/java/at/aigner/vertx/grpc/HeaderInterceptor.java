package at.aigner.vertx.grpc;

import io.grpc.Metadata;
import io.vertx.grpc.ContextServerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentMap;

public class HeaderInterceptor extends ContextServerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(HeaderInterceptor.class);

  @Override
  public void bind(Metadata metadata, ConcurrentMap<String, String> concurrentMap) {
    metadata.keys().forEach(k -> {
      var value = metadata.get(Metadata.Key.of(k, Metadata.ASCII_STRING_MARSHALLER));
      logger.info("put {} with value {}", k, value);
      concurrentMap.put(k, value);
    });
  }

}
