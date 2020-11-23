package at.aigner.vertx.grpc;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.SLF4JLogDelegateFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.grpc.VertxChannelBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;


@RunWith(VertxUnitRunner.class)
public class ServerTest {

  private Vertx vertx;
  private final int instanceCount = 10;

  @Before
  public void setUp(TestContext ctx) {
    System.setProperty("vertx.logger-delegate-factory-class-name", SLF4JLogDelegateFactory.class.getName());
    var vertxOptions = new VertxOptions().setEventLoopPoolSize(1);
    vertx = Vertx.vertx(vertxOptions);
    var deploymentOptions = new DeploymentOptions().setInstances(instanceCount);
    vertx.deployVerticle(Server.class.getName(), deploymentOptions, ctx.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void echoWithTimer(TestContext ctx) {
    var async = ctx.async(instanceCount);
    Server.PORTS.forEach(port -> call(ctx, async, port, true));
    async.await(5000);
  }

  @Test
  public void echoWithoutTimer(TestContext ctx) {
    var async = ctx.async(instanceCount);
    Server.PORTS.forEach(port -> call(ctx, async, port, false));
    async.await(5000);
  }

  private void call(TestContext ctx, Async async, int port, boolean useTimer) {
    var sessionId = UUID.randomUUID().toString();
    var extraHeaders = new Metadata();
    extraHeaders.put(Metadata.Key.of("sessionId", Metadata.ASCII_STRING_MARSHALLER), sessionId);
    var clientInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);

    var channel = VertxChannelBuilder
      .forAddress(vertx, "localhost", port)
      .intercept(clientInterceptor)
      .usePlaintext()
      .disableRetry()
      .build();

    var msg = "foobar";
    var stub = EchoGrpc.newStub(channel);
    var request = EchoRequest.newBuilder().setMsg(msg).setUseTimer(useTimer).build();

    stub.echo(request, new StreamObserver<>() {
      private EchoResponse response;

      @Override
      public void onNext(EchoResponse echoResponse) {
        this.response = echoResponse;
        async.countDown();
      }

      @Override
      public void onError(Throwable throwable) {
        System.out.println("Error " + throwable.getMessage());
        ctx.fail(throwable);
      }

      @Override
      public void onCompleted() {
        ctx.assertNotNull(request);
        ctx.assertNotNull(request.getMsg());
        System.out.println("Got the server response: " + response.getMsg());
      }
    });
  }

}
