package messaging.common.protocol;

import java.io.Serializable;

/**
 * 
 * @author winflex
 */
public class RpcMessage implements Serializable {

	private static final long serialVersionUID = 9009500469286134089L;

	public static final byte TYPE_REQUEST = 1;
	public static final byte TYPE_RESPONSE = 2;

	private byte type;
	private long id;
	private boolean oneWay; // 是否是单向请求
	private Object data;

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
