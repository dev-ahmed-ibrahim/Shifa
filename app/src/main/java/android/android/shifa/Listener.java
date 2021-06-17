package android.android.shifa;

import android.content.Intent;

public interface Listener {
    void onDialogDisplayed();

    void onDialogDismissed();

    void nfc_id(String id);

    void patient_name(String name);

    void patient_number(String number);

    void patient_bloodtype(String bloodtype);

    void patient_disease(String disease);

    void onNewIntent(Intent intent);
}
