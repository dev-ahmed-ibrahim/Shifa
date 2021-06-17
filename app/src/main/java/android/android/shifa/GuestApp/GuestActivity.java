package android.android.shifa.GuestApp;

import android.Manifest;
import android.android.shifa.Listener;
import android.android.shifa.Models.LocationModel;
import android.android.shifa.NFCFragments.NFCReadFragment3;
import android.android.shifa.ParamedicApp.ParamedicFragments.ParamedicNFC;
import android.android.shifa.R;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;

public class GuestActivity extends AppCompatActivity implements Listener, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {
    public static final String TAG = ParamedicNFC.class.getSimpleName();
    public static final int CAMERA_PERMISSION_CODE = 100;
    SwipeRefreshLayout swipeRefreshLayout;
    MaterialRippleLayout first_aid_card;
    Button scan_nfc, sendlocation;
    EditText patient_notes, nfc_id;
    String nfcid, namee, emergencyy, bloodtypee, diseasee, noote;
    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;
    NFCReadFragment3 mNfcReadFragment;
    boolean isDialogDisplayed = false;
    boolean isWrite = false;
    NfcAdapter mNfcAdapter;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private SurfaceView surfaceView;
    private TextView ttext;
    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private Button camera;

    @TargetApi(Build.VERSION_CODES.P)
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guest);

        first_aid_card = findViewById(R.id.first_aid_card);
        scan_nfc = findViewById(R.id.btn_read);
        sendlocation = findViewById(R.id.send_location_btn);
        patient_notes = findViewById(R.id.patient_note_field);
        nfc_id = findViewById(R.id.nfc_Idd);

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        surfaceView = findViewById(R.id.camira);
        ttext = findViewById(R.id.textt);
        camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA, CAMERA_PERMISSION_CODE);
            }
        });

        swipeRefreshLayout = findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(() -> qrCode());


        initNFC();
        qrCode();

        buildGoogleAPIClient();

        first_aid_card.setOnClickListener(new View.OnClickListener() {
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

        sendlocation.setOnClickListener(new View.OnClickListener() {
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
                        Intent intent = new Intent(getApplicationContext(), GuestActivity.class);
                        startActivity(intent);
                        return;
                    }

                    String latitude = Double.toString(lastlocation.getLatitude());
                    String longitude = Double.toString(lastlocation.getLongitude());

                    String text = ttext.getText().toString();
                    //   nfcid = ttext.getText().toString();
                    noote = patient_notes.getText().toString();

                  /*  if (nfcid.isEmpty()) {
                        nfcid = ttext.getText().toString();
                        Toast.makeText(getApplicationContext(), "NFC not scanned, Proceeding with QR-Code!", Toast.LENGTH_SHORT).show();
                        sendRequest(nfcid,text, namee, emergencyy, bloodtypee, diseasee, noote, latitude, longitude);
                        return;
                    }

                   */

                    if (text.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "QR-Code not scanned, Proceeding with NFC!", Toast.LENGTH_SHORT).show();
                        sendRequest(nfcid, text, namee, emergencyy, bloodtypee, diseasee, noote, latitude, longitude);
                        return;
                    } else {
                        nfcid = ttext.getText().toString();
                        Toast.makeText(getApplicationContext(), "NFC not scanned, Proceeding with QR-Code!", Toast.LENGTH_SHORT).show();
                        sendRequest(nfcid, text, namee, emergencyy, bloodtypee, diseasee, noote, latitude, longitude);
                    }

                    if (TextUtils.isEmpty(noote)) {
                        noote = "Hurry Up ...";
                    }


                } else {
                    Toast.makeText(getApplicationContext(), "please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void showReadFragment() {
        mNfcReadFragment = (NFCReadFragment3) getSupportFragmentManager().findFragmentByTag(NFCReadFragment3.TAG);

        if (mNfcReadFragment == null) {

            mNfcReadFragment = NFCReadFragment3.newInstance();
        }
        mNfcReadFragment.show(getSupportFragmentManager(), NFCReadFragment3.TAG);
    }

    private void qrCode() {
        barcodeDetector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE).build();

        cameraSource = new CameraSource.Builder(getApplicationContext(), barcodeDetector)
                .setRequestedPreviewSize(540, 400).build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                try {
                    cameraSource.start(holder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {

            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {

                final SparseArray<Barcode> qrcode = detections.getDetectedItems();
                if (qrcode.size() != 0) {
                    ttext.post(new Runnable() {
                        @Override
                        public void run() {
                            ttext.setText(qrcode.valueAt(0).displayValue);
                        }
                    });
                }
            }
        });

        swipeRefreshLayout.setRefreshing(false);


    }

    private void initNFC() {
        mNfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
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
        nfcid = id;
    }

    @Override
    public void patient_name(String name) {
        namee = name;
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
            mNfcAdapter.enableForegroundDispatch(GuestActivity.this, pendingIntent, nfcIntentFilter, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(GuestActivity.this);
    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        Log.d(TAG, "onNewIntent: " + intent.getAction());

        if (tag != null) {
            Toast.makeText(getApplicationContext(), getString(R.string.message_tag_detected), Toast.LENGTH_SHORT).show();
            Ndef ndef = Ndef.get(tag);

            if (isDialogDisplayed) {
                if (isWrite) {

                } else {
                    mNfcReadFragment = (NFCReadFragment3) getSupportFragmentManager().findFragmentByTag(NFCReadFragment3.TAG);
                    mNfcReadFragment.onNfcDetected(ndef);
                }
            }
        }
    }

    public void sendRequest(String nfc_id, String text, String full_name, String emergency_number, String blood_type, String disease_patient, String notes, String latitude, String longitude) {
        LocationModel locationModel = new LocationModel(nfc_id, text, full_name, emergency_number, blood_type, disease_patient, notes, latitude, longitude);

        String request_key = databaseReference.child("AllRequests").push().getKey();

        databaseReference.child("AllRequests").child(request_key).setValue(locationModel);

        Toast.makeText(getApplicationContext(), "Location Sent", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getApplicationContext(), GuestActivity.class);
        startActivity(intent);
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

    public void checkPermission(String permission, int requestCode) {
        if (ContextCompat.checkSelfPermission(GuestActivity.this, permission)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(GuestActivity.this, new String[]{permission},
                    requestCode);

        } else {
            Toast.makeText(this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(getApplicationContext(), GuestActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
