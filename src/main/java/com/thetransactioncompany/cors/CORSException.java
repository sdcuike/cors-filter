package com.thetransactioncompany.cors;


import javax.servlet.http.HttpServletResponse;


/**
 * Base Cross-Origin Resource Sharing (CORS) exception, typically thrown during
 * processing of CORS requests.
 *
 * @author Vladimir Dzhuvinov
 */
public class CORSException extends Exception {


	/**
	 * CORS origin denied exception.
	 */
	public static final CORSException ORIGIN_DENIED =
		new CORSException("CORS origin denied", HttpServletResponse.SC_FORBIDDEN);


	/**
	 * Unsupported HTTP method.
	 */
	public static final CORSException UNSUPPORTED_METHOD =
		new CORSException("Unsupported HTTP method", HttpServletResponse.SC_METHOD_NOT_ALLOWED);


	/**
	 * Unsupported HTTP request header.
	 */
	public static final CORSException UNSUPPORTED_REQUEST_HEADER =
		new CORSException("Unsupported HTTP request header", HttpServletResponse.SC_FORBIDDEN);
	
	
	/**
	 * Invalid simple / actual request.
	 */
	public static final CORSException INVALID_ACTUAL_REQUEST =
		new CORSException("Invalid simple/actual CORS request", HttpServletResponse.SC_BAD_REQUEST);
	
	
	/**
	 * Invalid preflight request.
	 */
	public static final CORSException INVALID_PREFLIGHT_REQUEST =
		new CORSException("Invalid preflight CORS request", HttpServletResponse.SC_BAD_REQUEST);
	
	
	/**
	 * Missing Access-Control-Request-Method header.
	 */
	public static final CORSException MISSING_ACCESS_CONTROL_REQUEST_METHOD_HEADER =
		new CORSException("Invalid preflight CORS request: Missing Access-Control-Request-Method header", HttpServletResponse.SC_BAD_REQUEST);
	
	
	/**
	 * Invalid request header value.
	 */
	public static final CORSException INVALID_HEADER_VALUE =
		new CORSException("Invalid preflight CORS request: Bad request header value", HttpServletResponse.SC_BAD_REQUEST);


	/**
	 * Generic HTTP requests not allowed.
	 */
	public static final CORSException GENERIC_HTTP_NOT_ALLOWED =
		new CORSException("Generic HTTP requests not allowed", HttpServletResponse.SC_FORBIDDEN);

	/**
	 * The HTTP status code, zero if not specified.
	 */
	private final int httpStatusCode;


	/**
	 * Creates a new CORS exception with the specified message and
	 * associated HTTP status code.
	 *
	 * @param message        The message.
	 * @param httpStatusCode The HTTP status code, zero if not specified.
	 */
	private CORSException(final String message, final int httpStatusCode) {

		super(message);
		this.httpStatusCode = httpStatusCode;
	}


	/**
	 * Returns the associated HTTP status code.
	 *
	 * @return The HTTP status code, zero if not specified.
	 */
	public int getHTTPStatusCode() {

		return httpStatusCode;
	}
}
