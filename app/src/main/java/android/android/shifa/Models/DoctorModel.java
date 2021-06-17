package android.android.shifa.Models;

public class DoctorModel {
    private String fullname, email, mobilenumber, specialization, address, imageurl;

    public DoctorModel() {
    }

    public DoctorModel(String fullname, String email, String mobilenumber, String specialization, String address, String imageurl) {
        this.fullname = fullname;
        this.email = email;
        this.mobilenumber = mobilenumber;
        this.specialization = specialization;
        this.address = address;
        this.imageurl = imageurl;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
