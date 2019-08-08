package com.moez.QKSMS.common.view;

import android.app.DialogFragment;
import android.app.FragmentTransaction;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "dialog_fragment";

    public void showDailogFragment(DialogFragment fragment) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        fragment.show(ft, TAG);
    }
}
