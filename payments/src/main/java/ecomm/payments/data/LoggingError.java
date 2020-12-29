package ecomm.payments.data;

public class LoggingError {
    private Long timestamp;
    private String sourceIp;
    private String service;
    private String request;
    private String error;

    @Override
    public String toString() {
        return "LoggingError{" +
                "timestamp=" + timestamp +
                ", sourceIp='" + sourceIp + '\'' +
                ", service='" + service + '\'' +
                ", request='" + request + '\'' +
                ", error='" + error + '\'' +
                '}';
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getSourceIp() {
        return sourceIp;
    }

    public void setSourceIp(String sourceIp) {
        this.sourceIp = sourceIp;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }




    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
