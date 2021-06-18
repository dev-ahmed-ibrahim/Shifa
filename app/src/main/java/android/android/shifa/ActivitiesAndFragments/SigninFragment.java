package android.android.shifa.ActivitiesAndFragments;

import android.android.shifa.AdminApp.AdminMainActivity;
import android.android.shifa.DoctorApp.DoctorMainActivity;
import android.android.shifa.GuestApp.GuestActivity;
import android.android.shifa.ParamedicApp.ParamedicMainActivity;
import android.android.shifa.PateintApp.PatientMainActivity;
import android.android.shifa.R;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.victor.loading.rotate.RotateLoading;

import java.util.concurrent.Executor;

import static android.content.Context.MODE_PRIVATE;

public class SigninFragment extends Fragment {
    View view;

    EditText email, password;
    TextView forgotpassword;
    Button sign_in, guest;

    String email_txt, password_txt;

    FirebaseAuth auth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    // Firebase User
    FirebaseUser user;

    RotateLoading rotateLoading;

    CheckBox checkBox;
    SharedPreferences loginPreferences;
    SharedPreferences.Editor loginPrefsEditor;
    Boolean saveLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.sign_in_fragment, container, false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        loginPreferences = getActivity().getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPrefsEditor = loginPreferences.edit();

        auth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        email = view.findViewById(R.id.email_field);
        password = view.findViewById(R.id.password_field);
        sign_in = view.findViewById(R.id.sign_in_btn);
        guest = view.findViewById(R.id.guest_btn);
        forgotpassword = view.findViewById(R.id.forgot_password_txt);
        rotateLoading = view.findViewById(R.id.signinrotateloading);
        checkBox = view.findViewById(R.id.remember_me_checkbox);

        user = FirebaseAuth.getInstance().getCurrentUser();

        if(user != null)
        {
            fingerprint();
        }

        forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth auth = FirebaseAuth.getInstance();
                final String emailAddress = email.getText().toString();

                if (TextUtils.isEmpty(emailAddress)) {
                    Toast.makeText(getContext(), "please enter your email firstly", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (emailAddress.equals("admin@admin.com")) {
                    Toast.makeText(getContext(), "you can't reset admin account password", Toast.LENGTH_SHORT).show();
                    return;
                }

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getContext(), "password reset email has been sent to : " + emailAddress, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        saveLogin = loginPreferences.getBoolean("saveLogin", false);

        if (saveLogin) {
            email.setText(loginPreferences.getString("username", ""));
            password.setText(loginPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }

        sign_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email_txt = email.getText().toString();
                password_txt = password.getText().toString();

                if (TextUtils.isEmpty(email_txt)) {
                    Toast.makeText(getContext(), "please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password_txt)) {
                    Toast.makeText(getContext(), "please enter your password", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (email_txt.equals("admin@admin.com") && password_txt.equals("adminadmin")) {
                    rotateLoading.start();

                    AdminLogin(email_txt, password_txt);

                    loginPrefsEditor.putBoolean("savepassword", true);
                    loginPrefsEditor.putString("pass", password_txt);
                    loginPrefsEditor.apply();

                    if (checkBox.isChecked()) {
                        loginPrefsEditor.putBoolean("saveLogin", true);
                        loginPrefsEditor.putString("username", email_txt);
                        loginPrefsEditor.putString("password", password_txt);
                        loginPrefsEditor.apply();
                    } else {
                        loginPrefsEditor.clear();
                        loginPrefsEditor.apply();
                    }
                } else {
                    rotateLoading.start();

                    UserLogin(email_txt, password_txt);

                    loginPrefsEditor.putBoolean("savepassword", true);
                    loginPrefsEditor.putString("pass", password_txt);
                    loginPrefsEditor.apply();

                    if (checkBox.isChecked()) {
                        loginPrefsEditor.putBoolean("saveLogin", true);
                        loginPrefsEditor.putString("username", email_txt);
                        loginPrefsEditor.putString("password", password_txt);
                        loginPrefsEditor.apply();
                    } else {
                        loginPrefsEditor.clear();
                        loginPrefsEditor.apply();
                    }
                }
            }
        });

        guest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), GuestActivity.class);
                startActivity(intent);
            }
        });
    }

    private void UserLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            category();
                        } else {
                            rotateLoading.stop();
                            String taskmessage = task.getException().getMessage();
                            Toast.makeText(getContext(), taskmessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void AdminLogin(String email, String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            rotateLoading.stop();

                            Intent intent = new Intent(getContext(), AdminMainActivity.class);
                            startActivity(intent);
                        } else {
                            rotateLoading.stop();
                            String taskmessage = task.getException().getMessage();
                            Toast.makeText(getContext(), taskmessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void category() {
        final String id = getUID();
        databaseReference.child("AllUsers").child("Doctors").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(id)) {
                            rotateLoading.stop();
                            //Toast.makeText(getContext(), "doctor : " + id, Toast.LENGTH_SHORT).show();
                            updateDoctorUI();
                        } else {
                            databaseReference.child("AllUsers").child("Patients").addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)) {
                                                rotateLoading.stop();
                                                //Toast.makeText(getContext(), "patient : " + id, Toast.LENGTH_SHORT).show();
                                                updatePatientUI();
                                            } else {
                                                rotateLoading.stop();
                                                //Toast.makeText(getContext(), "paramedic : " + id, Toast.LENGTH_SHORT).show();
                                                updateParamedicUI();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            rotateLoading.stop();
                                            Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        rotateLoading.stop();
                        Toast.makeText(getContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateDoctorUI() {
        Intent intent = new Intent(getContext(), DoctorMainActivity.class);
        startActivity(intent);
    }

    public void updateParamedicUI() {
        Intent intent = new Intent(getContext(), ParamedicMainActivity.class);
        startActivity(intent);
    }

    public void updatePatientUI() {
        Intent intent = new Intent(getContext(), PatientMainActivity.class);
        startActivity(intent);
    }

    private String getUID() {
        user = auth.getCurrentUser();
        String UserID = user.getUid();

        return UserID;
    }

    private void fingerprint() {
        BiometricManager biometricManager = BiometricManager.from(requireContext());
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Toast.makeText(requireContext(), "You can use the fingerprint to login", Toast.LENGTH_LONG).show();
                if(user != null)
                {
                    fingerprintDialog();
                }
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Toast.makeText(requireContext(), "The device don't have a fingerprint sensor", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Toast.makeText(requireContext(), "The biometric sensor is currently unavailable", Toast.LENGTH_LONG).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Toast.makeText(requireContext(), "Your device don't have fingerprint saved please check security settings", Toast.LENGTH_LONG).show();
                break;
        }

    }

    private void fingerprintDialog() {

        Executor executor = ContextCompat.getMainExecutor(requireContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(SigninFragment.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull @org.jetbrains.annotations.NotNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
            }

            @Override
            public void onAuthenticationSucceeded(@NonNull @org.jetbrains.annotations.NotNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(requireContext(), "Login Success... ", Toast.LENGTH_LONG).show();


                category();


            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Login")
                .setDescription("Use your fingerprint to login \n or \n Use your face detected")
                .setNegativeButtonText("Cancel")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }
}
