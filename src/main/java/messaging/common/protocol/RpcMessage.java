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
	public static final byte TYPE_HEARTBEAT = 3;

	private byte type;
	private long id;
	private byte serializerCode;
	private Object data;

	public byte getSerializerCode() {
		return serializerCode;
	}

	public void setSerializerCode(byte serializerCode) {
		this.serializerCode = serializerCode;
	}

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

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
}
