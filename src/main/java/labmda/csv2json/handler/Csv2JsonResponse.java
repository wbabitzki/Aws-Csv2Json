package labmda.csv2json.handler;

public class Csv2JsonResponse {
    private Integer statusCode;
    private String body;

    static class Csv2JsonBuilder extends Csv2JsonResponse {
        public Csv2JsonBuilder statusCode(Integer statusCode) {
            setStatusCode(statusCode);
            return this;
        }

        public Csv2JsonBuilder body(byte[] body) {
            setBody(new String(body));
            return this;
        }

        public Csv2JsonResponse build() {
            Csv2JsonResponse response = new Csv2JsonResponse();
            response.setStatusCode(getStatusCode());
            response.setBody(getBody());
            return response;
        }
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
