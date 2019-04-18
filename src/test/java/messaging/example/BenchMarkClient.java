package messaging.example;

import java.io.IOException;

import messaging.client.RpcClient;
import messaging.util.Endpoint;

/**
 * 
 * @author winflex
 */
public class BenchMarkClient {
	public static void main(String[] args) throws IOException {
		RpcClient client = new RpcClient(new Endpoint("localhost", 9999));
		AddRequest req = new AddRequest(1, 2);
		LoadRunner lr = LoadRunner.builder().millis(300000).reportInterval(1000).threads(4).transaction(() -> {
			client.request(req).awaitUninterruptibly();
		}).build();
		lr.run();
	}
}
