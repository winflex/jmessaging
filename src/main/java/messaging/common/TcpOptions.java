package messaging.common;

import java.util.BitSet;

/**
 * TCP相关的一些配置项
 * 
 * @author winflex
 */
public final class TcpOptions {
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

	public boolean isKeepAlive() {
		return keepAlive;
	}

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
		bitSet.set(BIT_KEEP_ALIVE);
	}

	public boolean isKeepAliveSet() {
		return bitSet.get(BIT_KEEP_ALIVE);
	}

	public int getSendBuffer() {
		return sendBuffer;
	}

	public void setSendBuffer(int sendBuffer) {
		this.sendBuffer = sendBuffer;
		bitSet.set(BIT_SEND_BUFFER);
	}

	public boolean isSendBufferSet() {
		return bitSet.get(BIT_SEND_BUFFER);
	}

	public int getRecieveBuffer() {
		return recieveBuffer;
	}

	public void setRecieveBuffer(int recieveBuffer) {
		this.recieveBuffer = recieveBuffer;
		bitSet.set(BIT_RECIEVE_BUFFER);
	}

	public boolean isRecieveBufferSet() {
		return bitSet.get(BIT_RECIEVE_BUFFER);
	}

	public boolean isReuseAddress() {
		return reuseAddress;
	}

	public void setReuseAddress(boolean reuseAddress) {
		this.reuseAddress = reuseAddress;
		bitSet.set(BIT_REUSE_ADDRESS);
	}

	public boolean isReuseAddressSet() {
		return bitSet.get(BIT_REUSE_ADDRESS);
	}

	public int getLinger() {
		return linger;
	}

	public void setLinger(int linger) {
		this.linger = linger;
		bitSet.set(BIT_LINGER);
	}

	public boolean isLingerSet() {
		return bitSet.get(BIT_LINGER);
	}

	public int getBacklog() {
		return backlog;
	}

	public void setBacklog(int backlog) {
		this.backlog = backlog;
		bitSet.set(BIT_BACKLOG);
	}

	public boolean isBacklogSet() {
		return bitSet.get(BIT_BACKLOG);
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
		bitSet.set(BIT_TIMEOUT);
	}

	public boolean isTimeoutSet() {
		return bitSet.get(BIT_TIMEOUT);
	}

	public boolean isNoDelay() {
		return noDelay;
	}

	public void setNoDelay(boolean noDelay) {
		this.noDelay = noDelay;
		bitSet.set(BIT_NO_DELEY);
	}

	public boolean isNoDelaySet() {
		return bitSet.get(BIT_NO_DELEY);
	}
}