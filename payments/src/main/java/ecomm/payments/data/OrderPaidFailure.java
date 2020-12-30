package ecomm.payments.data;

import java.util.Map;

public class OrderPaidFailure {
    Long timeStamp;
    Map<String,String[]> params;

    @Override
    public String toString() {
        return "OrderPaidFailure{" +
                "timeStamp=" + timeStamp +
                ", params=" + params +
                '}';
    }

    public Map<String, String[]> getParams() {
        return params;
    }

    public void setParams(Map<String, String[]> params) {
        this.params = params;
    }

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }


}
