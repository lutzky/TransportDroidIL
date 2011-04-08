package net.lutzky.transportdroidil;

@SuppressWarnings("serial")
public class InvalidServerResponseException extends Exception {
	public InvalidServerResponseException(String detailMessage) {
		super(detailMessage);
	}
}
