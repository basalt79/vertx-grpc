
https://quarkus.io/guides/context-propagation-guide

https://github.com/eclipse/microprofile-context-propagation

https://github.com/OpenLiberty/open-liberty/issues/8660

This projects prove, if the default Context implementation from gRPC can be used in an vertx environment.

it will show, that the default Context handler cannot be used

https://github.com/vert-x3/vertx-grpc/issues/48


The `at.aigner.vertx.grpc.EchoService` is the bounded service on the gRPC.
This service tries to access the `SessionIdInterceptor.SESSION_ID_CTX_KEY`
behind this, there is the `io.grpc.Context` which uses by default the abstract `Storage` .
The only implementation of this `Storage` is the `ThreadLocalContextStorage`
So the data is bound to the current **thread**!

But if the eventloop of vertx switch threads, the information is gone, or mixed up.

If the service is fast enough, the context is ok, but it is still not reliable.

You can try to switch the service using the following 3 methods:

* `at.aigner.vertx.grpc.EchoService#timer` with Random
* `at.aigner.vertx.grpc.EchoService#timer` with static time
* `at.aigner.vertx.grpc.EchoService#noTimer` no timer at all, so this **could** work


