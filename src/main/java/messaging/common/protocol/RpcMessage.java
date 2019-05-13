package messaging.common.protocol;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 
 * @author winflex
 */
@Getter
@Setter
@ToString
public class RpcMessage implements Serializable {

	private static final long serialVersionUID = 9009500469286134089L;

	public static final byte TYPE_REQUEST = 1;
	public static final byte TYPE_RESPONSE = 2;
	public static final byte TYPE_HEARTBEAT = 3;

	private byte type;
	private long id;
	private byte serializerCode;
	private Object data;

}
