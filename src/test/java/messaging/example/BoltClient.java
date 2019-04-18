package messaging.example;

import com.alipay.remoting.exception.RemotingException;
import com.alipay.remoting.rpc.RpcClient;

/**
 * 
 * @author winflex
 */
public class BoltClient {
	public static void main(String[] args) throws RemotingException, InterruptedException {
		AddRequest req = new AddRequest(1, 2);
		RpcClient client = new RpcClient();
		client.init();
		LoadRunner lr = LoadRunner.builder().millis(30000).reportInterval(1000).threads(1).transaction(() -> {
			try {
				client.invokeSync("localhost:9999", req, 3000);
			} catch (Exception e) {
				// ignore
			}
		}).build();
		lr.run();
	}
}
