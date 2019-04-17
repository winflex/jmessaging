package messaging.example;

import java.io.Serializable;

/**
 * 
 * @author winflex
 */
public class AddRequest implements Serializable {
	private static final long serialVersionUID = 3848094754327316874L;
	private int op1;
	private int op2;

	public AddRequest() {
	}

	public AddRequest(int op1, int op2) {
		super();
		this.op1 = op1;
		this.op2 = op2;
	}

	public int getOp1() {
		return op1;
	}

	public void setOp1(int op1) {
		this.op1 = op1;
	}

	public int getOp2() {
		return op2;
	}

	public void setOp2(int op2) {
		this.op2 = op2;
	}
}
