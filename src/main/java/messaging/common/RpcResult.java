/**
 * 
 */
package messaging.common;

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

	public boolean isSuccess() {
		return cause == null;
	}
}
