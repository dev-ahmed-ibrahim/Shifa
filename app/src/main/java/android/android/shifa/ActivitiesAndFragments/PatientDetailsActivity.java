package android.android.shifa.ActivitiesAndFragments;

import android.android.shifa.Models.PatientModel;
import android.android.shifa.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

public class PatientDetailsActivity extends AppCompatActivity {
    public static String PATIENT_NFC_KEY = "patient_nfc_key";
    String PATIENT_KEY;
    String patient_nfc_scanned_id;
    /*public static String PATIENT_NAME_KEY = "patient_nfc_key";
    public static String PATIENT_EMERGENCY_KEY = "patient_nfc_key";
    public static String PATIENT_BLOODTYPE_KEY = "patient_nfc_key";*/
    String selected_bloodtype;

    Button edit_profile_btn, savechanges_btn, scan_nfc;

    CardView scan_card;

    ImageView profilepicture, callmobile, datepick;
    TextView fullname_txt, nfcid_txt;
    CardView save_card;
    EditText email_field, fullname_field, mobile_field, closemobile_field, address_field, nfcid_field, personalid_field, birthday_field, medicaldiagnosis_field, pharmaceutical_field;
    Spinner bloodtypes;
    String mobile, profile_image_url, profile_image_url2, NFC, user_id;
    String full_name_txt, email_txt, mobile_txt, address_txt, closest_txt, nfc_id_txt, personal_id_txt, date_txt;

    String medicaldiagnosis_txt, pharmaceutical_txt;

    RotateLoading rotateLoading;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patinet_details);

        PATIENT_KEY = getIntent().getStringExtra(PatientsFragment.EXTRA_PATIENT_KEY);
        patient_nfc_scanned_id = getIntent().getStringExtra(NFCActivity.NFC_PATIENT);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        init();

        rotateLoading.start();

        if (TextUtils.isEmpty(PATIENT_KEY)) {
            return_scanned_data(patient_nfc_scanned_id);
            scan_card.setVisibility(View.GONE);
        } else {
            returndata(PATIENT_KEY);
        }

        scan_nfc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nfc_id_txt = nfcid_field.getText().toString();

                Intent intent = new Intent(getApplicationContext(), NFCActivity.class);
                intent.putExtra(PATIENT_NFC_KEY, nfc_id_txt);
                startActivity(intent);
            }
        });

        callmobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialContactPhone(mobile);
            }
        });

        savechanges_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {
                full_name_txt = fullname_field.getText().toString();
                email_txt = email_field.getText().toString();
                mobile_txt = mobile_field.getText().toString();
                closest_txt = closemobile_field.getText().toString();
                address_txt = address_field.getText().toString();
                nfc_id_txt = nfcid_field.getText().toString();
                personal_id_txt = personalid_field.getText().toString();
                date_txt = birthday_field.getText().toString();

                medicaldiagnosis_txt = medicaldiagnosis_field.getText().toString();
                pharmaceutical_txt = pharmaceutical_field.getText().toString();

                if (TextUtils.isEmpty(medicaldiagnosis_txt)) {
                    Toast.makeText(getApplicationContext(), "please enter medical diagnosis", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(pharmaceutical_txt)) {
                    Toast.makeText(getApplicationContext(), "please enter pharmaceutical", Toast.LENGTH_SHORT).show();
                    return;
                }

                save_card.setVisibility(View.GONE);
                medicaldiagnosis_field.setEnabled(false);
                pharmaceutical_field.setEnabled(false);
                savechanges_btn.setEnabled(false);
                edit_profile_btn.setEnabled(true);

                UpdatePatientProfile(user_id, full_name_txt, email_txt, personal_id_txt, nfc_id_txt, date_txt, closest_txt, mobile_txt, bloodtypes.getSelectedItemPosition(), address_txt, profile_image_url, profile_image_url2, medicaldiagnosis_txt, pharmaceutical_txt);
            }
        });

        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_card.setVisibility(View.VISIBLE);
                medicaldiagnosis_field.setEnabled(true);
                pharmaceutical_field.setEnabled(true);
                savechanges_btn.setEnabled(true);
                edit_profile_btn.setEnabled(false);
            }
        });
    }

    public void init() {
        scan_card = findViewById(R.id.card_scan);

        edit_profile_btn = findViewById(R.id.edit_profile_btn);
        savechanges_btn = findViewById(R.id.savechanges_btn);
        scan_nfc = findViewById(R.id.scan_nfc);

        save_card = findViewById(R.id.save_card);

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

        save_card.setVisibility(View.GONE);
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
        savechanges_btn.setEnabled(false);
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
                            user_id = patientModel.getPatient_user_id();

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

    public void return_scanned_data(String patient_nfc) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("Patients").child(patient_nfc).child(patient_nfc).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);

                        if (patientModel != null) {
                            user_id = patientModel.getPatient_user_id();

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
                            profile_image_url2 = patientModel.getImageurl2();


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

    public void UpdatePatientProfile(String user_id, String fullname, String email, String personalid, String nfcid, String birthdate, String closemobile, String mobile, int bloodtype, String address, String imageurl, String imageurl2, String med, String pharm) {
        PatientModel patientModel = new PatientModel(user_id, fullname, email, personalid, nfcid, birthdate, closemobile, mobile, address, imageurl, imageurl2, med, pharm, bloodtype);

        databaseReference.child("Patients").child(NFC).child(NFC).setValue(patientModel);
        databaseReference.child("AllUsers").child("Patients").child(user_id).setValue(patientModel);

        if (TextUtils.isEmpty(PATIENT_KEY)) {
            return_scanned_data(patient_nfc_scanned_id);
        } else {
            returndata(PATIENT_KEY);
        }

        Toast.makeText(getApplicationContext(), "saved", Toast.LENGTH_SHORT).show();
    }

    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }
}
