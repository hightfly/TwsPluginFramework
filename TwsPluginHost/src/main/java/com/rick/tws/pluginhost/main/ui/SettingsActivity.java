package com.rick.tws.pluginhost.main.ui;

import android.app.ActivityManager;
import android.app.TwsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rick.tws.pluginhost.R;
import com.rick.tws.pluginhost.debug.DebugPluginActivity;
import com.rick.tws.pluginhost.main.content.TestBundleObject;

import qrom.component.log.QRomLog;

public class SettingsActivity extends TwsActivity implements OnClickListener {

    private static final String TAG = "SettingsActivity";
    private RelativeLayout mPrivacyLayout, mDebugPluginFramework;
    private View mAccountLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sttings);
        initTwsActionBar(true);
        setTitle(R.string.settings);

        Intent intent = getIntent();
//        TestBundleObject testObjcet = (TestBundleObject) intent.getSerializableExtra(TestBundleObject.INTENT_EXTRA_NAME);
        TestBundleObject testObjcet = (TestBundleObject) intent.getParcelableExtra(TestBundleObject.INTENT_EXTRA_NAME);
        QRomLog.i(TAG, "getted testObjcet is " + testObjcet);
        Toast.makeText(this, "" + testObjcet, Toast.LENGTH_SHORT).show();
        if (null != intent) {
            String intentClassLoader = "intent ClassLoader is " + intent.getClass().getClassLoader();
            QRomLog.i(TAG, intentClassLoader);
        }

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
