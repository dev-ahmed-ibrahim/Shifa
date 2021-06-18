package android.android.shifa.ParamedicApp;

import android.Manifest;
import android.android.shifa.Listener;
import android.android.shifa.Models.LocationModel;
import android.android.shifa.NFCFragments.NFCReadFragment2;
import android.android.shifa.ParamedicApp.ParamedicFragments.ParamedicNFC;
import android.android.shifa.R;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ParamedicNFCActivity extends AppCompatActivity implements Listener, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String TAG = ParamedicNFC.class.getSimpleName();
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    String nfcid, namee, emergencyy, bloodtypee, diseasee, noote;
    Button scan_nfc, send_location;
    EditText patient_nfc, patient_name, patient_note;
    MaterialRippleLayout first_aid;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private NFCReadFragment2 mNfcReadFragment;
    private boolean isDialogDisplayed = false;
    private boolean isWrite = false;
    private NfcAdapter mNfcAdapter;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paramedic_nfc);

        scan_nfc = findViewById(R.id.btn_read);
        send_location = findViewById(R.id.send_location_btn);

        patient_nfc = findViewById(R.id.patient_nfc_field);
        patient_name = findViewById(R.id.patient_name_field);
        patient_note = findViewById(R.id.patient_note_field);

        first_aid = findViewById(R.id.first_aid_card);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        initNFC();

        buildGoogleAPIClient();

        first_aid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://first-aid-product.com/free-first-aid-guide.html";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        scan_nfc.setOnClickListener(new View.OnClickListener() {
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

        send_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectivityManager cm =
                        (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if (isConnected) {
                    final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(getApplicationContext(), "please check your gps is enabled", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (lastlocation == null) {
                        Toast.makeText(getApplicationContext(), "please refresh your GPS and try again", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), ParamedicNFCActivity.class);
                        startActivity(intent);
                        return;
                    }

                    double latitude1 = lastlocation.getLatitude();
                    double longitude1 = lastlocation.getLongitude();

                    String latitude = Double.toString(latitude1);
                    String longitude = Double.toString(longitude1);

                    //String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);

                    nfcid = patient_nfc.getText().toString();
                    namee = patient_name.getText().toString();
                    noote = patient_note.getText().toString();

                    if (TextUtils.isEmpty(nfcid)) {
                        Toast.makeText(getApplicationContext(), "please scan NFC firstly", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (TextUtils.isEmpty(noote)) {
                        noote = "Hurry Up ...";
                    }
                    sendRequest(nfcid, namee, emergencyy, bloodtypee, diseasee, noote, latitude, longitude);
                } else {
                    Toast.makeText(getApplicationContext(), "please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
    }

    private void showReadFragment() {
        mNfcReadFragment = (NFCReadFragment2) getSupportFragmentManager().findFragmentByTag(NFCReadFragment2.TAG);

        if (mNfcReadFragment == null) {
            mNfcReadFragment = NFCReadFragment2.newInstance();
        }
        mNfcReadFragment.show(getSupportFragmentManager(), NFCReadFragment2.TAG);
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
        emergencyy = number;
    }

    @Override
    public void patient_bloodtype(String bloodtype) {
        bloodtypee = bloodtype;
    }

    @Override
    public void patient_disease(String disease) {
        diseasee = disease;
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        IntentFilter techDetected = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        IntentFilter[] nfcIntentFilter = new IntentFilter[]{techDetected, tagDetected, ndefDetected};

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(), 0, new Intent(getApplicationContext(), getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        if (mNfcAdapter != null)
            mNfcAdapter.enableForegroundDispatch(ParamedicNFCActivity.this, pendingIntent, nfcIntentFilter, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(ParamedicNFCActivity.this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: " + intent.getAction());

        if (tag != null) {
            Toast.makeText(getApplicationContext(), getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {
                if (isWrite) {

                } else {
                    mNfcReadFragment = (NFCReadFragment2) getSupportFragmentManager().findFragmentByTag(NFCReadFragment2.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                }
            }
        }
    }

    public void sendRequest(String nfc_id, String full_name, String emergency_number, String blood_type, String disease_patient, String notes, String latitude, String longitude) {
        LocationModel locationModel = new LocationModel(nfc_id, full_name, emergency_number, blood_type, disease_patient, "", notes, latitude, longitude, "", "");

        String request_key = databaseReference.child("AllRequests").push().getKey();

        databaseReference.child("AllRequests").child(request_key).setValue(locationModel);

        Toast.makeText(getApplicationContext(), "Location Sent", Toast.LENGTH_SHORT).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;
    }

    protected synchronized void buildGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }
}
