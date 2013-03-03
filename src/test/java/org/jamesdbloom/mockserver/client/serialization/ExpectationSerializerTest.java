package org.jamesdbloom.mockserver.client.serialization;

import org.codehaus.jackson.map.ObjectMapper;
import org.eclipse.jetty.http.HttpStatus;
import org.jamesdbloom.mockserver.client.serialization.model.*;
import org.jamesdbloom.mockserver.matchers.Times;
import org.jamesdbloom.mockserver.mock.Expectation;
import org.jamesdbloom.mockserver.model.*;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author jamesdbloom
 */
public class ExpectationSerializerTest {

    private final Expectation fullExpectation = new Expectation(
            new HttpRequest()
                    .withMethod("GET")
                    .withPath("somepath")
                    .withBody("somebody")
                    .withHeaders(new Header("headerName", "headerValue"))
                    .withCookies(new Cookie("cookieName", "cookieValue"))
                    .withQueryParameters(new Parameter("queryParameterName", "queryParameterValue"))
                    .withBodyParameters(new Parameter("bodyParameterName", "bodyParameterValue")),
            Times.once()
    ).respond(new HttpResponse()
            .withStatusCode(HttpStatus.NOT_MODIFIED_304)
            .withBody("somebody")
            .withHeaders(new Header("headerName", "headerValue"))
            .withCookies(new Cookie("cookieName", "cookieValue"))
            .withDelay(new Delay(TimeUnit.MICROSECONDS, 1)));
    private final ExpectationDTO fullExpectationDTO = new ExpectationDTO()
            .setHttpRequest(
                    new HttpRequestDTO()
                            .setMethod("GET")
                            .setPath("somepath")
                            .setBody("somebody")
                            .setHeaders(Arrays.<HeaderDTO>asList((HeaderDTO) new HeaderDTO(new Header("headerName", Arrays.asList("headerValue")))))
                            .setCookies(Arrays.<CookieDTO>asList((CookieDTO) new CookieDTO(new Cookie("cookieName", Arrays.asList("cookieValue")))))
                            .setQueryParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("queryParameterName", Arrays.asList("queryParameterValue")))))
                            .setBodyParameters(Arrays.<ParameterDTO>asList((ParameterDTO) new ParameterDTO(new Parameter("bodyParameterName", Arrays.asList("bodyParameterValue"))))))
            .setHttpResponse(
                    new HttpResponseDTO()
                            .setStatusCode(HttpStatus.NOT_MODIFIED_304)
                            .setBody("somebody")
                            .setHeaders(Arrays.<HeaderDTO>asList((HeaderDTO) new HeaderDTO(new Header("headerName", Arrays.asList("headerValue")))))
                            .setCookies(Arrays.<CookieDTO>asList((CookieDTO) new CookieDTO(new Cookie("cookieName", Arrays.asList("cookieValue")))))
                            .setDelay(
                                    new DelayDTO()
                                            .setTimeUnit(TimeUnit.MICROSECONDS)
                                            .setValue(1)))
            .setTimes(new TimesDTO(Times.once()));

    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private ExpectationSerializer expectationSerializer;

    @Before
    public void setupTestFixture() {
        expectationSerializer = spy(new ExpectationSerializer());

        initMocks(this);
    }

    @Test
    public void deserialize() throws IOException {
        // given
        InputStream inputStream = mock(InputStream.class);
        when(objectMapper.readValue(same(inputStream), same(ExpectationDTO.class))).thenReturn(fullExpectationDTO);

        // when
        Expectation expectation = expectationSerializer.deserialize(inputStream);

        // then
        assertEquals(fullExpectation, expectation);
    }

    @Test(expected = RuntimeException.class)
    public void deserializeHandlesException() throws IOException {
        // given
        when(objectMapper.readValue(any(InputStream.class), same(ExpectationDTO.class))).thenThrow(new IOException());
        // InputStream inputStream = new ByteArrayInputStream(s.getBytes(charset))

        // when
        expectationSerializer.deserialize(mock(InputStream.class));
    }

    @Test
    public void serialize() throws IOException {
        // when
        expectationSerializer.serialize(fullExpectation);

        // then
        verify(objectMapper).writeValueAsString(fullExpectationDTO);
    }

    @Test(expected = RuntimeException.class)
    public void serializeHandlesException() throws IOException {
        // given
        Expectation expectation = mock(Expectation.class);
        when(objectMapper.writeValueAsString(any(Map.class))).thenThrow(new IOException());

        // when
        expectationSerializer.serialize(expectation);
    }
}
