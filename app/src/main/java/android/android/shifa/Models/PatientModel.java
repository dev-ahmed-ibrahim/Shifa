package android.android.shifa.Models;

public class PatientModel {
    private String patient_user_id, fullname, email, personal_ID, NFC_ID, birthdate, close_mobile_number, mobilenumber, address, imageurl, imageurl2, Medical_diagnosis, pharmaceutical;
    private int bloodtypes;

    public PatientModel() {
    }

    public PatientModel(String patient_user_id, String fullname, String email, String personal_ID, String NFC_ID, String birthdate, String close_mobile_number, String mobilenumber, String address, String imageurl, String imageurl2, String medical_diagnosis, String pharmaceutical, int bloodtypes) {
        this.patient_user_id = patient_user_id;
        this.fullname = fullname;
        this.email = email;
        this.personal_ID = personal_ID;
        this.NFC_ID = NFC_ID;
        this.birthdate = birthdate;
        this.close_mobile_number = close_mobile_number;
        this.mobilenumber = mobilenumber;
        this.address = address;
        this.imageurl = imageurl;
        this.imageurl2 = imageurl2;
        Medical_diagnosis = medical_diagnosis;
        this.pharmaceutical = pharmaceutical;
        this.bloodtypes = bloodtypes;
    }


    public String getImageurl2() {
        return imageurl2;
    }

    public void setImageurl2(String imageurl2) {
        this.imageurl2 = imageurl2;
    }

    public String getPatient_user_id() {
        return patient_user_id;
    }

    public void setPatient_user_id(String patient_user_id) {
        this.patient_user_id = patient_user_id;
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

    public String getPersonal_ID() {
        return personal_ID;
    }

    public void setPersonal_ID(String personal_ID) {
        this.personal_ID = personal_ID;
    }

    public String getNFC_ID() {
        return NFC_ID;
    }

    public void setNFC_ID(String NFC_ID) {
        this.NFC_ID = NFC_ID;
    }

    public String getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(String birthdate) {
        this.birthdate = birthdate;
    }

    public String getClose_mobile_number() {
        return close_mobile_number;
    }

    public void setClose_mobile_number(String close_mobile_number) {
        this.close_mobile_number = close_mobile_number;
    }

    public String getMobilenumber() {
        return mobilenumber;
    }

    public void setMobilenumber(String mobilenumber) {
        this.mobilenumber = mobilenumber;
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

    public String getMedical_diagnosis() {
        return Medical_diagnosis;
    }

    public void setMedical_diagnosis(String medical_diagnosis) {
        Medical_diagnosis = medical_diagnosis;
    }

    public String getPharmaceutical() {
        return pharmaceutical;
    }

    public void setPharmaceutical(String pharmaceutical) {
        this.pharmaceutical = pharmaceutical;
    }

    public int getBloodtypes() {
        return bloodtypes;
    }

    public void setBloodtypes(int bloodtypes) {
        this.bloodtypes = bloodtypes;
    }
}
