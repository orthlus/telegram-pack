package main.bash.exceptions;

public class QuoteNotFoundException extends RuntimeException {
	public QuoteNotFoundException(String s) {
		super(s);
	}
}
