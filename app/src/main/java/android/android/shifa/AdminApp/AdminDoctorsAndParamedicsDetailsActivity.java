package android.android.shifa.AdminApp;

import android.android.shifa.AdminApp.AdminFragments.DoctorsFragment;
import android.android.shifa.AdminApp.AdminFragments.ParamedicsFragment;
import android.android.shifa.Models.DoctorModel;
import android.android.shifa.Models.ParamedicModel;
import android.android.shifa.R;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.victor.loading.rotate.RotateLoading;

public class AdminDoctorsAndParamedicsDetailsActivity extends AppCompatActivity {
    static EditText email_field, fullname_field, mobile_field, address_field;
    String DOCTOR_KEY;
    String PARAMEDIC_KEY;
    ImageView profilepicture, callmobile;
    TextView fullname_txt, nfcid_txt;
    String profile_image_url, mobile;

    RotateLoading rotateLoading;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_doctors_and_paramedics_details);

        DOCTOR_KEY = getIntent().getStringExtra(DoctorsFragment.ADMIN_EXTRA_DOCTOR_KEY);
        PARAMEDIC_KEY = getIntent().getStringExtra(ParamedicsFragment.ADMIN_EXTRA_PARAMEDIC_KEY);

        fullname_txt = findViewById(R.id.fullname_txt);
        nfcid_txt = findViewById(R.id.nfcid_txt);

        profilepicture = findViewById(R.id.patient_profile_picture);
        callmobile = findViewById(R.id.phonenumber_btn);

        email_field = findViewById(R.id.email_field);
        fullname_field = findViewById(R.id.fullname_field);
        mobile_field = findViewById(R.id.mobile_field);
        address_field = findViewById(R.id.address_field);

        rotateLoading = findViewById(R.id.rotateloading);

        email_field.setEnabled(false);
        fullname_field.setEnabled(false);
        mobile_field.setEnabled(false);
        address_field.setEnabled(false);
        profilepicture.setEnabled(false);

        callmobile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialContactPhone(mobile);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        rotateLoading.start();

        if (!TextUtils.isEmpty(DOCTOR_KEY)) {
            returndata(DOCTOR_KEY);
        } else {
            returndata2(PARAMEDIC_KEY);
        }
    }

    public void returndata(String key) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("AllUsers").child("Doctors").child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        DoctorModel doctorModel = dataSnapshot.getValue(DoctorModel.class);

                        nfcid_txt.setText(doctorModel.getSpecialization());

                        mobile = doctorModel.getMobilenumber();
                        mobile_field.setText(mobile);

                        email_field.setText(doctorModel.getEmail());

                        fullname_txt.setText(doctorModel.getFullname());
                        fullname_field.setText(doctorModel.getFullname());

                        address_field.setText(doctorModel.getAddress());

                        profile_image_url = doctorModel.getImageurl();

                        Picasso.get()
                                .load(profile_image_url)
                                .placeholder(R.drawable.logo2)
                                .error(R.drawable.logo2)
                                .into(profilepicture);

                        rotateLoading.stop();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    public void returndata2(String key) {
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("AllUsers").child("Paramedics").child(key).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        ParamedicModel paramedicModel = dataSnapshot.getValue(ParamedicModel.class);

                        nfcid_txt.setText(paramedicModel.getWork_place());

                        mobile = paramedicModel.getMobilenumber();
                        mobile_field.setText(mobile);

                        email_field.setText(paramedicModel.getEmail());

                        fullname_txt.setText(paramedicModel.getFullname());
                        fullname_field.setText(paramedicModel.getFullname());

                        address_field.setText(paramedicModel.getAddress());

                        profile_image_url = paramedicModel.getImageurl();

                        Picasso.get()
                                .load(profile_image_url)
                                .placeholder(R.drawable.logo2)
                                .error(R.drawable.logo2)
                                .into(profilepicture);

                        rotateLoading.stop();
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
