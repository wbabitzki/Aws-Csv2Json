package labmda.csv2json.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Csv2JsonHandlerTest {
    private Csv2JsonHandler testee = new Csv2JsonHandler();

    @Test
    public void handleRequest_missingBody_throwsException() {
        // arrange
        String json = "{'foo': 'woo'}";
        InputStream is =  new ByteArrayInputStream(json.getBytes());
        // act
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> testee.handleRequest(is, null, mockContext()));
        // assert
        assertEquals(Csv2JsonHandler.ERROR_BODY_MISSING, exception.getMessage());
    }

    @Test
    void handleRequest_missingHeaders_throwsException() {
        // arrange
        String json = "{'body': 'woo'}";
        InputStream is =  new ByteArrayInputStream(json.getBytes());
        // act
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> testee.handleRequest(is, null, mockContext()));
        // assert
        assertEquals(Csv2JsonHandler.ERROR_HEADERS_MISSING, exception.getMessage());
    }

    @Test
    void handleRequest_missingContentType_throwsException() {
        // arrange
        String json = "{" +
                "'body': 'test'," +
                "'headers': {'test': 'test'}" +
                "}";
        InputStream is =  new ByteArrayInputStream(json.getBytes());
        // act
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> testee.handleRequest(is, null, mockContext()));
        // assert
        assertEquals(Csv2JsonHandler.ERROR_CONTENT_TYPE_MISSING, exception.getMessage());
    }

    @Test
    void handleRequest_invalidContentType_throwsException() {
        // arrange
        String json = "{" +
                "'body': 'test'," +
                "'headers': {'Content-Type': 'invalid'}" +
                "}";
        InputStream is =  new ByteArrayInputStream(json.getBytes());
        // act
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> testee.handleRequest(is, null, mockContext()));
        // assert
        assertEquals(Csv2JsonHandler.ERROR_CONTAIN_TYPE_INVALID_PARAMETER_NUMBER, exception.getMessage());
    }

    @Test
    void handleRequest_missingBoundary_throwsException() {
        // arrange
        String json = "{" +
                "'body': 'test'," +
                "'headers': " +
                    "{'Content-Type': 'test;invalid'}" +
                "}";
        InputStream is =  new ByteArrayInputStream(json.getBytes());
        // act
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> testee.handleRequest(is, null, mockContext()));
        // assert
        assertEquals(Csv2JsonHandler.ERROR_ILLEGAL_BOUNDARY_PARAMETER, exception.getMessage());
    }

    @Test
    void handleRequest_notEncodedBody_throwsException() {
        // arrange
        String json = "{" +
                "'body': 'test'," +
                "'headers': " +
                    "{'Content-Type':" + "'test;boundary=--------------------------076431130545451175906685'}" +
                "}";
        InputStream is =  new ByteArrayInputStream(json.getBytes());
        // act
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> testee.handleRequest(is, null, mockContext()));
        // assert
        assertEquals(Csv2JsonHandler.ERROR_BODY_INVALID, exception.getMessage());
    }

    @Test
    void handleRequest_valid_writesToOutput() throws Exception {
        // arrange
        OutputStream output = mock(OutputStream.class);
        final Csv2JsonConverter converter = mock(Csv2JsonConverter.class);
        when(converter.toJson(any(byte[].class), eq(','))).thenReturn("Test");
        testee.setConverter(converter);
        // act
        testee.handleRequest(getClass().getClassLoader().getResourceAsStream("request.json"), output, mockContext());
        // assert
        verify(converter).toJson("Hello from File via multipart".getBytes(), ',');
    }

    private Context mockContext() {
        Context context = mock(Context.class);
        when(context.getLogger()).thenReturn(mock(LambdaLogger.class));
        return context;
    }
}