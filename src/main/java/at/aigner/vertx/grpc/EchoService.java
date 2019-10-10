package at.aigner.vertx.grpc;

import at.aigner.vertx.grpc.EchoGrpc.EchoVertxImplBase;
import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Random;

public class EchoService extends EchoVertxImplBase {

  private static final Random RANDOM = new Random();
  private final Vertx vertx;

  EchoService(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void echo(EchoRequest request, Future<EchoResponse> response) {
    timer(RANDOM.nextInt(100) + 1, request, response);
//    noTimer(request, response);
//    timer(5, request, response);

  }

  private void noTimer(EchoRequest request, Future<EchoResponse> response) {
    response.complete(EchoResponse.newBuilder()
      .setMsg(request.getMsg() + "-" + SessionIdInterceptor.SESSION_ID_CTX_KEY.get()).build());
  }

  private void timer(int ms, EchoRequest request, Future<EchoResponse> response) {
    vertx.setTimer(ms, h -> response.complete(EchoResponse.newBuilder()
      .setMsg(request.getMsg() + "-" + SessionIdInterceptor.SESSION_ID_CTX_KEY.get()).build()));
  }

}
