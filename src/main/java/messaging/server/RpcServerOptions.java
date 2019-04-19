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
	 * 心跳间隔
	 */
	private int heartbeatInterval = 10000;
	
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

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public TcpOptions getTcpOptions() {
		return tcpOptions;
	}

	public void setTcpOptions(TcpOptions tcpOptions) {
		this.tcpOptions = tcpOptions;
	}
}