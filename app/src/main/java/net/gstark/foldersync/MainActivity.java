package net.gstark.foldersync;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.DocumentsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;
import androidx.navigation.ui.AppBarConfiguration;

import net.gstark.foldersync.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import io.reactivex.rxjava3.core.Single;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    RxDataStore<Preferences> dataStore;
    Preferences.Key<String> URL_KEY = PreferencesKeys.stringKey("url");
    Preferences.Key<String> USERNAME_KEY = PreferencesKeys.stringKey("username");
    Preferences.Key<String> PASSWORD_KEY = PreferencesKeys.stringKey("password");
    Preferences.Key<String> LOCALDIR_KEY = PreferencesKeys.stringKey("localdir");

    final int READ_REQUEST_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        dataStore = new RxPreferenceDataStoreBuilder(getApplicationContext(), "settings").build();
        dataStore.data();

        setupTextHandler(R.id.urlField, URL_KEY, "https://");
        setupTextHandler(R.id.usernameField, USERNAME_KEY, "");
        setupTextHandler(R.id.passwordField, PASSWORD_KEY, "");

        Button dirSelector = findViewById(R.id.selectLocalDirButton);
        dirSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, loadValue(LOCALDIR_KEY, ""));
                startActivityForResult(intent, READ_REQUEST_CODE);
            }
        });

        Button downloadButton = findViewById(R.id.runDownloadButton);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                WebdavHandler webdavHandler = new WebdavHandler(
                        loadValue(URL_KEY, "https://"),
                        loadValue(USERNAME_KEY, ""),
                        loadValue(PASSWORD_KEY, ""),
                        loadValue(LOCALDIR_KEY, "content://com.android.externalstorage.documents/tree/primary%3AFolderSync"),
                        getApplicationContext()
                );
                webdavHandler.downloadFiles();
            }
        });

        Log.i("MainActivity", "external: " + android.os.Environment.getExternalStorageDirectory().toString());
    }

    public void setupTextHandler(@IdRes int id, Preferences.Key<String> key, String defaultValue) {
        EditText field = findViewById(id);

        field.setText(loadValue(key, defaultValue));

        field.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                storeValue(key, s.toString());
            }
        });
    }

    private String loadValue(Preferences.Key<String> key, String defaultValue) {
        Single<String> value = dataStore.data().firstOrError().map(prefs -> prefs.get(key)).onErrorReturnItem(defaultValue);
        return value.blockingGet();
    }

    private void storeValue(Preferences.Key<String> key, String value) {
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);

        if (requestCode == READ_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i("FolderSync", uri.toString());
                storeValue(LOCALDIR_KEY, uri.toString());
            }
        }
    }
}