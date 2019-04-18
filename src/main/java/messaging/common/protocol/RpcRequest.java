package messaging.common.protocol;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 
 * @author winflex
 */
public class RpcRequest extends RpcMessage {

	private static final long serialVersionUID = -9169741435572800622L;

	private boolean oneWay; // 是否是单向请求
	
	public RpcRequest(Object data, boolean oneWay) {
		this(sequence.incrementAndGet(), data, oneWay);
	}
	
	public RpcRequest(long requestId, Object data, boolean oneWay) {
		setId(requestId);
		setData(data);
		setOneWay(oneWay);
		setType(TYPE_REQUEST);
	}

	public boolean isOneWay() {
		return oneWay;
	}

	public void setOneWay(boolean oneWay) {
		this.oneWay = oneWay;
	}
	
	private static final AtomicLong sequence = new AtomicLong();
}
