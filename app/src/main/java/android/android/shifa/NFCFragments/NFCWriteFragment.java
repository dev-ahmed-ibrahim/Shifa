package android.android.shifa.NFCFragments;

import android.android.shifa.ActivitiesAndFragments.NFCActivity;
import android.android.shifa.Listener;
import android.android.shifa.R;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.victor.loading.rotate.RotateLoading;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class NFCWriteFragment extends DialogFragment {
    public static final String TAG = NFCWriteFragment.class.getSimpleName();
    TextView mTvMessage;
    RotateLoading rotateLoading;
    Listener mListener;

    public static NFCWriteFragment newInstance() {
        return new NFCWriteFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_write, container, false);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        initViews(view);
        return view;
    }

    private void initViews(View view) {
        mTvMessage = view.findViewById(R.id.tv_message);
        rotateLoading = view.findViewById(R.id.rotateloading);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mListener = (NFCActivity) context;
        mListener.onDialogDisplayed();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onDialogDismissed();
    }

    public void onNfcDetected(Ndef ndef, String messageToWrite) {
        rotateLoading.start();
        writeToNfc(ndef, messageToWrite);
    }

    private void writeToNfc(Ndef ndef, String message) {
        mTvMessage.setText(getString(R.string.message_write_progress));
        if (ndef != null) {

            try {
                ndef.connect();
                NdefRecord mimeRecord = NdefRecord.createMime("text/plain", message.getBytes(StandardCharsets.US_ASCII));
                ndef.writeNdefMessage(new NdefMessage(mimeRecord));
                ndef.close();
                //Write Successful
                mTvMessage.setText(getString(R.string.message_write_success));
                dismiss();

            } catch (IOException | FormatException e) {
                e.printStackTrace();
                mTvMessage.setText(getString(R.string.message_write_error));

            } finally {
                rotateLoading.stop();
            }
        }
    }
}
