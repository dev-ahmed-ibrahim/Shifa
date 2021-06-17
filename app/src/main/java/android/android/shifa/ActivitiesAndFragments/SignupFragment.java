package android.android.shifa.ActivitiesAndFragments;

import android.android.shifa.DoctorApp.DoctorMainActivity;
import android.android.shifa.GuestApp.GuestActivity;
import android.android.shifa.Models.DoctorModel;
import android.android.shifa.Models.ParamedicModel;
import android.android.shifa.Models.PatientModel;
import android.android.shifa.ParamedicApp.ParamedicMainActivity;
import android.android.shifa.PateintApp.PatientMainActivity;
import android.android.shifa.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import com.balysv.materialripple.MaterialRippleLayout;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;

public class SignupFragment extends Fragment {
    public final static int QRCodeWidth = 500;
    public static String EXTRA_PROFILE_TAG = "profile_tag";
    static EditText first_name, last_name, email_address, password, phone_number, closest_number, address, nfc_id, personal_id, date_edittext;
    View view;
    MaterialRippleLayout doctor_sign_up, patient_sign_up;
    Button paramedic_btn, guest_btn;
    Bitmap bitmap;
    CircleImageView profile_image;
    ImageView date_picker;
    String first_name_txt, last_name_txt, full_name_txt, email_txt, password_txt, mobile_txt, address_txt, closest_txt, nfc_id_txt, personal_id_txt, date_txt;
    Button sign_up_btn, cancel_btn;
    Spinner specialization_spinner, hospital_spinner, blood_spinner;
    String specialization_txt, hospital_txt, blood_txt;
    String selectedimageurl, selectedimageurl2;
    Uri photoPath, photoPath2;
    ProgressDialog progressDialog;
    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;
    private EditText text;
    private Button download;
    private Button generate;
    private ImageView imageView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sign_up_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loginPreferences = getActivity().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        doctor_sign_up = view.findViewById(R.id.doctor_sign_up_card);
        patient_sign_up = view.findViewById(R.id.patient_sign_up_card);
        paramedic_btn = view.findViewById(R.id.paramedic_sign_up_btn);
        guest_btn = view.findViewById(R.id.guest_btn);

        auth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.keepSynced(true);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference().child("images");

