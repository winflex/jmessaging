# jmessaging
一个轻量级、易用的异步通讯框架

# Example
## Custom message
### AddRequest
```
public class AddRequest implements Serializable {
  private static final long serialVersionUID = 3848094754327316874L;
  private int op1;
  private int op2;

  public AddRequest() {
  }

  public AddRequest(int op1, int op2) {
    super();
    this.op1 = op1;
    this.op2 = op2;
  }

  public int getOp1() {
    return op1;
  }

  public void setOp1(int op1) {
    this.op1 = op1;
  }

  public int getOp2() {
    return op2;
  }

  public void setOp2(int op2) {
    this.op2 = op2;
  }
}

```
### AddResponse
```
public class AddResponse implements Serializable {
  private static final long serialVersionUID = -4644759432114731809L;
  private int value;

  public AddResponse() {
  }

  public AddResponse(int value) {
    super();
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  public void setValue(int value) {
    this.value = value;
  }
}
```

## Server
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

## Client
```
public class Client {
  public static void main(String[] args) throws RpcException {
    RpcClientOptions options = new RpcClientOptions(new Endpoint("localhost", 9999));
    RpcClient client = new RpcClient(options);
    IFuture<AddResponse> future = client.request(new AddRequest(1, 2));
    AddResponse resp = future.awaitUninterruptibly().getNow();
  }
}
```
