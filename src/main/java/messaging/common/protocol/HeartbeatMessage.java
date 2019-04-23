package messaging.common.protocol;

/**
 * 
 * @author winflex
 */
public class HeartbeatMessage extends RpcMessage {

	private static final long serialVersionUID = -3256535789697792543L;

	public HeartbeatMessage() {
		setType(TYPE_HEARTBEAT);
	}
	
}
