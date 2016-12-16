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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = super.getApplicationContext();
        alias = new ComponentName(context, Shared.PACKAGE_NAME + ".MainActivity-Alias");
        prefs = getSharedPreferences(Shared.PREFS_FILE_NAME, MODE_WORLD_READABLE);

        this.delayValue = prefs.getInt("delay", 0);
        this.hiddenFromLauncher = prefs.getBoolean("hidden", false);

        SeekBar delayValueSlider = (SeekBar) findViewById(R.id.delayValueSlider);
        if (delayValueSlider != null) {

            delayValueSlider.setProgress(delayValue);
            delayValueSlider.setOnSeekBarChangeListener(this);
        }

        delayValueText = (TextView) findViewById(R.id.delayValueText);
        if (delayValueText != null)
            delayValueText.setText(getString(R.string.delay_value_text, delayValue));

        checkBox = (CheckBox) findViewById(R.id.checkBox);
        if (checkBox != null)
            checkBox.setChecked(hiddenFromLauncher);
    }

    public void saveValues(View view) {
        this.hiddenFromLauncher = checkBox.isChecked();
        prefs.edit().putInt("delay", this.delayValue).putBoolean("hidden", this.hiddenFromLauncher).apply();

        if (this.hiddenFromLauncher) {
            getPackageManager().setComponentEnabledSetting(alias,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
        } else {
            getPackageManager().setComponentEnabledSetting(alias,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
        }

        Toast.makeText(context, "Reboot to apply changes!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProgressChanged(SeekBar s, int progress, boolean fromTouch) {
        this.delayValue = progress;
        delayValueText.setText(getString(R.string.delay_value_text, this.delayValue));
    }

    @Override
    public void onStartTrackingTouch(SeekBar s) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar s) {
    }

    public void openSource(View view) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Shared.SOURCE_LINK));
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
