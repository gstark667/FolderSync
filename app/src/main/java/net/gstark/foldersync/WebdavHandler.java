package net.gstark.foldersync;

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import at.bitfire.dav4jvm.BasicDigestAuthHandler;
import at.bitfire.dav4jvm.DavCollection;
import at.bitfire.dav4jvm.MultiResponseCallback;
import at.bitfire.dav4jvm.ResponseCallback;
import at.bitfire.dav4jvm.exception.DavException;
import at.bitfire.dav4jvm.exception.HttpException;
import at.bitfire.dav4jvm.property.DisplayName;
import at.bitfire.dav4jvm.property.GetLastModified;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebdavHandler {
    public void downloadFiles(String address, String username, String password) {
        // DAV examples:
        new Thread(() -> {
            BasicDigestAuthHandler authHandler = new BasicDigestAuthHandler(
                    null,
                    username,
                    password,
                    false
            );
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .followRedirects(false)
                    .authenticator(authHandler)
                    .addNetworkInterceptor(authHandler)
                    .build();

            HttpUrl location = HttpUrl.parse(address);
            DavCollection davCollection = new DavCollection(okHttpClient, location);
            try {
                davCollection.put(RequestBody.create("World", MediaType.parse("text/plain")), null, null, false, new ResponseCallback() {
                    @Override
                    public void onResponse(@NonNull Response response) {
                        Log.i("WebdavHandler", "Put Success");
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (HttpException e) {
                throw new RuntimeException(e);
            }
            /*davCollection.put(RequestBody.create("World", MediaType.parse("text/plain")), response -> {
                Log.i("MainActivity", "put");
            });*/

            davCollection.get("", null, response -> {
                if (response.body() != null) {
                    try {
                        Log.i("MainActivity", response.body().string());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            location = HttpUrl.parse("");
            davCollection = new DavCollection(okHttpClient, location);
            try {
                davCollection.propfind(1, new at.bitfire.dav4jvm.Property.Name[]{DisplayName.NAME, GetLastModified.NAME}, new MultiResponseCallback() {

                    @Override
                    public void onResponse(@NonNull at.bitfire.dav4jvm.Response response, @NonNull at.bitfire.dav4jvm.Response.HrefRelation hrefRelation) {

                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (DavException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}
