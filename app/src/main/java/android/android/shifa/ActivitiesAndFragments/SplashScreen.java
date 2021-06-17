package android.android.shifa.ActivitiesAndFragments;

import android.android.shifa.AdminApp.AdminMainActivity;
import android.android.shifa.DoctorApp.DoctorMainActivity;
import android.android.shifa.ParamedicApp.ParamedicMainActivity;
import android.android.shifa.PateintApp.PatientMainActivity;
import android.android.shifa.R;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Timer;
import java.util.TimerTask;

public class SplashScreen extends AppCompatActivity {
    ImageView imageView;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        imageView = findViewById(R.id.splash);

        //bindLogo();
        imageView.setScaleX(0);
        imageView.setScaleY(0);

        imageView.animate().scaleXBy(1).scaleYBy(1).setDuration(3000);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            category();
        } else {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), CardWizardActivity.class);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        }
    }

    public void category() {
        final String id = getUID();
        FirebaseDatabase firebaseDatabase;
        final DatabaseReference databaseReference;

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        databaseReference.child("AllUsers").child("Doctors").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(id)) {
                            //Toast.makeText(getContext(), "doctor : " + id, Toast.LENGTH_SHORT).show();
                            updateDoctorUI();
                        } else {
                            databaseReference.child("AllUsers").child("Patients").addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(id)) {
                                                //Toast.makeText(getContext(), "patient : " + id, Toast.LENGTH_SHORT).show();
                                                updatePatientUI();
                                            } else {
                                                databaseReference.child("AllUsers").child("Paramedics").addListenerForSingleValueEvent(
                                                        new ValueEventListener() {
                                                            @Override
                                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                                if (dataSnapshot.hasChild(id)) {
                                                                    updateParamedicUI();
                                                                    //Toast.makeText(getApplicationContext(), "paramedic : " + id, Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    updateAdminUI();
                                                                }
                                                            }

                                                            @Override
                                                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                                                Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                                            }
                                                        }
                                                );
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                            Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                            );
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Toast.makeText(getApplicationContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void updateDoctorUI() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), DoctorMainActivity.class);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        } else {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), NoInternetActivity.class);
                    i.putExtra("doctor", 0);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        }
    }

    public void updatePatientUI() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), PatientMainActivity.class);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        } else {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), NoInternetActivity.class);
                    i.putExtra("doctor", 1);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        }
    }

    public void updateParamedicUI() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), ParamedicMainActivity.class);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        } else {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), NoInternetActivity.class);
                    i.putExtra("doctor", 2);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        }
    }

    public void updateAdminUI() {
        ConnectivityManager cm =
                (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if (isConnected) {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), AdminMainActivity.class);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        } else {
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // go to the main activity
                    Intent i = new Intent(getApplicationContext(), NoInternetActivity.class);
                    i.putExtra("doctor", 3);
                    startActivity(i);
                    // kill current activity
                    finish();
                }
            };
            // Show splash screen for 3 seconds
            new Timer().schedule(task, 3000);
        }
    }

    private String getUID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String UserID = user.getUid();

        return UserID;
    }

    @Override
    public void onBackPressed() {

    }
}
