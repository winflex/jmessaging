package messaging.example;

import java.io.IOException;

import messaging.client.RpcClient;
import messaging.client.RpcClientOptions;
import messaging.common.RpcException;
import messaging.util.Endpoint;

/**
 * 
 * @author winflex
 */
public class BenchMarkClient {
	public static void main(String[] args) throws IOException {
		RpcClientOptions options = new RpcClientOptions(new Endpoint("localhost", 9999));
		options.setMaxConnections(3);
		RpcClient client = new RpcClient(options);
		AddRequest req = new AddRequest(1, 2);
		LoadRunner lr = LoadRunner.builder().millis(300000).reportInterval(1000).threads(64).transaction(() -> {
			try {
				client.requestSync(req, 3000);
			} catch (RpcException e) {
				e.printStackTrace();
			}
		}).build();
		lr.run();
		client.close();
	}
}
