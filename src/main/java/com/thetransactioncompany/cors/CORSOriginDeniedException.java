package com.thetransactioncompany.cors;


/**
 * CORS origin denied (not allowed) exception.
 *
 * @author Vladimir Dzhuvinov
 */
public class CORSOriginDeniedException extends CORSException {

	
	/**
	 * The request origin.
	 */
	private final ValidatedOrigin requestOrigin;
	
	
	/**
	 * Creates a new CORS origin denied exception with the specified 
	 * message and request origins.
	 *
	 * @param message       The message.
	 * @param requestOrigin The request origin, {@code null} if unknown.
	 */
	public CORSOriginDeniedException(final String message, final Origin requestOrigin) {
	
		super(message);

		// Validate origin to prevent potential XSS as reported in
		// https://bitbucket.org/thetransactioncompany/cors-filter/issue/29/need-to-fix-xss-vulnerability-for-invalid

		ValidatedOrigin validatedOrigin = null;

		if (requestOrigin != null) {

			try {
				validatedOrigin = requestOrigin.validate();

			} catch (OriginException e) {
				// Invalid origin, don't record
			}
		}

		this.requestOrigin = validatedOrigin != null ? validatedOrigin : null;
	}
	
	
	/**
	 * Gets the validated request origin.
	 *
	 * @return The request origin, {@code null} if unknown, or if
	 *         validation of the origin string has failed.
	 */
	public ValidatedOrigin getRequestOrigin() {
	
		return requestOrigin;
	}
}
