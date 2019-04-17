package messaging.common.protocol;

import messaging.common.RpcResult;

/**
 * 
 * @author winflex
 */
public class RpcResponse extends RpcMessage {

	private static final long serialVersionUID = -6908316723638678541L;

	public RpcResponse(long requestId, RpcResult data) {
		setId(requestId);
		setData(data);
		setOneWay(false);
		setType(TYPE_RESPONSE);
	}
}
