package at.aigner.vertx.grpc;

import io.grpc.*;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class SessionIdInterceptor implements ServerInterceptor {
  private static final Logger logger = LoggerFactory.getLogger(SessionIdInterceptor.class);
  private static final Metadata.Key<String> SESSION_ID_METADATA_KEY = Metadata.Key.of("sessionId", ASCII_STRING_MARSHALLER);
  static final Context.Key<String> SESSION_ID_CTX_KEY = Context.key("sessionId");

  @Override
  public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
    ServerCall<ReqT, RespT> serverCall,
    Metadata metadata,
    ServerCallHandler<ReqT, RespT> serverCallHandler
  ) {
    var sessionId = metadata.get(SESSION_ID_METADATA_KEY);
    logger.info("sessionId in interceptor: " + sessionId);
    var ctx = Context.current().withValue(SESSION_ID_CTX_KEY, sessionId);
    return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
  }
}
