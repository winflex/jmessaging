package messaging.util;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import lombok.Data;

/**
 * 
 * @author winflex
 */
@Data
public class Endpoint implements Serializable {

	private static final long serialVersionUID = 6780623948082682620L;

	private final String ip;
	private final int port;

	public Endpoint(String address, int port) {
		this.ip = address;
		this.port = port;
	}

	public SocketAddress toSocketAddress() {
		return new InetSocketAddress(ip, port);
	}
}
