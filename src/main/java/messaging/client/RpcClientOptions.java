package messaging.client;

import messaging.common.TcpOptions;
import messaging.util.Endpoint;
import messaging.util.StringUtils;

/**
 * RPC客户端配置
 * 
 * @author winflex
 */
public class RpcClientOptions {

	/**
	 * 服务端地址
	 */
	private Endpoint endpoint;

	/**
	 * io线程个数
	 */
	private int ioThreads;

	/**
	 * 创建连接超时时间
	 */
	private int connectTimeoutMillis = 3000;

	/**
	 * 最大连接数
	 */
	private int maxConnections = 1;
	
	private int heartbeatIntervalMillis = 30000;

	/**
	 * 序列化扩展点名字, 默认使用hessian序列化
	 */
	private byte serializerCode = 0;

	/**
	 * 动态代理扩展点名字, 默认使用jdk
	 */
	private String proxy = "jdk";
	
	private TcpOptions tcpOptions;

	public RpcClientOptions(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(Endpoint endpoint) {
		this.endpoint = endpoint;
	}

	public int getMaxConnections() {
		return maxConnections;
	}

	public void setMaxConnections(int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public int getIoThreads() {
		return ioThreads;
	}

	public void setIoThreads(int ioThreads) {
		this.ioThreads = ioThreads;
	}

	public int getConnectTimeout() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeout(int connectTimeoutMillis) {
		if (connectTimeoutMillis <= 0) {
			throw new IllegalArgumentException("The connectTimeoutMillis must be positive");
		}
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public int getHeartbeatInterval() {
		return heartbeatIntervalMillis;
	}

	public void setHeartbeatInterval(int heartbeatIntervalMillis) {
		this.heartbeatIntervalMillis = heartbeatIntervalMillis;
	}

	public byte getSerializerCode() {
		return serializerCode;
	}

	public void setSerializerCode(byte serializerCode) {
		this.serializerCode = serializerCode;
	}

	public String getProxy() {
		return proxy;
	}

	public void setProxy(String proxy) {
		if (StringUtils.isEmpty(proxy)) {
			throw new IllegalArgumentException("The proxy can't be null");
		}
		this.proxy = proxy;
	}

	public TcpOptions getTcpOptions() {
		return tcpOptions;
	}

	public void setTcpOptions(TcpOptions tcpOptions) {
		this.tcpOptions = tcpOptions;
	}
}
