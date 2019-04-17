package messaging.example;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import messaging.client.RpcClient;
import messaging.util.Endpoint;

/**
 * 
 * @author winflex
 */
public class Client {
	public static void main(String[] args) throws IOException, InterruptedException {
		RpcClient client = new RpcClient(new Endpoint("localhost", 9999));
		CountDownLatch latch = new CountDownLatch(1);
		client.<AddResponse, AddRequest>request(new AddRequest(1, 2)).awaitUninterruptibly().addListener((f) -> {
			if (f.isSuccess()) {
				System.out.println(f.getNow().getValue());
			} else {
				f.cause().printStackTrace();
			}
			latch.countDown();
		});
		latch.await();
		client.close();
	}
}
