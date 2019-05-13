package messaging.client;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import messaging.common.TcpOptions;
import messaging.util.Endpoint;

/**
 * RPC客户端配置
 * 
 * @author winflex
 */
@Getter
@Setter
@ToString
public class RpcClientOptions extends TcpOptions {

	/**
	 * 服务端地址
	 */
	private Endpoint endpoint;

	/**
	 * io线程个数
	 */
	private int ioThreads;

	/**
	 * 创建连接超时时间, 毫秒
	 */
	private int connectTimeout = 3000;

	/**
	 * 最大连接数
	 */
	private int maxConnections = 1;
	
	/**
	 * 心跳间隔, 毫秒
	 */
	private int heartbeatInterval = 30000;

	/**
	 * 序列化扩展点名字, 默认使用hessian序列化
	 */
	private byte serializerCode = 0;

	/**
	 * 动态代理扩展点名字, 默认使用jdk
	 */
	private String proxy = "jdk";
	
	public RpcClientOptions(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public void setConnectTimeout(int connectTimeoutMillis) {
		if (connectTimeoutMillis <= 0) {
			throw new IllegalArgumentException("The connectTimeoutMillis must be positive");
		}
		this.connectTimeout = connectTimeoutMillis;
	}

	public void setProxy(String proxy) {
		if (proxy == null || proxy.isEmpty()) {
			throw new IllegalArgumentException("The proxy can't be null");
		}
		this.proxy = proxy;
	}
}
