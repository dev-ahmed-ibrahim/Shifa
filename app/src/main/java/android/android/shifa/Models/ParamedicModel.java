package android.android.shifa.Models;

public class ParamedicModel {
    private String fullname, email, mobilenumber, work_place, address, imageurl;

    public ParamedicModel() {
    }

    public ParamedicModel(String fullname, String email, String mobilenumber, String work_place, String address, String imageurl) {
        this.fullname = fullname;
        this.email = email;
        this.mobilenumber = mobilenumber;
        this.work_place = work_place;
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

    public String getWork_place() {
        return work_place;
    }

    public void setWork_place(String work_place) {
        this.work_place = work_place;
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
