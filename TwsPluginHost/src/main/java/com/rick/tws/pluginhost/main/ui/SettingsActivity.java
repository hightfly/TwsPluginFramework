package com.rick.tws.pluginhost.main.ui;

import android.app.ActionBar;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rick.tws.pluginhost.R;
import com.rick.tws.pluginhost.debug.DebugPluginActivity;

public class SettingsActivity extends AppCompatActivity implements OnClickListener {

    private RelativeLayout mPrivacyLayout, mDebugPluginFramework;
    private View mAccountLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initActionBar();

        setContentView(R.layout.activity_sttings);
        mPrivacyLayout = (RelativeLayout) findViewById(R.id.about_watch_assistant_privacy_layout);
        mAccountLogout = findViewById(R.id.settings_account_logout_btn);
        mDebugPluginFramework = (RelativeLayout) findViewById(R.id.debug_plugin_framework);
        if (ActivityManager.isUserAMonkey()) {
            mAccountLogout.setEnabled(false);
        }

        mDebugPluginFramework.setVisibility(View.VISIBLE);
        mDebugPluginFramework.setOnClickListener(this);
        mPrivacyLayout.setOnClickListener(this);
        mAccountLogout.setOnClickListener(this);
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        if (null != actionBar) {
            actionBar.show();
            actionBar.setTitle(getResources().getString(R.string.activity_settings_title));
            actionBar.setStackedBackgroundDrawable(getResources().getDrawable(R.mipmap.ab_solid_light_holo_opacity));
        } else {
            setTitle(getResources().getString(R.string.activity_settings_title));
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.debug_plugin_framework:
                Intent intent = new Intent(this, DebugPluginActivity.class);
                startActivity(intent);
                break;
            case R.id.about_watch_assistant_privacy_layout:
                Toast.makeText(this, "click PrivacyLayout", Toast.LENGTH_SHORT).show();
                break;
            case R.id.settings_account_logout_btn:
                Toast.makeText(this, "click AccountLogout", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
