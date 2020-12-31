package ecomm.payments.data;

public class OrderPaid {
    private String orderId;
    private String userId;
    private Float amountPaid;

    @Override
    public String toString() {
        return "OrderPaid{" +
                "orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", amountPaid=" + amountPaid +
                '}';
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Float getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Float amountPaid) {
        this.amountPaid = amountPaid;
    }
}
