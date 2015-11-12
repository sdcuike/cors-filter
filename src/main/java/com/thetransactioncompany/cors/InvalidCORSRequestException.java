package com.thetransactioncompany.cors;


/**
 * Invalid CORS request exception. Thrown to indicate a CORS request (simple /
 * actual or preflight) that doesn't conform to the specification.
 *
 * @author Vladimir Dzhuvinov
 */
public class InvalidCORSRequestException extends CORSException {


	/**
	 * Invalid simple / actual request.
	 */
	public static final InvalidCORSRequestException INVALID_ACTUAL_REQUEST =
		new InvalidCORSRequestException("Invalid simple/actual CORS request");


	/**
	 * Invalid preflight request.
	 */
	public static final InvalidCORSRequestException INVALID_PREFLIGHT_REQUEST =
		new InvalidCORSRequestException("Invalid preflight CORS request");


	/**
	 * Missing Access-Control-Request-Method header.
	 */
	public static final InvalidCORSRequestException MISSING_ACCESS_CONTROL_REQUEST_METHOD_HEADER =
		new InvalidCORSRequestException("Invalid preflight CORS request: Missing Access-Control-Request-Method header");


	/**
	 * Invalid request header value.
	 */
	public static final InvalidCORSRequestException INVALID_HEADER_VALUE =
		new InvalidCORSRequestException("Invalid preflight CORS request: Bad request header value");

	
	/**
	 * Creates a new invalid CORS request exception with the specified 
	 * message.
	 *
	 * @param message The message.
	 */
	private InvalidCORSRequestException(final String message) {
	
		super(message);
	}
}
