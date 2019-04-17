package messaging.common.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author winflex
 */
public class RpcRequest extends RpcMessage {

	private static final long serialVersionUID = -9169741435572800622L;

	public RpcRequest(Object data, boolean oneWay) {
		this(sequence.incrementAndGet(), data, oneWay);
	}
	
	public RpcRequest(long requestId, Object data, boolean oneWay) {
		setId(requestId);
		setData(data);
		setOneWay(oneWay);
		setType(TYPE_REQUEST);
	}

	private static final AtomicLong sequence = new AtomicLong();
}
