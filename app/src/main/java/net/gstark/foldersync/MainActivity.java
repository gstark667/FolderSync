package net.gstark.foldersync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import net.gstark.foldersync.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    SettingsStore settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        settings = new SettingsStore(getApplicationContext());

        setupTextHandler(R.id.urlField, settings.URL_KEY, "https://");
        setupTextHandler(R.id.usernameField, settings.USERNAME_KEY, "");
        setupTextHandler(R.id.passwordField, settings.PASSWORD_KEY, "");
        setupTextHandler(R.id.localDirField, settings.LOCALDIR_KEY, settings.external);

        Button downloadButton = findViewById(R.id.runDownloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebdavHandler webdavHandler = new WebdavHandler(
                        settings.loadValue(settings.URL_KEY, "https://"),
                        settings.loadValue(settings.USERNAME_KEY, ""),
                        settings.loadValue(settings.PASSWORD_KEY, ""),
                        settings.loadValue(settings.LOCALDIR_KEY, settings.external),
                        getApplicationContext()
                );
                webdavHandler.downloadFiles();
            }
        });

        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            Uri uri = Uri.fromParts("package", this.getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        }

        Log.i("MainActivity", "external: " + android.os.Environment.getExternalStorageDirectory().toString());
    }

    public void setupTextHandler(@IdRes int id, Preferences.Key<String> key, String defaultValue) {
        EditText field = findViewById(id);

        field.setText(settings.loadValue(key, defaultValue));

        field.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                settings.storeValue(key, s.toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}