package messaging.example;

import java.io.IOException;

import messaging.client.RpcClient;
import messaging.client.RpcClientOptions;
import messaging.util.Endpoint;

/**
 * 
 * @author winflex
 */
public class BenchMarkClient {
	public static void main(String[] args) throws IOException {
		RpcClientOptions options = new RpcClientOptions(new Endpoint("10.8.1.84", 9999));
		options.setMaxConnections(3);
		RpcClient client = new RpcClient(options);
		AddRequest req = new AddRequest(1, 2);
		LoadRunner lr = LoadRunner.builder().millis(300000).reportInterval(1000).threads(64).transaction(() -> {
			client.requestSync(req, 3000);
		}).build();
		lr.run();
	}
}
