package android.android.shifa.ActivitiesAndFragments;

import android.android.shifa.DoctorApp.DoctorMainActivity;
import android.android.shifa.Listener;
import android.android.shifa.Models.PatientModel;
import android.android.shifa.NFCFragments.NFCReadFragment;
import android.android.shifa.NFCFragments.NFCWriteFragment;
import android.android.shifa.PateintApp.PatientMainActivity;
import android.android.shifa.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.victor.loading.rotate.RotateLoading;

public class NFCActivity extends AppCompatActivity implements Listener {
    public static final String TAG = PatientMainActivity.class.getSimpleName();
    public static final String NFC_PATIENT = "nfc_patient_id";
    String nfc_id;
    RotateLoading rotateLoading;
    Button view_profile_btn;
    EditText patient_nfc, patient_name, patient_emergency, patient_bloodtype, patient_disease;
    String all, nfc, name, emergency, bloodtype, disease;
    private Button mBtWrite;
    private Button mBtRead;
    private NFCWriteFragment mNfcWriteFragment;
    private NFCReadFragment mNfcReadFragment;

    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;

    private NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        nfc_id = getIntent().getStringExtra(PatientDetailsActivity.PATIENT_NFC_KEY);

        initViews();
        if (!TextUtils.isEmpty(nfc_id)) {
            return_scanned_data(nfc_id);
        }
        initNFC();
    }

    public void return_scanned_data(String patient_nfc_id) {
        rotateLoading.start();

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        mDatabase.child("Patients").child(patient_nfc_id).child(patient_nfc_id).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);

                        patient_nfc.setText(patientModel.getNFC_ID());
                        patient_name.setText(patientModel.getFullname());
                        patient_emergency.setText(patientModel.getClose_mobile_number());

                        int blood = patientModel.getBloodtypes();

                        if (blood == 1) {
                            patient_bloodtype.setText("O -");
                        } else if (blood == 2) {
                            patient_bloodtype.setText("O +");
                        } else if (blood == 3) {
                            patient_bloodtype.setText("A -");
                        } else if (blood == 4) {
                            patient_bloodtype.setText("A +");
                        } else if (blood == 5) {
                            patient_bloodtype.setText("B -");
                        } else if (blood == 6) {
                            patient_bloodtype.setText("B +");
                        } else if (blood == 7) {
                            patient_bloodtype.setText("AB -");
                        } else {
                            patient_bloodtype.setText("AB +");
                        }

                        rotateLoading.stop();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    private void initViews() {
        /*fullname = getIntent().getStringExtra(PatientDetailsActivity.PATIENT_NAME_KEY);
        emergency = getIntent().getStringExtra(PatientDetailsActivity.PATIENT_EMERGENCY_KEY);
        bloodtype = getIntent().getStringExtra(PatientDetailsActivity.PATIENT_BLOODTYPE_KEY);*/

        //mEtMessage = findViewById(R.id.et_message);
        mBtWrite = findViewById(R.id.btn_write);
        mBtRead = findViewById(R.id.btn_read);

        rotateLoading = findViewById(R.id.rotateloading);

        view_profile_btn = findViewById(R.id.view_profile_btn);
        patient_nfc = findViewById(R.id.patient_nfc_field);
        patient_name = findViewById(R.id.patient_name_field);
        patient_emergency = findViewById(R.id.patient_emergency_field);
        patient_bloodtype = findViewById(R.id.patient_bloodtype_field);
        patient_disease = findViewById(R.id.patient_disease_field);

        patient_nfc.setEnabled(false);
        patient_bloodtype.setEnabled(false);
        patient_name.setEnabled(false);
        patient_emergency.setEnabled(false);
        //patient_disease.setEnabled(false);

        view_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nfc2 = patient_nfc.getText().toString();

                if (TextUtils.isEmpty(nfc2)) {
                    Toast.makeText(getApplicationContext(), "please sure that you scanned NFC", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(getApplicationContext(), PatientDetailsActivity.class);
                    intent.putExtra(NFC_PATIENT, nfc2);
                    startActivity(intent);
                }
            }
        });

        mBtWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
                NfcAdapter adapter = manager.getDefaultAdapter();
                if (adapter != null && adapter.isEnabled()) {
                    // adapter exists and is enabled.

                    nfc = patient_nfc.getText().toString();
                    name = patient_name.getText().toString();
                    emergency = patient_emergency.getText().toString();
                    bloodtype = patient_bloodtype.getText().toString();
                    disease = patient_disease.getText().toString();

                    all = nfc + "," + name + "," + emergency + "," + bloodtype + "," + disease;

                    if (TextUtils.isEmpty(disease)) {
                        Toast.makeText(getApplicationContext(), "please enter disease", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    showWriteFragment();
                } else {
                    Toast.makeText(getApplicationContext(), "please check that NFC is enabled firstly", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
                NfcAdapter adapter = manager.getDefaultAdapter();
                if (adapter != null && adapter.isEnabled()) {
                    // adapter exists and is enabled.
                    showReadFragment();
                } else {
                    Toast.makeText(getApplicationContext(), "please check that NFC is enabled firstly", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    private void showWriteFragment() {
        isWrite = true;

        mNfcWriteFragment = (NFCWriteFragment) getSupportFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);

        if (mNfcWriteFragment == null) {
            mNfcWriteFragment = NFCWriteFragment.newInstance();
        }
        mNfcWriteFragment.show(getSupportFragmentManager(), NFCWriteFragment.TAG);
    }

    private void showReadFragment() {
        mNfcReadFragment = (NFCReadFragment) getSupportFragmentManager().findFragmentByTag(NFCReadFragment.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment.newInstance();
        }
        mNfcReadFragment.show(getSupportFragmentManager(), NFCReadFragment.TAG);
    }

    @Override
    public void onDialogDisplayed() {
        isDialogDisplayed = true;
    }

    @Override
    public void onDialogDismissed() {
        isDialogDisplayed = false;
        isWrite = false;
    }

    @Override
    public void nfc_id(String id) {
        patient_nfc.setText(id);
    }

    @Override
    public void patient_name(String name) {
        patient_name.setText(name);
    }

    @Override
    public void patient_number(String number) {
        patient_emergency.setText(number);
    }

    @Override
    public void patient_bloodtype(String bloodtype) {
        patient_bloodtype.setText(bloodtype);
    }

    @Override
    public void patient_disease(String disease) {
        patient_disease.setText(disease);
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(this, pendingIntent, nfcIntentFilter, null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: " + intent.getAction());

        if (tag != null) {
            Toast.makeText(this, getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {
                if (isWrite) {
                    nfc = patient_nfc.getText().toString();
                    name = patient_name.getText().toString();
                    emergency = patient_emergency.getText().toString();
                    bloodtype = patient_bloodtype.getText().toString();
                    disease = patient_disease.getText().toString();

                    all = nfc + "," + name + "," + emergency + "," + bloodtype + "," + disease;

                    mNfcWriteFragment = (NFCWriteFragment) getSupportFragmentManager().findFragmentByTag(NFCWriteFragment.TAG);
                    mNfcWriteFragment.onNfcDetected(ndef, all);
                } else {
                    mNfcReadFragment = (NFCReadFragment) getSupportFragmentManager().findFragmentByTag(NFCReadFragment.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (TextUtils.isEmpty(nfc_id)) {
            super.onBackPressed();
        } else {
            Intent intent = new Intent(getApplicationContext(), DoctorMainActivity.class);
            startActivity(intent);
        }
    }
}
