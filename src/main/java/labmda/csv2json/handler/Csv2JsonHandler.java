package labmda.csv2json.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.google.gson.Gson;
import org.apache.commons.fileupload.MultipartStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

public class Csv2JsonHandler implements RequestStreamHandler {
    static final String KEY_BODY = "body";
    static final String KEY_CONTENT_TYPE = "Content-Type";
    static final String KEY_HEADERS = "headers";
    static final String KEY_BOUNDARY = "boundary";

    static final String ERROR_BODY_MISSING = "Element '" + KEY_BODY + "' is missing in the request";
    static final String ERROR_BODY_INVALID = "Invalid body, cannot be processed as base64 encoded multipart";
    static final String ERROR_CONTENT_TYPE_MISSING = "Element '" + KEY_CONTENT_TYPE + "' is missing in the request";
    static final String ERROR_HEADERS_MISSING = "Element '" + KEY_HEADERS + "' is missing in the request";
    static final String ERROR_CONTAIN_TYPE_INVALID_PARAMETER_NUMBER = "'" + KEY_CONTENT_TYPE + "' must contain two parameters";
    static final String ERROR_ILLEGAL_BOUNDARY_PARAMETER = "Illegal or missing parameter '" + "BOUNDARY" + "'";

    private final static Gson gson = new Gson();
    private final Base64.Decoder Base64Decoder = Base64.getDecoder();

    private Csv2JsonConverter converter = new Csv2JsonConverter();

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        Map<String, Object> map = gson.fromJson(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8), Map.class);
        context.getLogger().log("Called Csv2JsonHandler:handleRequest");
        validateBody(map);
        validateHeaders(map);
        validateContentType(map);

        final byte[] body = readBody(map);
        final String json = getConverter().toJson(body, ',');

        context.getLogger().log("json: " + json);

        Csv2JsonResponse response = new Csv2JsonResponse.Csv2JsonBuilder()
                .statusCode(200)
                .body(json.getBytes())
                .build();

        gson.toJson(response, Csv2JsonResponse.class);
        outputStream.write(gson.toJson(response, Csv2JsonResponse.class).getBytes());
    }

    private void validateBody(Map<String, Object> map) {
        if(!map.containsKey(KEY_BODY)) {
            throw new IllegalArgumentException(ERROR_BODY_MISSING);
        }
    }

    private void validateHeaders(Map<String, Object> map) {
        if(!map.containsKey(KEY_HEADERS)) {
            throw new IllegalArgumentException(ERROR_HEADERS_MISSING);
        }
    }

    private void validateContentType(Map<String, Object> map) {
        Map<String, Map> headers = (Map<String, Map>) map.get(KEY_HEADERS);
        if(!headers.containsKey(KEY_CONTENT_TYPE)) {
            throw new IllegalArgumentException(ERROR_CONTENT_TYPE_MISSING);
        }
    }

    private byte[] getBoundary(Map<String, Object> map) {
        Map<String, String> headers = (Map<String, String>) map.get(KEY_HEADERS);
        String[] contentTypeValues = headers.get(KEY_CONTENT_TYPE).split(";");
        validateContainType(contentTypeValues);
        String[] boundaryArray = contentTypeValues[1].trim().split("=");
        validateBoundary(boundaryArray);
        return boundaryArray[1].getBytes();
    }

    private void validateBoundary(String[] boundaryArray) {
        if (boundaryArray.length != 2 && KEY_BOUNDARY.compareToIgnoreCase(boundaryArray[0]) != 0) {
            throw new IllegalArgumentException(ERROR_ILLEGAL_BOUNDARY_PARAMETER);
        }
    }

    private void validateContainType(String[] contentTypeValues) {
        if (contentTypeValues.length != 2) {
            throw new IllegalArgumentException(ERROR_CONTAIN_TYPE_INVALID_PARAMETER_NUMBER);
        }
    }

    private byte[] readBody(Map<String, Object> map) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] body = Base64Decoder.decode(map.get(KEY_BODY).toString().getBytes());
        byte[] boundary = getBoundary(map);
        try {
            MultipartStream multipartStream = new MultipartStream(new ByteArrayInputStream(body), boundary, body.length, null);
            boolean nextPart = multipartStream.skipPreamble();
            while (nextPart) {
                multipartStream.readHeaders();
                multipartStream.readBodyData(out);
                nextPart = multipartStream.readBoundary();
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(ERROR_BODY_INVALID);
        }
        return out.toByteArray();
    }

    public Csv2JsonConverter getConverter() {
        return converter;
    }

    public void setConverter(Csv2JsonConverter converter) {
        this.converter = converter;
    }

}
