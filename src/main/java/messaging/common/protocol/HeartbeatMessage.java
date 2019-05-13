package messaging.common.protocol;

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
public class HeartbeatMessage extends RpcMessage {

	private static final long serialVersionUID = -3256535789697792543L;

	public HeartbeatMessage() {
		setType(TYPE_HEARTBEAT);
	}
	
}
