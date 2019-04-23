package messaging.example;

import messaging.client.RpcClient;
import messaging.client.RpcClientOptions;
import messaging.util.Endpoint;

/**
 * 
 * @author winflex
 */
public class Client {
	public static void main(String[] args) throws Exception {
		RpcClientOptions options = new RpcClientOptions(new Endpoint("localhost", 9999));
		options.setHeartbeatInterval(3000);
		RpcClient client = new RpcClient(options);
		AddResponse resp = client.requestSync(new AddRequest(1, 2), 3000);
		System.out.println(resp.getValue());
//		client.close();
	}
}