        doctor_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDoctorDialog();
            }
        });

        patient_sign_up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPatientDialog();
            }
        });

        paramedic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showParamedicDialog();
            }
        });

        guest_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GuestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showDoctorDialog() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.doctor_register_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        profile_image = dialog.findViewById(R.id.profile_image);
        first_name = dialog.findViewById(R.id.first_name_field);
        last_name = dialog.findViewById(R.id.last_name_field);
        email_address = dialog.findViewById(R.id.email_field);
        password = dialog.findViewById(R.id.password_field);
        phone_number = dialog.findViewById(R.id.mobile_field);
        address = dialog.findViewById(R.id.address_field);
        specialization_spinner = dialog.findViewById(R.id.specialization_spinner);

        sign_up_btn = dialog.findViewById(R.id.sign_up_btn);
        cancel_btn = dialog.findViewById(R.id.cancel_btn);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.department, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        specialization_spinner.setAdapter(adapter1);

        specialization_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                specialization_txt = String.valueOf(parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_name_txt = first_name.getText().toString();
                last_name_txt = last_name.getText().toString();
                full_name_txt = first_name_txt + " " + last_name_txt;
                email_txt = email_address.getText().toString();
                password_txt = password.getText().toString();
                mobile_txt = phone_number.getText().toString();
                address_txt = address.getText().toString();

                if (TextUtils.isEmpty(first_name_txt)) {
                    Toast.makeText(getContext(), "please enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(last_name_txt)) {
                    Toast.makeText(getContext(), "please enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email_txt)) {
                    Toast.makeText(getContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password_txt)) {
                    Toast.makeText(getContext(), "please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(mobile_txt)) {
                    Toast.makeText(getContext(), "please enter your mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address_txt)) {
                    Toast.makeText(getContext(), "please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoPath == null) {
                    Toast.makeText(getContext(), "please add your picture", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (specialization_txt.equals("Select your specialty")) {
                    Toast.makeText(getContext(), "please select your specialization", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Doctor Sign Up");
                progressDialog.setMessage("Please Wait Until Creating Account ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);

                //Toast.makeText(getContext(), full_name_txt, Toast.LENGTH_SHORT).show();

                CreateDoctorAccount(email_txt, password_txt, full_name_txt, mobile_txt, specialization_txt, address_txt);

                loginPrefsEditor.putBoolean("savepassword", true);
                loginPrefsEditor.putString("pass", password_txt);
                loginPrefsEditor.apply();

                //CustomerRegister(full_name_txt,email_txt,password_txt,mobile_txt,"Customer");
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1, 1)
                        .start(getContext(), SignupFragment.this);
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

    private void showPatientDialog() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.patient_register_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        profile_image = dialog.findViewById(R.id.profile_image);
        first_name = dialog.findViewById(R.id.first_name_field);
        last_name = dialog.findViewById(R.id.last_name_field);
        email_address = dialog.findViewById(R.id.email_field);
        password = dialog.findViewById(R.id.password_field);
        phone_number = dialog.findViewById(R.id.mobile_field);
        closest_number = dialog.findViewById(R.id.closest_mobile_field);
        address = dialog.findViewById(R.id.address_field);


        nfc_id = dialog.findViewById(R.id.nfc_id_field);
        download = dialog.findViewById(R.id.download);
        download.setVisibility(View.INVISIBLE);
        generate = dialog.findViewById(R.id.generate);
        imageView = dialog.findViewById(R.id.image);


        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nfc_id.getText().toString().trim().length() == 0) {
                    Toast.makeText(getContext(), "Enter Text", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        bitmap = textToImageEncode(nfc_id.getText().toString());
                        imageView.setImageBitmap(bitmap);
                        download.setVisibility(View.GONE);


                    } catch (WriterException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        personal_id = dialog.findViewById(R.id.personal_id_field);
        date_picker = dialog.findViewById(R.id.date_picker);
        date_edittext = dialog.findViewById(R.id.date_field);
        blood_spinner = dialog.findViewById(R.id.blood_spinner);

        sign_up_btn = dialog.findViewById(R.id.sign_up_btn);
        cancel_btn = dialog.findViewById(R.id.cancel_btn);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.bloodtypes, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        blood_spinner.setAdapter(adapter1);

        blood_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                blood_txt = String.valueOf(parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_name_txt = first_name.getText().toString();
                last_name_txt = last_name.getText().toString();
                full_name_txt = first_name_txt + " " + last_name_txt;
                email_txt = email_address.getText().toString();
                password_txt = password.getText().toString();
                mobile_txt = phone_number.getText().toString();
                closest_txt = closest_number.getText().toString();
                address_txt = address.getText().toString();
                nfc_id_txt = nfc_id.getText().toString();


                personal_id_txt = personal_id.getText().toString();
                date_txt = date_edittext.getText().toString();

                if (TextUtils.isEmpty(first_name_txt)) {
                    Toast.makeText(getContext(), "please enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(last_name_txt)) {
                    Toast.makeText(getContext(), "please enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email_txt)) {
                    Toast.makeText(getContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password_txt)) {
                    Toast.makeText(getContext(), "please enter your password", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "please enter your QR-Code id", Toast.LENGTH_SHORT).show();
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

                if (photoPath == null) {
                    Toast.makeText(getContext(), "please add your picture", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (blood_spinner.getSelectedItemPosition() == 0) {
                    Toast.makeText(getContext(), "please select your blood type", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Patient Sign Up");
                progressDialog.setMessage("Please Wait Until Creating Account ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);

                //Toast.makeText(getContext(), full_name_txt, Toast.LENGTH_SHORT).show();

                CreatePatientAccount(full_name_txt, email_txt, password_txt, personal_id_txt, nfc_id_txt, date_txt, closest_txt, mobile_txt, blood_spinner.getSelectedItemPosition(), address_txt, selectedimageurl, selectedimageurl2);

                loginPrefsEditor.putBoolean("savepassword", true);
                loginPrefsEditor.putString("pass", password_txt);
                loginPrefsEditor.apply();

                //CustomerRegister(full_name_txt,email_txt,password_txt,mobile_txt,"Customer");
            }
        });

        date_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1, 1)
                        .start(getContext(), SignupFragment.this);
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

    private void showParamedicDialog() {
        final Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.paramedic_register_dialog);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes();
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        profile_image = dialog.findViewById(R.id.profile_image);
        first_name = dialog.findViewById(R.id.first_name_field);
        last_name = dialog.findViewById(R.id.last_name_field);
        email_address = dialog.findViewById(R.id.email_field);
        password = dialog.findViewById(R.id.password_field);
        phone_number = dialog.findViewById(R.id.mobile_field);
        address = dialog.findViewById(R.id.address_field);
        hospital_spinner = dialog.findViewById(R.id.hospital_spinner);

        sign_up_btn = dialog.findViewById(R.id.sign_up_btn);
        cancel_btn = dialog.findViewById(R.id.cancel_btn);

        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(getContext(),
                R.array.hospitals, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        hospital_spinner.setAdapter(adapter1);

        hospital_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                hospital_txt = String.valueOf(parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sign_up_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                first_name_txt = first_name.getText().toString();
                last_name_txt = last_name.getText().toString();
                full_name_txt = first_name_txt + " " + last_name_txt;
                email_txt = email_address.getText().toString();
                password_txt = password.getText().toString();
                mobile_txt = phone_number.getText().toString();
                address_txt = address.getText().toString();

                if (TextUtils.isEmpty(first_name_txt)) {
                    Toast.makeText(getContext(), "please enter your first name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(last_name_txt)) {
                    Toast.makeText(getContext(), "please enter your last name", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(email_txt)) {
                    Toast.makeText(getContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password_txt)) {
                    Toast.makeText(getContext(), "please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(mobile_txt)) {
                    Toast.makeText(getContext(), "please enter your mobile number", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(address_txt)) {
                    Toast.makeText(getContext(), "please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (photoPath == null) {
                    Toast.makeText(getContext(), "please add your picture", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (hospital_txt.length() == 0) {
                    Toast.makeText(getContext(), "please select your work hospital", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressDialog = new ProgressDialog(getContext());
                progressDialog.setTitle("Paramedic Sign Up");
                progressDialog.setMessage("Please Wait Until Creating Account ...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.show();
                progressDialog.setCancelable(false);

                //Toast.makeText(getContext(), full_name_txt, Toast.LENGTH_SHORT).show();

                CreateParamedicAccount(email_txt, password_txt, full_name_txt, mobile_txt, hospital_txt, address_txt);

                loginPrefsEditor.putBoolean("savepassword", true);
                loginPrefsEditor.putString("pass", password_txt);
                loginPrefsEditor.apply();

                //CustomerRegister(full_name_txt,email_txt,password_txt,mobile_txt,"Customer");
            }
        });

        profile_image.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1, 1)
                        .start(getContext(), SignupFragment.this);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == Activity.RESULT_OK) {
                if (result != null) {
                    photoPath = result.getUri();

                    Picasso.get()
                            .load(photoPath)
                            .placeholder(R.drawable.addphoto)
                            .error(R.drawable.addphoto)
                            .into(profile_image);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void uploadImage(final String fullname, final String email, final String mobilenumber, final String specialty, final String address) {
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

                selectedimageurl = downloadUri.toString();

                AddDoctortoDB(fullname, email, mobilenumber, specialty, address, selectedimageurl);
                progressDialog.dismiss();

                Intent intent = new Intent(getContext(), DoctorMainActivity.class);
                startActivity(intent);
                //Toast.makeText(getContext(), "successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void AddDoctortoDB(String fullname, String email, String mobilenumber, String specialty, String address, String imageurl) {
        DoctorModel doctorModel = new DoctorModel(fullname, email, mobilenumber, specialty, address, imageurl);

        databaseReference.child("Doctors").child(specialty).child(getUID()).setValue(doctorModel);
        databaseReference.child("AllUsers").child("Doctors").child(getUID()).setValue(doctorModel);
    }

    private void CreateDoctorAccount(final String email, String password, final String fullname, final String mobilenumber, final String specialty, final String address) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            uploadImage(fullname, email, mobilenumber, specialty, address);
                        } else {
                            String error_message = task.getException().getMessage();
                            Toast.makeText(getContext(), error_message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void CreateParamedicAccount(final String email, String password, final String fullname, final String mobilenumber, final String hospital, final String address) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            uploadParamedicImage(fullname, email, mobilenumber, hospital, address);
                        } else {
                            String error_message = task.getException().getMessage();
                            Toast.makeText(getContext(), error_message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void AddParamedicoDB(String fullname, String email, String mobilenumber, String hospital, String address, String imageurl) {
        ParamedicModel paramedicModel = new ParamedicModel(fullname, email, mobilenumber, hospital, address, imageurl);

        databaseReference.child("Paramedics").child(hospital).child(getUID()).setValue(paramedicModel);
        databaseReference.child("AllUsers").child("Paramedics").child(getUID()).setValue(paramedicModel);
    }

    private void uploadParamedicImage(final String fullname, final String email, final String mobilenumber, final String hospital, final String address) {
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

                selectedimageurl = downloadUri.toString();

                AddParamedicoDB(fullname, email, mobilenumber, hospital, address, selectedimageurl);
                progressDialog.dismiss();

                Intent intent = new Intent(getContext(), ParamedicMainActivity.class);
                startActivity(intent);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private void AddPatienttoDB(String user_id, String fullname, String email, String personalid, String nfcid, String birthdate, String closemobile, String mobile, int bloodtype, String address, String imageurl, String imageurl2) {
        PatientModel patientModel = new PatientModel(user_id, fullname, email, personalid, nfcid, birthdate, closemobile, mobile, address, imageurl, imageurl2, "", "", bloodtype);

        databaseReference.child("Patients").child(nfcid).child(nfcid).setValue(patientModel);
        databaseReference.child("AllUsers").child("Patients").child(getUID()).setValue(patientModel);
    }

    private void CreatePatientAccount(final String fullname, final String email, String password, final String personalid, final String nfcid, final String birthdate, final String closemobile, final String mobile, final int bloodtype, final String address, final String imageurl, final String imageurl2) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            String id = FirebaseAuth.getInstance().getCurrentUser().getUid();

                            uploadPatientImage(id, fullname, email, personalid, nfcid, birthdate, closemobile, mobile, bloodtype, address);
                        } else {
                            String error_message = task.getException().getMessage();
                            Toast.makeText(getContext(), error_message, Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();
                        }
                    }
                });
    }

    private void uploadPatientImage(final String user_id, final String fullname, final String email, final String personalid, final String nfcid, final String birthdate, final String closemobile, final String mobile, final int bloodtype, final String address) {
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

                selectedimageurl = downloadUri.toString();
                selectedimageurl2 = downloadUri.toString();

                AddPatienttoDB(user_id, fullname, email, personalid, nfcid, birthdate, closemobile, mobile, bloodtype, address, selectedimageurl, selectedimageurl2);
                progressDialog.dismiss();

                Intent intent = new Intent(getContext(), PatientMainActivity.class);
                intent.putExtra(EXTRA_PROFILE_TAG, 123);
                startActivity(intent);
                //Toast.makeText(getContext(), "Successfully", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(getContext(), "Can't Upload Photo", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    private String getUID() {
        String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return id;
    }

    private Bitmap textToImageEncode(String value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(value,
                    BarcodeFormat.QR_CODE, QRCodeWidth, QRCodeWidth, null);
        } catch (IllegalArgumentException e) {
            return null;
        }

        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offSet = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offSet + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.overlay_dark_90) : getResources().getColor(R.color.overlay_light_80);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
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
            date_edittext.setText(day + "/" + month2 + "/" + year);
        }
    }

}