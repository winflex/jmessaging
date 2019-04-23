package messaging.server;

import messaging.common.TcpOptions;

/**
 * RPC服务端配置
 * 
 * @author winflex
 */
public class RpcServerOptions {

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
	private int idleTimeoutMillis = 90 * 1000;
	
	private TcpOptions tcpOptions;

	public RpcServerOptions(int port) {
		if (port <= 0) {
			throw new IllegalArgumentException("port must be positive");
		}
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getBindIp() {
		return bindIp;
	}

	public void setBindIp(String bindIp) {
		this.bindIp = bindIp;
	}

	public int getIoThreads() {
		return ioThreads;
	}

	public void setIoThreads(int ioThreads) {
		this.ioThreads = ioThreads;
	}

	public int getIdleTimeout() {
		return idleTimeoutMillis;
	}

	public void setIdleTimeout(int idleTimeoutMillis) {
		this.idleTimeoutMillis = idleTimeoutMillis;
	}

	public TcpOptions getTcpOptions() {
		return tcpOptions;
	}

	public void setTcpOptions(TcpOptions tcpOptions) {
		this.tcpOptions = tcpOptions;
	}
}