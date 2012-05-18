package me.imid.movablecheckbox;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class MovableCheckboxActivity extends PreferenceActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.RGBA_8888);
        addPreferencesFromResource(R.xml.test4preference);
    }
}