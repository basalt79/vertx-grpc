package at.aigner.vertx.grpc;

import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
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

import static io.vertx.core.logging.LoggerFactory.LOGGER_DELEGATE_FACTORY_CLASS_NAME;

@RunWith(VertxUnitRunner.class)
public class ServerTest {

  private static final Logger logger = LoggerFactory.getLogger(ServerTest.class);
  private Vertx vertx;
  private final int instanceCount = 10;

  @Before
  public void setUp(TestContext ctx) {
    System.setProperty(LOGGER_DELEGATE_FACTORY_CLASS_NAME, SLF4JLogDelegateFactory.class.getName());
    var vertxOptions = new VertxOptions().setEventLoopPoolSize(1);
    var deploymentOptions = new DeploymentOptions().setInstances(instanceCount);
    vertx = Vertx.vertx(vertxOptions);
    vertx.deployVerticle(Server.class.getName(), deploymentOptions, ctx.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testMyApplication(TestContext ctx) {
    var async = ctx.async(instanceCount);
    Server.PORTS.forEach(port -> call(ctx, async, port));
    async.await(5000);
  }

  private void call(TestContext ctx, Async async, int port) {
    var sessionId = UUID.randomUUID().toString();
    var extraHeaders = new Metadata();
    extraHeaders.put(Metadata.Key.of("sessionId", Metadata.ASCII_STRING_MARSHALLER), sessionId);
    var clientInterceptor = MetadataUtils.newAttachHeadersInterceptor(extraHeaders);

    var channel = VertxChannelBuilder
      .forAddress(vertx, "localhost", port)
      .intercept(clientInterceptor)
      .usePlaintext(true)
      .disableRetry()
      .build();

    var msg = "Foobar";
    var stub = EchoGrpc.newVertxStub(channel);
    var request = EchoRequest.newBuilder().setMsg(msg).build();
    stub.echo(request, asyncResponse -> {
      if (asyncResponse.succeeded()) {
        logger.warn("Succeeded " + asyncResponse.result().getMsg());
        ctx.assertEquals(msg + "-" + sessionId, asyncResponse.result().getMsg());
        async.countDown();
      } else {
        logger.error(asyncResponse.cause());
        ctx.fail(asyncResponse.cause());
      }
      channel.shutdownNow();
    });
  }


}
