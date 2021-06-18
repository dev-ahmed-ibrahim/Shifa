package android.android.shifa.PateintApp.PatientFragments;

import android.Manifest;
import android.android.shifa.ActivitiesAndFragments.RegisterActivity;
import android.android.shifa.Listener;
import android.android.shifa.Models.LocationModel;
import android.android.shifa.Models.PatientModel;
import android.android.shifa.PateintApp.PatientMainActivity;
import android.android.shifa.R;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.victor.loading.rotate.RotateLoading;

import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class PatientProfileFragment extends Fragment implements Listener, GoogleApiClient.ConnectionCallbacks
        , GoogleApiClient.OnConnectionFailedListener, LocationListener {
    static EditText email_field, fullname_field, mobile_field, closemobile_field, address_field, nfcid_field, personalid_field, birthday_field, medicaldiagnosis_field, pharmaceutical_field;
    View view;
    Button edit_profile_btn, signout_btn, savechanges_btn, generate, sendLocation, getLocation;
    ImageView profilepicture, datepick, remover_user;
    TextView fullname_txt, nfcid_txt;
    CardView save_card;
    EditText patientNote;
    Spinner bloodtypes;
    String mobile, profile_image_url;

    String full_name_txt, email_txt, mobile_txt, address_txt, closest_txt, nfc_id_txt, personal_id_txt, date_txt, noote;

    RotateLoading rotateLoading;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String selected_placeimaeURL = "";
    Uri photoPath;


    GoogleApiClient googleApiClient;
    Location lastlocation;
    LocationRequest locationRequest;

    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;

    String pass, nfcid, full_name, emergency_number, blood_type, disease_patient;

    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.patient_profile_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        buildGoogleAPIClient();

        loginPreferences = getActivity().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        edit_profile_btn = view.findViewById(R.id.edit_profile_btn);
        signout_btn = view.findViewById(R.id.signout_btn);
        generate = view.findViewById(R.id.generate);
        sendLocation = view.findViewById(R.id.send_location_patient_btn);


        sendLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ConnectivityManager cm =
                        (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                boolean isConnected = activeNetwork != null &&
                        activeNetwork.isConnectedOrConnecting();

                if (isConnected) {
                    final LocationManager manager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

                    if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        Toast.makeText(getActivity(), "please check your gps is enabled", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (lastlocation == null) {
                        Toast.makeText(getActivity(), "please refresh your GPS and try again", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getActivity(), PatientMainActivity.class);
                        startActivity(intent);
                        return;
                    }

                    String latitude = Double.toString(lastlocation.getLatitude());
                    String longitude = Double.toString(lastlocation.getLongitude());

                    noote = patientNote.getText().toString();

                    if (TextUtils.isEmpty(noote)) {
                        noote = "Hurry Up ...";
                    }

                    Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                    sendRequest(noote, latitude, longitude);
                } else {
                    Toast.makeText(getActivity(), "please check your internet connection", Toast.LENGTH_SHORT).show();
                }
            }
        });
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GenerateCode.class);
                startActivity(intent);
            }
        });

        savechanges_btn = view.findViewById(R.id.savechanges_btn);

        save_card = view.findViewById(R.id.save_card);

        fullname_txt = view.findViewById(R.id.fullname_txt);
        nfcid_txt = view.findViewById(R.id.nfcid_txt);

        profilepicture = view.findViewById(R.id.patient_profile_picture);
        datepick = view.findViewById(R.id.date_picker);
        remover_user = view.findViewById(R.id.remover_user_btn);

        email_field = view.findViewById(R.id.email_field);
        fullname_field = view.findViewById(R.id.fullname_field);
        mobile_field = view.findViewById(R.id.mobile_field);
        closemobile_field = view.findViewById(R.id.closest_mobile_field);
        address_field = view.findViewById(R.id.address_field);
        nfcid_field = view.findViewById(R.id.nfc_id_field);
        personalid_field = view.findViewById(R.id.personal_id_field);
        birthday_field = view.findViewById(R.id.datebirth_field);
        medicaldiagnosis_field = view.findViewById(R.id.medical_diagnosis_field);
        pharmaceutical_field = view.findViewById(R.id.pharmaceutical_field);
        patientNote = view.findViewById(R.id.patient_note_patient_field);

        bloodtypes = view.findViewById(R.id.blood_spinner);
        rotateLoading = view.findViewById(R.id.rotateloading);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.bloodtypes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        bloodtypes.setAdapter(adapter1);

        saveLogin = loginPreferences.getBoolean("savepassword", false);

        if (saveLogin) {
            pass = loginPreferences.getString("pass", "");
        }

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

        remover_user.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteDialog();
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

                if (TextUtils.isEmpty(full_name_txt)) {
                    Toast.makeText(getContext(), "please enter your full name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email_txt)) {
                    Toast.makeText(getContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(mobile_txt)) {
                    Toast.makeText(getContext(), "please enter your mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(closest_txt)) {
                    Toast.makeText(getContext(), "please enter your closest mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address_txt)) {
                    Toast.makeText(getContext(), "please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(nfc_id_txt)) {
                    Toast.makeText(getContext(), "please enter your NFC id", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(personal_id_txt)) {
                    Toast.makeText(getContext(), "please enter your personal id", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(date_txt)) {
                    Toast.makeText(getContext(), "please pick or enter your birth date", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (bloodtypes.getSelectedItemPosition() == 0) {
                    Toast.makeText(getContext(), "please select your blood type", Toast.LENGTH_SHORT).show();
                    return;
                }

                save_card.setVisibility(View.GONE);
                bloodtypes.setEnabled(false);
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
                edit_profile_btn.setEnabled(true);

                if (photoPath == null) {
                    UpdatePatientProfile(getUid(), full_name_txt, email_txt, personal_id_txt, nfc_id_txt, date_txt, closest_txt, mobile_txt, bloodtypes.getSelectedItemPosition(), address_txt, profile_image_url, "");
                } else {
                    uploadImage(getUid(), full_name_txt, email_txt, personal_id_txt, nfc_id_txt, date_txt, closest_txt, mobile_txt, bloodtypes.getSelectedItemPosition(), address_txt);
                }
            }
        });

        profilepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1, 1)
                        .start(getContext(), PatientProfileFragment.this);
            }
        });

        signout_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(getContext(), NFCActivity.class);
                startActivity(intent);*/
                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(getContext(), RegisterActivity.class);
                startActivity(intent);
            }
        });

        datepick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_card.setVisibility(View.VISIBLE);
                fullname_field.setEnabled(true);
                mobile_field.setEnabled(true);
                closemobile_field.setEnabled(true);
                address_field.setEnabled(true);
                birthday_field.setEnabled(true);
                datepick.setEnabled(true);
                profilepicture.setEnabled(true);
                savechanges_btn.setEnabled(true);
                edit_profile_btn.setEnabled(false);
            }
        });

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        rotateLoading.start();

        returndata();
    }

    private void sendRequest(String noote, String latitude, String longitude) {
        LocationModel locationModel = new LocationModel(nfcid, nfc_id_txt, full_name, emergency_number, blood_type, disease_patient, noote, latitude, longitude, "", "");

        String request_key = databaseReference.child("AllRequests").push().getKey();

        databaseReference.child("AllRequests").child(request_key).setValue(locationModel);

        Toast.makeText(getContext(), "Location Sent", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(getContext(), PatientMainActivity.class);
        startActivity(intent);
    }

    @Override
    public void nfc_id(String id) {
        nfcid = id;
    }

    @Override
    public void patient_name(String name) {
        full_name = name;
    }

    @Override
    public void patient_number(String number) {
        emergency_number = number;
    }

    @Override
    public void patient_bloodtype(String bloodtype) {
        blood_type = bloodtype;
    }

    @Override
    public void patient_disease(String disease) {
        disease_patient = disease;
    }

    @Override
    public void onDialogDisplayed() {

    }

    @Override
    public void onDialogDismissed() {

    }

    @Override
    public void onNewIntent(Intent intent) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(@NonNull int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        lastlocation = location;
    }

    protected synchronized void buildGoogleAPIClient() {
        googleApiClient = new GoogleApiClient.Builder(requireContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    public void returndata() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        final String userId = user.getUid();

        mDatabase.child("AllUsers").child("Patients").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        PatientModel patientModel = dataSnapshot.getValue(PatientModel.class);

                        fullname_txt.setText(patientModel.getFullname());

                        nfcid = patientModel.getNFC_ID();

                        nfcid_txt.setText(nfcid);
                        mobile = patientModel.getMobilenumber();
                        email_field.setText(patientModel.getEmail());
                        fullname_field.setText(patientModel.getFullname());
                        mobile_field.setText(patientModel.getMobilenumber());
                        closemobile_field.setText(patientModel.getClose_mobile_number());
                        address_field.setText(patientModel.getAddress());
                        nfcid_field.setText(patientModel.getNFC_ID());
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

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void UpdatePatientProfile(String user_id, String fullname, String email, String personalid, String nfcid, String birthdate, String closemobile, String mobile, int bloodtype, String address, String imageurl, String imageurl2) {
        PatientModel patientModel = new PatientModel(user_id, fullname, email, personalid, nfcid, birthdate, closemobile, mobile, address, imageurl, imageurl2, "", "", bloodtype);

        databaseReference.child("Patients").child(nfcid).child(nfcid).setValue(patientModel);
        databaseReference.child("AllUsers").child("Patients").child(getUid()).setValue(patientModel);

        Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();

        if (photoPath == null) {
            returndata();
        }
    }

    private void uploadImage(final String user_id, final String fullname, final String email, final String personalid, final String nfcid, final String birthdate, final String closemobile, final String mobile, final int bloodtype, final String address) {
        rotateLoading.start();

        UploadTask uploadTask;

        final StorageReference ref = storageReference.child("images/" + photoPath.getLastPathSegment());

        uploadTask = ref.putFile(photoPath);

        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return ref.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                Uri downloadUri = task.getResult();

                rotateLoading.stop();

                selected_placeimaeURL = downloadUri.toString();

                UpdatePatientProfile(user_id, fullname, email, personalid, nfcid, birthdate, closemobile, mobile, bloodtype, address, selected_placeimaeURL, "");

                Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();

                returndata();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                if (result != null) {
                    photoPath = result.getUri();

                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.patient2)
                            .error(R.drawable.patient2)
                            .into(profilepicture);

                    selected_placeimaeURL = photoPath.toString();
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void showDeleteDialog() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.delete_user_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Button yes_btn = dialog.findViewById(R.id.yes_btn);
        Button cancel_btn = dialog.findViewById(R.id.cancel_btn);

        yes_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("DeleteAccount");
                progressDialog.setMessage("Please Wait Until Deleting Account ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);

                databaseReference.child("Patients").child(nfcid).child(nfcid).removeValue();
                databaseReference.child("AllUsers").child("Patients").child(getUid()).removeValue();

                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    /*user.delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful())
                                    {
                                        Intent intent = new Intent(getContext(), RegisterActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            });*/
                    // Get auth credentials from the user for re-authentication. The example below shows
                    // email and password credentials but there are multiple possible providers,
                    // such as GoogleAuthProvider or FacebookAuthProvider.
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(email_field.getText().toString(), pass);

                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    user.delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Intent intent = new Intent(getContext(), RegisterActivity.class);
                                                        startActivity(intent);
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                }
                            });
                }
            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), R.style.dialoge, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            // Do something with the date chosen by the user
            int month2 = month + 1;
            birthday_field.setText(day + "/" + month2 + "/" + year);
        }
    }


}
