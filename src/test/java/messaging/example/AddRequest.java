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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + op1;
		result = prime * result + op2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AddRequest other = (AddRequest) obj;
		if (op1 != other.op1)
			return false;
		if (op2 != other.op2)
			return false;
		return true;
	}
	
}
