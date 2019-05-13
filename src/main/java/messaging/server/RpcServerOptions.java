package messaging.server;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import messaging.common.TcpOptions;

/**
 * RPC服务端配置
 * 
 * @author winflex
 */
@Getter
@Setter
@ToString
public class RpcServerOptions extends TcpOptions {

	/**
	 * RPC 服务端监听端口
	 */
	private int port;

	/**
	 * RPC服务端监听地址
	 */
	private String bindIp = "0.0.0.0";

	/**
	 * IO线程数
	 */
	private int ioThreads = 0;

	/**
	 * 空闲多久后关闭连接, <= 0时不启用
	 */
	private int idleTimeout = 90 * 1000;

	public RpcServerOptions(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("port must be positive");
		}
		this.port = port;
	}
}