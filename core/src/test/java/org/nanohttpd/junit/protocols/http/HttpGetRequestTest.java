package org.nanohttpd.junit.protocols.http;

/*
 * #%L
 * NanoHttpd-Core
 * %%
 * Copyright (C) 2012 - 2015 nanohttpd
 * %%
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the nanohttpd nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.nanohttpd.protocols.http.request.Method;
import org.nanohttpd.protocols.http.response.Response;

public class HttpGetRequestTest extends HttpServerTest {

    @Test
    public void testDecodingFieldWithEmptyValueAndFieldWithMissingValueGiveDifferentResults() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo&bar= HTTP/1.1");
        assertTrue(this.testServer.decodedParamters.get("foo") instanceof List);
        assertEquals(0, this.testServer.decodedParamters.get("foo").size());
        assertTrue(this.testServer.decodedParamters.get("bar") instanceof List);
        assertEquals(1, this.testServer.decodedParamters.get("bar").size());
        assertEquals("", this.testServer.decodedParamters.get("bar").get(0));
    }

    @Test
    public void testDecodingMixtureOfParameters() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar&foo=baz&zot&zim= HTTP/1.1");
        assertTrue(this.testServer.decodedParamters.get("foo") instanceof List);
        assertEquals(2, this.testServer.decodedParamters.get("foo").size());
        assertEquals("bar", this.testServer.decodedParamters.get("foo").get(0));
        assertEquals("baz", this.testServer.decodedParamters.get("foo").get(1));
        assertTrue(this.testServer.decodedParamters.get("zot") instanceof List);
        assertEquals(0, this.testServer.decodedParamters.get("zot").size());
        assertTrue(this.testServer.decodedParamters.get("zim") instanceof List);
        assertEquals(1, this.testServer.decodedParamters.get("zim").size());
        assertEquals("", this.testServer.decodedParamters.get("zim").get(0));
    }

    @Test
    public void testDecodingParametersFromParameterMap() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar&foo=baz&zot&zim= HTTP/1.1");
        assertEquals(this.testServer.decodedParamters, this.testServer.decodedParamtersFromParameter);
    }

    // --------------------------------------------------------------------------------------------------------
    // //

    @Test
    public void testDecodingParametersWithSingleValue() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar&baz=zot HTTP/1.1");
        assertEquals("foo=bar&baz=zot", this.testServer.queryParameterString);
        assertTrue(this.testServer.decodedParamters.get("foo") instanceof List);
        assertEquals(1, this.testServer.decodedParamters.get("foo").size());
        assertEquals("bar", this.testServer.decodedParamters.get("foo").get(0));
        assertTrue(this.testServer.decodedParamters.get("baz") instanceof List);
        assertEquals(1, this.testServer.decodedParamters.get("baz").size());
        assertEquals("zot", this.testServer.decodedParamters.get("baz").get(0));
    }

    @Test
    public void testDecodingParametersWithSingleValueAndMissingValue() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo&baz=zot HTTP/1.1");
        assertEquals("foo&baz=zot", this.testServer.queryParameterString);
        assertTrue(this.testServer.decodedParamters.get("foo") instanceof List);
        assertEquals(0, this.testServer.decodedParamters.get("foo").size());
        assertTrue(this.testServer.decodedParamters.get("baz") instanceof List);
        assertEquals(1, this.testServer.decodedParamters.get("baz").size());
        assertEquals("zot", this.testServer.decodedParamters.get("baz").get(0));
    }

    @Test
    public void testDecodingSingleFieldRepeated() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar&foo=baz HTTP/1.1");
        assertTrue(this.testServer.decodedParamters.get("foo") instanceof List);
        assertEquals(2, this.testServer.decodedParamters.get("foo").size());
        assertEquals("bar", this.testServer.decodedParamters.get("foo").get(0));
        assertEquals("baz", this.testServer.decodedParamters.get("foo").get(1));
    }

    @Test
    public void testEmptyHeadersSuppliedToServeMethodFromSimpleWorkingGetRequest() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + " HTTP/1.1");
        assertNotNull(this.testServer.parameters);
        assertNotNull(this.testServer.header);
        assertNotNull(this.testServer.files);
        assertNotNull(this.testServer.uri);
    }

    @Test
    public void testFullyQualifiedWorkingGetRequest() throws Exception {
        ByteArrayOutputStream outputStream = invokeServer("GET " + HttpServerTest.URI + " HTTP/1.1");

        String[] expected = {
            "HTTP/1.1 200 OK",
            "Content-Type: text/html",
            "Date: .*",
            "Connection: keep-alive",
            "Content-Length: 0",
            ""
        };

        assertResponse(outputStream, expected);
    }

    @Test
    public void testMultipleGetParameters() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar&baz=zot HTTP/1.1");
        assertEquals("bar", this.testServer.parameters.get("foo").get(0));
        assertEquals("zot", this.testServer.parameters.get("baz").get(0));
    }

    @Test
    public void testMultipleGetParametersWithMissingValue() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=&baz=zot HTTP/1.1");
        assertEquals("", this.testServer.parameters.get("foo").get(0));
        assertEquals("zot", this.testServer.parameters.get("baz").get(0));
    }

    @Test
    public void testMultipleGetParametersWithMissingValueAndRequestHeaders() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=&baz=zot HTTP/1.1\nAccept: text/html");
        assertEquals("", this.testServer.parameters.get("foo").get(0));
        assertEquals("zot", this.testServer.parameters.get("baz").get(0));
        assertEquals("text/html", this.testServer.header.get("accept"));
    }

    @Test
    public void testMultipleHeaderSuppliedToServeMethodFromSimpleWorkingGetRequest() throws IOException {
        String userAgent = "jUnit 4.8.2 Unit Test";
        String accept = "text/html";
        invokeServer("GET " + HttpServerTest.URI + " HTTP/1.1\nUser-Agent: " + userAgent + "\nAccept: " + accept);
        assertEquals(userAgent, this.testServer.header.get("user-agent"));
        assertEquals(accept, this.testServer.header.get("accept"));
    }

    @Test
    public void testOutputOfServeSentBackToClient() throws Exception {
        String responseBody = "Success!";
        this.testServer.response = Response.newFixedLengthResponse(responseBody);
        ByteArrayOutputStream outputStream = invokeServer("GET " + HttpServerTest.URI + " HTTP/1.1");

        String[] expected = {
            "HTTP/1.1 200 OK",
            "Content-Type: text/html",
            "Date: .*",
            "Connection: keep-alive",
            "Content-Length: 8",
            "",
            responseBody
        };

        assertResponse(outputStream, expected);
    }

    @Test
    public void testSingleGetParameter() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar HTTP/1.1");
        assertEquals("bar", this.testServer.parameters.get("foo").get(0));
    }

    @Test
    public void testMultipleValueGetParameter() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar&foo=baz HTTP/1.1");
        assertEquals(2, this.testServer.parameters.get("foo").size());
        assertEquals("bar", this.testServer.parameters.get("foo").get(0));
        assertEquals("baz", this.testServer.parameters.get("foo").get(1));
    }

    @Test
    public void testSingleGetParameterWithNoValue() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo HTTP/1.1");
        assertEquals("", this.testServer.parameters.get("foo").get(0));
    }

    @Test
    public void testSingleUserAgentHeaderSuppliedToServeMethodFromSimpleWorkingGetRequest() throws IOException {
        String userAgent = "jUnit 4.8.2 Unit Test";
        invokeServer("GET " + HttpServerTest.URI + " HTTP/1.1\nUser-Agent: " + userAgent + "\n");
        assertEquals(userAgent, this.testServer.header.get("user-agent"));
        assertEquals(Method.GET, this.testServer.method);
        assertEquals(HttpServerTest.URI, this.testServer.uri);
    }

    @Test
    public void testGetQueryParameterContainsSpace() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar%20baz HTTP/1.1");
        assertEquals("Parameter count in URL and decodedParameters should match.", 1, this.testServer.decodedParamters.size());
        assertEquals("The query parameter value with space decoding incorrect", "bar baz", this.testServer.decodedParamters.get("foo").get(0));
    }

    @Test
    public void testGetQueryParameterContainsQuestionMark() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar%3F HTTP/1.1");
        assertEquals("Parameter count in URL and decodedParameters should match.", 1, this.testServer.decodedParamters.size());
        assertEquals("The query parameter value with question mark decoding incorrect", "bar?", this.testServer.decodedParamters.get("foo").get(0));
    }

    @Test
    public void testGetQueryParameterContainsAmpersand() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar%26 HTTP/1.1");
        assertEquals("Parameter count in URL and decodedParameters should match.", 1, this.testServer.decodedParamters.size());
        assertEquals("The query parameter value with ampersand decoding incorrect", "bar&", this.testServer.decodedParamters.get("foo").get(0));
    }

    @Test
    public void testGetQueryParameterContainsSpecialCharactersSingleFieldRepeated() throws IOException {
        invokeServer("GET " + HttpServerTest.URI + "?foo=bar%20baz&foo=bar%3F&foo=bar%26 HTTP/1.1");
        assertEquals("Parameter count in URL and decodedParameters should match.", 1, this.testServer.decodedParamters.size());
        String[] parametersAsArray = this.testServer.decodedParamters.get("foo").toArray(new String[0]);
        String[] expected = new String[]{
            "bar baz",
            "bar?",
            "bar&"
        };
        assertArrayEquals("Repeated parameter not decoded correctly", expected, parametersAsArray);
    }
}
