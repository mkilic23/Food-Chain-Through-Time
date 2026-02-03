package exceptions;

/**
 * Custom exception class thrown when a move violates game rules.
 * (e.g., Going out of grid bounds, entering a blocked path, etc.)
 */
public class InvalidMoveException extends Exception {
	
	private static final long serialVersionUID = 1L;

		public InvalidMoveException(String message) {
		super(message);
	}
}
