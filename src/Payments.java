public class Payments {

    private String paymentID;
    private String rentalID;
    private int amount;
    private String method;
    private String paymentDate;

    public Payments(String paymentID, String rentalID, int amount, String method, String paymentDate) {
        this.paymentID = paymentID;
        this.rentalID = rentalID;
        this.amount = amount;
        this.method = method;
        this.paymentDate = paymentDate;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public String getRentalID() {
        return rentalID;
    }

    public int getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getPaymentDate() {
        return paymentDate;
    }
}