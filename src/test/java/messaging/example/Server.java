package messaging.example;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.alipay.remoting.NamedThreadFactory;

import messaging.common.RpcException;
import messaging.server.Context;
import messaging.server.IRequestHandler;
import messaging.server.RpcServer;
import messaging.server.RpcServerOptions;

/**
 * 
 * @author winflex
 */
public class Server {
	public static void main(String[] args) throws RpcException {
		RpcServerOptions options = new RpcServerOptions(9999);
		options.setIoThreads(1);
		options.setIdleTimeout(10000);
		RpcServer server = new RpcServer(options);
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
		server.setExecutor(Executors.newSingleThreadExecutor(new NamedThreadFactory("ServiceExecutor", true)));
		server.start().closeFuture().awaitUninterruptibly();
	}
}
