package messaging.example;

import com.alipay.remoting.AsyncContext;
import com.alipay.remoting.BizContext;
import com.alipay.remoting.rpc.RpcServer;
import com.alipay.remoting.rpc.protocol.AsyncUserProcessor;

/**
 * 
 * @author winflex
 */
public class BoltServer {
	public static void main(String[] args) {
		RpcServer server = new RpcServer(9999);
		server.registerUserProcessor(new AddHandler());
		server.start();
	}
	
	static class AddHandler extends AsyncUserProcessor<AddRequest> {

		@Override
		public void handleRequest(BizContext bizCtx, AsyncContext asyncCtx, AddRequest request) {
			asyncCtx.sendResponse(new AddResponse(request.getOp1() + request.getOp2()));
		}

		@Override
		public String interest() {
			return AddRequest.class.getName();
		}
		
	}
}
