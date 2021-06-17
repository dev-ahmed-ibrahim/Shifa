package android.android.shifa.Models;

public class LocationModel {
    String nfc_id, text, name, emergency, bloodtype, disease, notes, latitude, longitude;

    public LocationModel() {
    }


    public LocationModel(String nfc_id, String text, String name, String emergency, String bloodtype, String disease, String notes, String latitude, String longitude) {
        this.nfc_id = nfc_id;
        this.text = text;
        this.name = name;
        this.emergency = emergency;
        this.bloodtype = bloodtype;
        this.disease = disease;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNfc_id() {
        return nfc_id;
    }

    public void setNfc_id(String nfc_id) {
        this.nfc_id = nfc_id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmergency() {
        return emergency;
    }

    public void setEmergency(String emergency) {
        this.emergency = emergency;
    }

    public String getBloodtype() {
        return bloodtype;
    }

    public void setBloodtype(String bloodtype) {
        this.bloodtype = bloodtype;
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
