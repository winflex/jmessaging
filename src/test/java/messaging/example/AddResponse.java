package messaging.example;

import java.io.Serializable;

/**
 * 
 * @author winflex
 */
public class AddResponse implements Serializable {
	private static final long serialVersionUID = -4644759432114731809L;
	private int value;

	public AddResponse() {
	}

	public AddResponse(int value) {
		super();
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
