# jmessaging
An easy-to-use RPC framework

# Example

# Server
```
public class Server {
  public static void main(String[] args) throws RpcException {
    RpcServer server = new RpcServer(9999);
    server.registerHandler(new IRequestHandler<AddRequest>() {

      @Override
      public void handleRequest(Context ctx, AddRequest request) {
        int value = request.getOp1() + request.getOp2();
        ctx.writeResponse(new AddResponse(value));
      }

      @Override
      public String interestClass() {
        return AddRequest.class.getName();
      }

      @Override
      public Executor getExecutor() {
        return null;
      }
    });
    server.start().closeFuture().awaitUninterruptibly();
  }
}
```

# Client
```
public class Client {
  public static void main(String[] args) throws RpcException {
    RpcClientOptions options = new RpcClientOptions(new Endpoint("localhost", 9999));
    RpcClient client = new RpcClient(options);
    
    // synchronous request
    try {
      AddResponse response = client.requestSync(new AddRequest(1, 2), 3000);
      // xxx
    } catch (RpcException e) {
      // xxx 
    }
    
    // asynchronous request
    IFuture<AddResponse> future = client.requestAsync(new AddRequest(1, 2), 3000);
    future.addListener((future) -> {
      if (future.isSuccess()) {
        // xxx
      } else {
        // xxx
      }
    });
    
    // synchronous one way request
    Object oneWayRequest = ...;
    try {
      client.sendSync(oneWayRequest, 3000);
      // xxx
    } catch (RpcException e) {
      // xxx 
    }
    
    // asynchronous one way request
    IFuture<Void> future = client.sendAsync(oneWayRequest, 3000);
    future.addListener((future) -> {
      if (future.isSuccess()) {
        // xxx
      } else {
        // xxx
      }
    }
  }
}
```
