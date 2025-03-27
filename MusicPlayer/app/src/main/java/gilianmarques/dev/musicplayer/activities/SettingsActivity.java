package gilianmarques.dev.musicplayer.activities;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Switch;

import com.pixplicity.easyprefs.library.Prefs;

import gilianmarques.dev.musicplayer.R;
import gilianmarques.dev.musicplayer.persistence.c;
import gilianmarques.dev.musicplayer.utils.App;

public class SettingsActivity extends MyActivity {
    private Switch switch1, switch2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        setData();
    }

    @Override protected void applyTheme(Window mWindow, View decorView) {
        setTheme(darkTheme ? R.style.AppThemeDark : R.style.AppThemeLight);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }


    private void initViews() {
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
    }

    private void setData() {
        switch1.setChecked(MyActivity.darkTheme);
        switch2.setChecked(Prefs.getBoolean(c.can_use_mobile_data, false));
    }




    private void applyChanges() {
        Prefs.putBoolean(c.dark_theme, switch1.isChecked());
        if (MyActivity.darkTheme != switch1.isChecked()) App.binder.get().reboot();
        Prefs.putBoolean(c.can_use_mobile_data, switch2.isChecked());
    }


    @Override protected void onStop() {
        applyChanges();
        super.onStop();
    }
}
