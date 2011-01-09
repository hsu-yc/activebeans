package org.activebeans;

@SuppressWarnings("serial")
public class ActiveBeansException extends RuntimeException {

	public ActiveBeansException() {
		super();
	}

	public ActiveBeansException(String message, Throwable cause) {
		super(message, cause);
	}

	public ActiveBeansException(String message) {
		super(message);
	}

	public ActiveBeansException(Throwable cause) {
		super(cause);
	}

}
