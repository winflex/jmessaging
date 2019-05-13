package messaging.common;

import java.util.BitSet;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * TCP相关的一些配置项
 * 
 * @author winflex
 */
@Getter
@Setter
@ToString
public class TcpOptions {
	private static final int BIT_KEEP_ALIVE = 0;
	private static final int BIT_SEND_BUFFER = 1;
	private static final int BIT_RECIEVE_BUFFER = 2;
	private static final int BIT_REUSE_ADDRESS = 3;
	private static final int BIT_LINGER = 4;
	private static final int BIT_BACKLOG = 5;
	private static final int BIT_TIMEOUT = 6;
	private static final int BIT_NO_DELEY = 7;

	private boolean keepAlive;
	private int sendBuffer;
	private int recieveBuffer;
	private boolean reuseAddress;
	private int linger;
	private int backlog;
	private int timeout;
	private boolean noDelay;

	private final BitSet bitSet = new BitSet(8);

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
		bitSet.set(BIT_KEEP_ALIVE);
	}

	public boolean isKeepAliveSet() {
		return bitSet.get(BIT_KEEP_ALIVE);
	}

	public void setSendBuffer(int sendBuffer) {
		this.sendBuffer = sendBuffer;
		bitSet.set(BIT_SEND_BUFFER);
	}

	public boolean isSendBufferSet() {
		return bitSet.get(BIT_SEND_BUFFER);
	}

	public void setRecieveBuffer(int recieveBuffer) {
		this.recieveBuffer = recieveBuffer;
		bitSet.set(BIT_RECIEVE_BUFFER);
	}

	public boolean isRecieveBufferSet() {
		return bitSet.get(BIT_RECIEVE_BUFFER);
	}

	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
		bitSet.set(BIT_REUSE_ADDRESS);
	}

	public boolean isReuseAddressSet() {
		return bitSet.get(BIT_REUSE_ADDRESS);
	}

	public void setLinger(int linger) {
		this.linger = linger;
		bitSet.set(BIT_LINGER);
	}

	public boolean isLingerSet() {
		return bitSet.get(BIT_LINGER);
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
		bitSet.set(BIT_BACKLOG);
	}

	public boolean isBacklogSet() {
		return bitSet.get(BIT_BACKLOG);
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		bitSet.set(BIT_TIMEOUT);
	}

	public boolean isTimeoutSet() {
		return bitSet.get(BIT_TIMEOUT);
	}

	public void setNoDelay(boolean noDelay) {
		this.noDelay = noDelay;
		bitSet.set(BIT_NO_DELEY);
	}

	public boolean isNoDelaySet() {
		return bitSet.get(BIT_NO_DELEY);
	}
}