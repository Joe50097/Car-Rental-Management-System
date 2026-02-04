public class Car {

    // hidden internal primary key from database (AUTO_INCREMENT)
    private int cardIDFixed;

    private String carID;
    private int price;
    private int year;
    private String plateNb;
    private String status;
    private String brand;
    private String model;

    // store image from mySQL
    private byte[] carImage;

    public Car(int cardIDFixed, String carID, int price, int year, String plateNb, String status,
               String brand, String model, byte[] carImage) {

        this.cardIDFixed = cardIDFixed; // internal PK
        this.carID = carID;
        this.price = price;
        this.year = year;
        this.plateNb = plateNb;
        this.status = status;
        this.brand = brand;
        this.model = model;
        this.carImage = carImage; // store image bytes
    }

    public int getCardIDFixed() {
        return cardIDFixed;
    }

    public String getCarID() {
        return carID;
    }

    public int getPrice() {
        return price;
    }

    public int getYear() {
        return year;
    }

    public String getPlateNb() {
        return plateNb;
    }

    public String getStatus() {
        return status;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public byte[] getCarImage() {
        return carImage;
    }

    public void setCardIDFixed(int cardIDFixed) {
        this.cardIDFixed = cardIDFixed;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void setPlateNb(String plateNb) {
        this.plateNb = plateNb;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setCarImage(byte[] carImage) {
        this.carImage = carImage;
    }
}