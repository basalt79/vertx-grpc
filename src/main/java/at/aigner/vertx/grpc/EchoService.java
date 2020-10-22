package at.aigner.vertx.grpc;

import at.aigner.vertx.grpc.EchoGrpc.EchoVertxImplBase;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

import java.util.Random;

public class EchoService extends EchoVertxImplBase {

  private static final Random RANDOM = new Random();
  private final Vertx vertx;

  EchoService(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void echo(EchoRequest request, Promise<EchoResponse> response) {
    if (request.getUseTimer()) {
      timer(RANDOM.nextInt(100) + 1, request, response);
    } else {
      noTimer(request, response);
    }
  }

  private void noTimer(EchoRequest request, Promise<EchoResponse> response) {
    var echoResponse = EchoResponse.newBuilder()
      .setMsg(request.getMsg() + "-" + SessionIdInterceptor.SESSION_ID_CTX_KEY.get())
      .build();
    response.complete(echoResponse);
  }

  private void timer(int ms, EchoRequest request, Promise<EchoResponse> response) {
    vertx.setTimer(ms, h -> {
      var sessionId = SessionIdInterceptor.SESSION_ID_CTX_KEY.get();
      var echoResponse = EchoResponse.newBuilder()
        .setMsg(request.getMsg() + "-" + sessionId)
        .build();
      response.complete(echoResponse);
    });
  }

}
