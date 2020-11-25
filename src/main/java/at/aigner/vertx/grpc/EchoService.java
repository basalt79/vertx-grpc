package at.aigner.vertx.grpc;

import io.grpc.stub.StreamObserver;
import io.vertx.core.Vertx;
import io.vertx.grpc.ContextServerInterceptor;

import java.util.Random;

public class EchoService extends EchoGrpc.EchoImplBase {

  private static final Random RANDOM = new Random();
  private final Vertx vertx;

  EchoService(Vertx vertx) {
    this.vertx = vertx;
  }

  @Override
  public void echo(EchoRequest request, StreamObserver<EchoResponse> responseObserver) {
    if (request.getUseTimer()) {
      timer(RANDOM.nextInt(100) + 1, request, responseObserver);
    } else {
      noTimer(request, responseObserver);
    }
  }

  private void noTimer(EchoRequest request, StreamObserver<EchoResponse> response) {
    response.onNext(EchoResponse.newBuilder()
      .setMessage(request.getMessage() + "-" + getSessionId())
      .build());
    response.onCompleted();
  }

  private void timer(int ms, EchoRequest request, StreamObserver<EchoResponse> response) {
    vertx.setTimer(ms, h -> {
      response.onNext(EchoResponse.newBuilder()
        .setMessage(request.getMessage() + "-" + getSessionId())
        .build());
      response.onCompleted();
    });
  }

  private String getSessionId() {
    return ContextServerInterceptor.get("sessionid");
  }

}
