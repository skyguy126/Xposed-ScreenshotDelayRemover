package com.skyguy126.screenshotdelayremover;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    private int delayValue = 0;
    private boolean hiddenFromLauncher = false;

    private Context context;
    private CheckBox checkBox;
    private ComponentName alias;
    private SharedPreferences prefs;
    private TextView delayValueText;
    private SeekBar delayValueSlider;
    private SharedPreferences.Editor prefsEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        context = super.getApplicationContext();
        alias = new ComponentName(context, Shared.PACKAGE_NAME + ".MainActivity-Alias");
        prefs = getSharedPreferences(Shared.PREFS_FILE_NAME, MODE_WORLD_READABLE);
        prefsEditor = prefs.edit();
        loadValues();

        delayValueText = (TextView) findViewById(R.id.delayValueText);
        delayValueSlider = (SeekBar) findViewById(R.id.delayValueSlider);
        delayValueText.setText(getString(R.string.delayValueText, delayValue));
        delayValueSlider.setProgress(delayValue);
        delayValueSlider.setOnSeekBarChangeListener(this);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        checkBox.setChecked(hiddenFromLauncher);
    }

    public void loadValues() {
        delayValue = prefs.getInt("delay", 0);
        hiddenFromLauncher = prefs.getBoolean("hidden", false);
    }

    public void saveValues(View view) {
        hiddenFromLauncher = checkBox.isChecked();

        prefsEditor.putInt("delay", delayValue);
        prefsEditor.putBoolean("hidden", hiddenFromLauncher);
        prefsEditor.apply();

        if (hiddenFromLauncher) {
            getPackageManager().setComponentEnabledSetting(alias,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            getPackageManager().setComponentEnabledSetting(alias,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        Toast.makeText(context, "Reboot to apply changes!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(SeekBar arg0, int progress, boolean fromTouch) {
        delayValue = progress;
        delayValueText.setText(getString(R.string.delayValueText, delayValue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar arg0) {}

    @Override
    public void onStopTrackingTouch(SeekBar arg0) {}

    public void openSource(View view) {
        openWebsite(Shared.SOURCE_LINK);
    }

    public void openSupport(View view) {
        openWebsite(Shared.SUPPORT_LINK);
    }

    public void openWebsite(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            intent.setPackage(null);
            context.startActivity(intent);
        }
    }
}
