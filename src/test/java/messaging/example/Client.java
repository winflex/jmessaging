package messaging.example;

import java.io.IOException;

import messaging.client.RpcClient;
import messaging.client.RpcClientOptions;
import messaging.util.Endpoint;

/**
 * 
 * @author winflex
 */
public class Client {
	public static void main(String[] args) throws IOException, InterruptedException {
		RpcClientOptions options = new RpcClientOptions(new Endpoint("localhost", 9999));
		RpcClient client = new RpcClient(options);
		AddResponse resp = client.requestSync(new AddRequest(1, 2), 3000);
		System.out.println(resp.getValue());
		client.close();
	}
}
