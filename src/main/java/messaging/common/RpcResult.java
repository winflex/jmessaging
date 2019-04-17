/**
 * 
 */
package messaging.common;

import java.io.Serializable;

/**
 *
 * @author winflex
 */
public class RpcResult implements Serializable {

	public static final RpcResult newSuccessResult(Object data) {
		RpcResult result = new RpcResult();
		result.setResult(data);
		return result;
	}

	public static final RpcResult newFailureResult(Throwable cause) {
		RpcResult result = new RpcResult();
		result.setCause(cause);
		return result;
	}

	private static final long serialVersionUID = 5849606699245716833L;

	private Object result;

	private Throwable cause;

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public boolean isSuccess() {
		return cause == null;
	}
}
