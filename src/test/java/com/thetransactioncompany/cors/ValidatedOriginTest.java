package com.thetransactioncompany.cors;


import junit.framework.TestCase;


/**
 * Tests the validated origin class.
 *
 * @author Vladimir Dzhuvinov
 * @author Jared Ottley
 */
public class ValidatedOriginTest extends TestCase {
	
	
	public void testHTTPOrigin() {
	
		String uri = "http://example.com";
		
		ValidatedOrigin o = null;
		
		try {
			o = new ValidatedOrigin(new Origin(uri));
		
		} catch (OriginException e) {
			fail(e.getMessage());
		}
		
		assertEquals(uri, o.toString());
		
		assertEquals("http", o.getScheme());
		assertEquals("example.com", o.getHost());
		assertEquals(-1, o.getPort());
		assertEquals("example.com", o.getSuffix());
	}


	public void testHTTPSOrigin() {
	
		String uri = "https://example.com";
		
		ValidatedOrigin o = null;
		
		try {
			o = new ValidatedOrigin(new Origin(uri));
		
		} catch (OriginException e) {
			fail(e.getMessage());
		}
		
		assertEquals(uri, o.toString());
		
		assertEquals("https", o.getScheme());
		assertEquals("example.com", o.getHost());
		assertEquals(-1, o.getPort());
		assertEquals("example.com", o.getSuffix());
	}


        public void testAPPOrigin() {
	        
            String uri = "app://example.com";
            
            ValidatedOrigin o = null;
            
            try {
                    o = new ValidatedOrigin(new Origin(uri));
            
            } catch (OriginException e) {
                    fail(e.getMessage());
            }
            
            assertEquals(uri, o.toString());
            
            assertEquals("app", o.getScheme());
            assertEquals("example.com", o.getHost());
            assertEquals(-1, o.getPort());
            assertEquals("example.com", o.getSuffix());
        }
	
	
	public void testIPAddressHost() {
	
		String uri = "http://192.168.0.1:8080";
		
		ValidatedOrigin o = null;
		
		try {
			o = new ValidatedOrigin(new Origin(uri));
		
		} catch (OriginException e) {
			
			fail(e.getMessage());
		}
		
		assertEquals(uri, o.toString());
		
		assertEquals("http", o.getScheme());
		assertEquals("192.168.0.1", o.getHost());
		assertEquals(8080, o.getPort());
		assertEquals("192.168.0.1:8080", o.getSuffix());
	}


	// See https://bitbucket.org/thetransactioncompany/cors-filter/issues/32/nullpointerexception-is-thrown-when-there
	public void testNullHost() {

		String uri = "http:///path/";

		ValidatedOrigin o;

		try {
			o = new ValidatedOrigin(new Origin(uri));
			fail();
		} catch (OriginException e) {
			assertEquals("Bad origin URI: Missing authority (host)", e.getMessage());
		}
	}
	

//      Path+query+fragment checking not implemented at present
//
// 	public void testOriginURIWithPath() {
// 	
// 		String uri = "https://LOCALHOST:8080/my-app/upload.php";
// 		
// 		try {
// 			new ValidatedOrigin(uri);
// 		
// 			fail("Failed to raise exception");
// 			
// 		} catch (OriginException e) {
// 			
// 			// ok
// 		}
// 	}
}
