package net.gstark.foldersync;

import android.content.Context;
import android.os.Environment;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import io.reactivex.rxjava3.core.Single;

public class SettingsStore {
    RxDataStore<Preferences> dataStore;
    Preferences.Key<String> URL_KEY = PreferencesKeys.stringKey("url");
    Preferences.Key<String> USERNAME_KEY = PreferencesKeys.stringKey("username");
    Preferences.Key<String> PASSWORD_KEY = PreferencesKeys.stringKey("password");
    Preferences.Key<String> LOCALDIR_KEY = PreferencesKeys.stringKey("localdir");

    final String external = Environment.getExternalStorageDirectory().toString();

    SettingsStore(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context, "settings").build();
        dataStore.data();
    }

    public String loadValue(Preferences.Key<String> key, String defaultValue) {
        Single<String> value = dataStore.data().firstOrError().map(prefs -> prefs.get(key)).onErrorReturnItem(defaultValue);
        return value.blockingGet();
    }

    public void storeValue(Preferences.Key<String> key, String value) {
        Single<Preferences> updateResult =  dataStore.updateDataAsync(prefsIn -> {
            MutablePreferences mutablePreferences = prefsIn.toMutablePreferences();
            mutablePreferences.set(key, value);
            return Single.just(mutablePreferences);
        });
    }
}
