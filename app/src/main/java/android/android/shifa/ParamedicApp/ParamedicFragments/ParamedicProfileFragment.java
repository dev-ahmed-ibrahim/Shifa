package android.android.shifa.ParamedicApp.ParamedicFragments;

import android.android.shifa.ActivitiesAndFragments.RegisterActivity;
import android.android.shifa.Models.ParamedicModel;
import android.android.shifa.R;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

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

import static android.app.Activity.RESULT_OK;
import static android.content.Context.MODE_PRIVATE;

public class ParamedicProfileFragment extends Fragment {

    static EditText email_field, fullname_field, mobile_field, address_field;
    View view;
    Button edit_profile_btn, signout_btn, savechanges_btn;
    ImageView profilepicture, remover_user;
    TextView fullname_txt, nfcid_txt;
    CardView save_card;
    String mobile, profile_image_url, special;

    String full_name_txt, email_txt, mobile_txt, address_txt;

    RotateLoading rotateLoading;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    String selected_placeimaeURL = "";
    Uri photoPath;

    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;

    String pass;

    ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.paramedic_profile_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        loginPreferences = getActivity().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        edit_profile_btn = view.findViewById(R.id.edit_profile_btn);
        signout_btn = view.findViewById(R.id.signout_btn);
        savechanges_btn = view.findViewById(R.id.savechanges_btn);

        save_card = view.findViewById(R.id.save_card);

        fullname_txt = view.findViewById(R.id.fullname_txt);
        nfcid_txt = view.findViewById(R.id.nfcid_txt);

        profilepicture = view.findViewById(R.id.patient_profile_picture);
        remover_user = view.findViewById(R.id.remover_user_btn);

        email_field = view.findViewById(R.id.email_field);
        fullname_field = view.findViewById(R.id.fullname_field);
        mobile_field = view.findViewById(R.id.mobile_field);
        address_field = view.findViewById(R.id.address_field);

        rotateLoading = view.findViewById(R.id.rotateloading);

        saveLogin = loginPreferences.getBoolean("savepassword", false);

        if (saveLogin) {
            pass = loginPreferences.getString("pass", "");
        }

        save_card.setVisibility(View.GONE);
        email_field.setEnabled(false);
        fullname_field.setEnabled(false);
        mobile_field.setEnabled(false);
        address_field.setEnabled(false);
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
                address_txt = address_field.getText().toString();

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

                if (TextUtils.isEmpty(address_txt)) {
                    Toast.makeText(getContext(), "please enter your address", Toast.LENGTH_SHORT).show();
                    return;
                }

                save_card.setVisibility(View.GONE);
                fullname_field.setEnabled(false);
                mobile_field.setEnabled(false);
                address_field.setEnabled(false);
                profilepicture.setEnabled(false);
                savechanges_btn.setEnabled(false);
                edit_profile_btn.setEnabled(true);

                if (photoPath == null) {
                    UpdatePatientProfile(full_name_txt, email_txt, mobile_txt, special, address_txt, profile_image_url);
                } else {
                    uploadImage(full_name_txt, email_txt, mobile_txt, special, address_txt);
                }
            }
        });

        profilepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1, 1)
                        .start(getContext(), ParamedicProfileFragment.this);
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

        edit_profile_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save_card.setVisibility(View.VISIBLE);
                fullname_field.setEnabled(true);
                mobile_field.setEnabled(true);
                address_field.setEnabled(true);
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

    public void returndata() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.keepSynced(true);

        final String userId = user.getUid();

        mDatabase.child("AllUsers").child("Paramedics").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        ParamedicModel paramedicModel = dataSnapshot.getValue(ParamedicModel.class);

                        special = paramedicModel.getWork_place();
                        nfcid_txt.setText(special);

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
                        Toast.makeText(getContext(), "can't fetch data", Toast.LENGTH_SHORT).show();
                        rotateLoading.stop();
                    }
                });
    }

    private void dialContactPhone(final String phoneNumber) {
        startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null)));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void UpdatePatientProfile(String fullname, String email, String mobile, String spec, String address, String imageurl) {
        ParamedicModel paramedicModel = new ParamedicModel(fullname, email, mobile, spec, address, imageurl);

        databaseReference.child("Paramedics").child(special).child(getUid()).setValue(paramedicModel);
        databaseReference.child("AllUsers").child("Paramedics").child(getUid()).setValue(paramedicModel);

        Toast.makeText(getContext(), "saved", Toast.LENGTH_SHORT).show();

        if (photoPath == null) {
            returndata();
        }
    }

    private void uploadImage(final String fullname, final String email, final String mobile, final String spec, final String address) {
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

                UpdatePatientProfile(fullname, email, mobile, spec, address, selected_placeimaeURL);

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

                databaseReference.child("Paramedics").child(special).child(getUid()).removeValue();
                databaseReference.child("AllUsers").child("Paramedics").child(getUid()).removeValue();

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
}
