package messaging.example;

import java.util.concurrent.Executor;

import messaging.common.RpcException;
import messaging.server.Context;
import messaging.server.IRequestHandler;
import messaging.server.RpcServer;

/**
 * 
 * @author winflex
 */
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
