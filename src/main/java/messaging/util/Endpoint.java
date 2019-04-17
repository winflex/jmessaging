package messaging.util;

import java.io.Serializable;
import java.net.InetSocketAddress;

/**
 * 
 * @author winflex
 */
public class Endpoint implements Serializable {

	private static final long serialVersionUID = 6780623948082682620L;

	private final String ip;
	private final int port;
	private String str;

	public Endpoint(String address, int port) {
		this.ip = address;
		this.port = port;
	}

	public String getIp() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}

	public InetSocketAddress toInetSocketAddress() {
		return new InetSocketAddress(this.ip, this.port);
	}

	@Override
	public String toString() {
		if (str == null) {
			str = this.ip + ":" + this.port;
		}
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.ip == null ? 0 : this.ip.hashCode());
		result = prime * result + this.port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Endpoint other = (Endpoint) obj;
		if (this.ip == null) {
			if (other.ip != null) {
				return false;
			}
		} else if (!this.ip.equals(other.ip)) {
			return false;
		}
		return this.port == other.port;
	}
}
