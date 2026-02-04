public class Rental {

    private String rentalID;
    private String rentDate; // format: YYYY-MM-DD
    private String returnDate; // format: YYYY-MM-DD
    private int totalPrice;
    private String status; // active or returned
    private String paymentStatus; // paid or unpaid
    private int userID;

    private int carIDFixed; // FK to cars.cardIDFixed (INT)
    private String carID; // real car ID displayed in UI (VARCHAR)

    public Rental(
            String rentalID,
            String rentDate,
            String returnDate,
            int totalPrice,
            String status,
            String paymentStatus,
            int userID,
            int carIDFixed,
            String carID
    ) {
        this.rentalID = rentalID;
        this.rentDate = rentDate;
        this.returnDate = returnDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.userID = userID;
        this.carIDFixed = carIDFixed;
        this.carID = carID;
    }

    public String getRentalID() {
        return rentalID;
    }

    public void setRentalID(String rentalID) {
        this.rentalID = rentalID;
    }

    public String getRentDate() {
        return rentDate;
    }

    public void setRentDate(String rentDate) {
        this.rentDate = rentDate;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public int getCarIDFixed() {
        return carIDFixed;
    }

    public void setCarIDFixed(int carIDFixed) {
        this.carIDFixed = carIDFixed;
    }

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }
}