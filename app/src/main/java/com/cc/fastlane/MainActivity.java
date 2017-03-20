package com.cc.fastlane;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;

import io.fabric.sdk.android.Fabric;

/****
 * This class is used to show version name & version code
 */
public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());
        setContentView(R.layout.activity_main);
        setupReference();
        showVersionNameAndCode();

    }

    private void setupReference() {
        textView = (TextView) findViewById(R.id.text_helloWorld);
    }

    private void showVersionNameAndCode() {
        textView.setText(getDisplayTextForApplication());
    }

    private String getDisplayTextForApplication() {
        int versionCode = BuildConfig.VERSION_CODE;
        String versionName = BuildConfig.VERSION_NAME;
        return new StringBuilder(41).append("Version Code: ").append(versionCode)
                .append("\n").append("Version Name: ")
                .append(versionName).toString();
    }
}
