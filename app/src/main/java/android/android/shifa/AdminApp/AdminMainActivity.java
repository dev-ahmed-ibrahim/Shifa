package android.android.shifa.AdminApp;

import android.android.shifa.AdminApp.AdminFragments.DoctorsFragment;
import android.android.shifa.AdminApp.AdminFragments.ParamedicsFragment;
import android.android.shifa.AdminApp.AdminFragments.PatientsFragment;
import android.android.shifa.AdminApp.AdminFragments.ProfileFragment;
import android.android.shifa.AdminApp.AdminFragments.RequestsFragment;
import android.android.shifa.R;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AdminMainActivity extends AppCompatActivity {
    private static final String CHANNEL_ID = "simplified_coding";
    private static final String CHANNEL_NAME = "simplified coding";
    private static final String CHANNEL_DESC = "simplified coding Notification";
    BottomNavigationView navigation;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_main);

        navigation = findViewById(R.id.navigation);

        fragmentManager = getSupportFragmentManager();

        Fragment requestsFragment = new RequestsFragment();
        loadFragment(requestsFragment);

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.admin_requests:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment requestsFragment1 = new RequestsFragment();
                        loadFragment(requestsFragment1);
                        return true;
                    case R.id.admin_doctors:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment doctorsFragment = new DoctorsFragment();
                        loadFragment(doctorsFragment);
                        return true;
                    case R.id.admin_patients:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment patientsFragment = new PatientsFragment();
                        loadFragment(patientsFragment);
                        return true;
                    case R.id.admin_paramedics:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment paramedicsFragment = new ParamedicsFragment();
                        loadFragment(paramedicsFragment);
                        return true;
                    case R.id.admin_profile:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment profileFragment = new ProfileFragment();
                        loadFragment(profileFragment);
                        return true;
                }
                return false;
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription(CHANNEL_DESC);
            NotificationManager notification = getSystemService(NotificationManager.class);
            notification.createNotificationChannel(channel);
        }

        getReq();
    }

    public void loadFragment(Fragment fragment) {
        fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);

        getFragmentManager().popBackStack();
        // Commit the transaction
        fragmentTransaction.commit();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finishAffinity();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onBackPressed() {
        doExitApp();
    }

    @Override
    protected void onStop() {
        super.onStop();

        getReq();
    }

    private void getReq() {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        databaseReference.child("AllRequests").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                notification("New Request");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void notification(String message) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(AdminMainActivity.this, CHANNEL_ID)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_foreground))
                        .setSmallIcon(R.mipmap.ic_launcher_foreground)
                        .setContentTitle("Shifa")
                        .setContentText(message)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(AdminMainActivity.this);

        notificationManagerCompat.notify(1, mBuilder.build());
    }
}
