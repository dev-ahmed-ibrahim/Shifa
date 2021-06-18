package android.android.shifa.PateintApp;

import android.android.shifa.ActivitiesAndFragments.DoctorsFragment;
import android.android.shifa.ActivitiesAndFragments.SignupFragment;
import android.android.shifa.PateintApp.PatientFragments.PatientProfileFragment;
import android.android.shifa.R;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PatientMainActivity extends AppCompatActivity {
    BottomNavigationView navigation;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patient_activity_main);

        navigation = findViewById(R.id.navigation);

        fragmentManager = getSupportFragmentManager();

        int i = getIntent().getIntExtra(SignupFragment.EXTRA_PROFILE_TAG, 0);

        if (i == 123) {
            Fragment profile = new PatientProfileFragment();
            loadFragment(profile);
            navigation.setSelectedItemId(R.id.profile);
        } else {
            Fragment doctors = new DoctorsFragment();
            loadFragment(doctors);

        }

        navigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.doctors:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment doctors = new DoctorsFragment();
                        loadFragment(doctors);
                        return true;
                    case R.id.profile:
                        //navigation.setBackgroundColor(getResources().getColor(R.color.blue_grey_700));
                        Fragment profile = new PatientProfileFragment();
                        loadFragment(profile);
                        return true;
                }
                return false;
            }
        });
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
}
