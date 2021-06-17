package android.android.shifa.AdminApp;

import android.android.shifa.AdminApp.AdminFragments.PatientsFragment;
import android.android.shifa.AdminApp.AdminFragments.RequestsFragment;
import android.android.shifa.Models.PatientModel;
import android.android.shifa.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

public class AdminPatientsDetailsActivity extends AppCompatActivity {
    String PATIENT_KEY;
    String NFC_KEY;

    CardView scan_card;

    ImageView profilepicture, callmobile, datepick;
    TextView fullname_txt, nfcid_txt;
    EditText email_field, fullname_field, mobile_field, closemobile_field, address_field, nfcid_field, personalid_field, birthday_field, medicaldiagnosis_field, pharmaceutical_field;
    Spinner bloodtypes;
    String mobile, profile_image_url, NFC;

    RotateLoading rotateLoading;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_patients_details);

        PATIENT_KEY = getIntent().getStringExtra(PatientsFragment.ADMIN_EXTRA_PATIENT_KEY);
        NFC_KEY = getIntent().getStringExtra(RequestsFragment.ADMIN_EXTRA_REQUEST_KEY);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        init();

        rotateLoading.start();

        if (!TextUtils.isEmpty(NFC_KEY)) {
            returnNFCdata(NFC_KEY);
        } else {
            returndata(PATIENT_KEY);
        }
        callmobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialContactPhone(mobile);
            }
        });
    }

    public void init() {
        scan_card = findViewById(R.id.card_scan);

        fullname_txt = findViewById(R.id.fullname_txt);
        nfcid_txt = findViewById(R.id.nfcid_txt);

        profilepicture = findViewById(R.id.patient_profile_picture);
        callmobile = findViewById(R.id.phonenumber_btn);
        datepick = findViewById(R.id.date_picker);

        email_field = findViewById(R.id.email_field);
        fullname_field = findViewById(R.id.fullname_field);
        mobile_field = findViewById(R.id.mobile_field);
        closemobile_field = findViewById(R.id.closest_mobile_field);
        address_field = findViewById(R.id.address_field);
        nfcid_field = findViewById(R.id.nfc_id_field);
        personalid_field = findViewById(R.id.personal_id_field);
        birthday_field = findViewById(R.id.datebirth_field);
        medicaldiagnosis_field = findViewById(R.id.medical_diagnosis_field);
        pharmaceutical_field = findViewById(R.id.pharmaceutical_field);

        bloodtypes = findViewById(R.id.blood_spinner);
        rotateLoading = findViewById(R.id.rotateloading);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getApplicationContext(),
                R.array.bloodtypes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        bloodtypes.setAdapter(adapter1);

        bloodtypes.setEnabled(false);
        email_field.setEnabled(false);
        fullname_field.setEnabled(false);
        mobile_field.setEnabled(false);
        closemobile_field.setEnabled(false);
        address_field.setEnabled(false);
        nfcid_field.setEnabled(false);
        personalid_field.setEnabled(false);
        birthday_field.setEnabled(false);
        datepick.setEnabled(false);
        medicaldiagnosis_field.setEnabled(false);
        pharmaceutical_field.setEnabled(false);
        profilepicture.setEnabled(false);
    }

    public void returndata(String patient_key) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("AllUsers").child("Patients").child(patient_key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);

                        if (patientModel != null) {
                            NFC = patientModel.getNFC_ID();

                            fullname_txt.setText(patientModel.getFullname());
                            nfcid_txt.setText(patientModel.getNFC_ID());
                            mobile = patientModel.getMobilenumber();
                            email_field.setText(patientModel.getEmail());
                            fullname_field.setText(patientModel.getFullname());
                            mobile_field.setText(patientModel.getMobilenumber());
                            closemobile_field.setText(patientModel.getClose_mobile_number());
                            address_field.setText(patientModel.getAddress());
                            nfcid_field.setText(NFC);
                            personalid_field.setText(patientModel.getPersonal_ID());
                            birthday_field.setText(patientModel.getBirthdate());
                            bloodtypes.setSelection(patientModel.getBloodtypes());

                            medicaldiagnosis_field.setText(patientModel.getMedical_diagnosis());
                            pharmaceutical_field.setText(patientModel.getPharmaceutical());

                            profile_image_url = patientModel.getImageurl();


                            Picasso.get()
                                    .load(profile_image_url)
                                    .placeholder(R.drawable.patient2)
                                    .error(R.drawable.patient2)
                                    .into(profilepicture);

                            rotateLoading.stop();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    public void returnNFCdata(String nfc) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("Patients").child(nfc).child(nfc).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);

                        if (patientModel != null) {
                            NFC = patientModel.getNFC_ID();

                            fullname_txt.setText(patientModel.getFullname());
                            nfcid_txt.setText(patientModel.getNFC_ID());
                            mobile = patientModel.getMobilenumber();
                            email_field.setText(patientModel.getEmail());
                            fullname_field.setText(patientModel.getFullname());
                            mobile_field.setText(patientModel.getMobilenumber());
                            closemobile_field.setText(patientModel.getClose_mobile_number());
                            address_field.setText(patientModel.getAddress());
                            nfcid_field.setText(NFC);
                            personalid_field.setText(patientModel.getPersonal_ID());
                            birthday_field.setText(patientModel.getBirthdate());
                            bloodtypes.setSelection(patientModel.getBloodtypes());

                            medicaldiagnosis_field.setText(patientModel.getMedical_diagnosis());
                            pharmaceutical_field.setText(patientModel.getPharmaceutical());

                            profile_image_url = patientModel.getImageurl();


                            Picasso.get()
                                    .load(profile_image_url)
                                    .placeholder(R.drawable.patient2)
                                    .error(R.drawable.patient2)
                                    .into(profilepicture);

                            rotateLoading.stop();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }
}
