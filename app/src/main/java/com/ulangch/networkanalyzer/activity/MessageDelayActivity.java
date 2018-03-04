package com.ulangch.networkanalyzer.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.annotation.Nullable;

import com.ulangch.networkanalyzer.R;

/**
 * Created by ulangch on 18-3-4.
 */

public class MessageDelayActivity extends PreferenceActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_message_delay);
    }
}
