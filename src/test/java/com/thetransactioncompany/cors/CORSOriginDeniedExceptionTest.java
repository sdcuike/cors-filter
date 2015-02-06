package com.thetransactioncompany.cors;


import junit.framework.TestCase;


/**
 * Tests the CORS origin denied exception.
 */
public class CORSOriginDeniedExceptionTest extends TestCase {


	public void testWithValidOrigin() {

		Origin origin = new Origin("http://example.com");

		CORSOriginDeniedException e = new CORSOriginDeniedException("Origin denied", origin);

		assertEquals("Origin denied", e.getMessage());
		assertEquals("http://example.com", e.getRequestOrigin().toString());
	}


	public void testWithNullOrigin() {

		CORSOriginDeniedException e = new CORSOriginDeniedException("Origin denied", null);

		assertEquals("Origin denied", e.getMessage());
		assertNull(e.getRequestOrigin());
	}


	public void testWithInValidOrigin() {

		Origin origin = new Origin("{}");

		CORSOriginDeniedException e = new CORSOriginDeniedException("Origin denied", origin);

		assertEquals("Origin denied", e.getMessage());
		assertNull(e.getRequestOrigin());
	}
}
