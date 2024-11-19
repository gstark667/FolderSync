package net.gstark.foldersync;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import at.bitfire.dav4jvm.BasicDigestAuthHandler;
import at.bitfire.dav4jvm.DavCollection;
import at.bitfire.dav4jvm.MultiResponseCallback;
import at.bitfire.dav4jvm.PropStat;
import at.bitfire.dav4jvm.Property;
import at.bitfire.dav4jvm.ResponseCallback;
import at.bitfire.dav4jvm.XmlUtils;
import at.bitfire.dav4jvm.property.DisplayName;
import at.bitfire.dav4jvm.property.GetLastModified;
import at.bitfire.dav4jvm.property.ResourceType;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebdavHandler {
    BasicDigestAuthHandler authHandler;
    OkHttpClient okHttpClient;
    String root;
    String storageDir;
    Context context;
    public WebdavHandler(String address, String username, String password, String path, Context appContext) {
        authHandler = new BasicDigestAuthHandler(
                null,
                username,
                password,
                false
        );
        okHttpClient = new OkHttpClient.Builder()
                .followRedirects(false)
                .authenticator(authHandler)
                .addNetworkInterceptor(authHandler)
                .build();

        root = address;
        storageDir = path;
        context = appContext;
    }

    private String extractPath(HttpUrl url) {
        String tmp = url.toString();
        return tmp.substring(root.length());
    }

    private String extractDirectory(HttpUrl url) {
        String tmp = url.toString();
        String localPath = tmp.substring(root.length());
        return localPath.substring(0, localPath.lastIndexOf('/'));
    }

    private String extractFilename(HttpUrl url) {
        String tmp = url.toString();
        return tmp.substring(tmp.lastIndexOf('/') + 1);
    }

    private void makeDirectory(String path) {
        HttpUrl location = HttpUrl.parse(root + "/" + path);
        Log.i("WebdavHandler", "url=" + location.toString());
        DavCollection davCollection = new DavCollection(okHttpClient, location);

        Log.i("WebdavHandler", "makeDirectory: " + davCollection.fileName());
        try {
            davCollection.mkCol("", new ResponseCallback() {
                @Override
                public void onResponse(@NonNull Response response) {
                    Log.i("WebdavHandler", "mkCol Success: " + path);
                }
            });
        } catch (Exception e) {
            Log.e("WebdavHandler", "mkCol", e);
        }
    }

    private void uploadFile(String path, String content) {
        HttpUrl location = HttpUrl.parse(root + "/" + path);
        DavCollection davCollection = new DavCollection(okHttpClient, location);

        Log.i("WebdavHandler", "root=" + root);
        try {
            davCollection.put(
                    RequestBody.create(content, MediaType.parse("text/plain")),
                    null,
                    null,
                    false,
                    new ResponseCallback() {
                @Override
                public void onResponse(@NonNull Response response) {
                    Log.i("WebdavHandler", "Put Success: " + path);
                }
            });
        } catch (Exception e) {
            Log.e("WebdavHandler", "put", e);
        }
    }

    private void downloadFile(HttpUrl location, long time) {
        DavCollection davCollection = new DavCollection(okHttpClient, location);

        davCollection.get("", null, response -> {
            if (response.body() != null) {
                try {
                    Log.i("WebdavHandler", storageDir + extractPath(location));
                    File file = new File(storageDir + extractPath(location));

                    // doesn't do anything useful yet
                    if (file.lastModified() > time) {
                        Log.i("WebdavHandler", "last modified newer than server");
                    }

                    // write the file
                    FileOutputStream fos = new FileOutputStream(file);
                    fos.write(response.body().bytes());
                    fos.close();

                    // update with the server-side time
                    file.setLastModified(time);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                Log.e("WebdavHandler", "get failed");
            }
        });
    }
    public void downloadFiles() {
        // DAV examples:
        new Thread(() -> {
            HttpUrl location = HttpUrl.parse(root);
            DavCollection davCollection = new DavCollection(okHttpClient, location);
            try {
                davCollection.propfind(2, new Property.Name[]{DisplayName.NAME, GetLastModified.NAME, ResourceType.NAME}, new MultiResponseCallback() {

                    @Override
                    public void onResponse(@NonNull at.bitfire.dav4jvm.Response response, @NonNull at.bitfire.dav4jvm.Response.HrefRelation hrefRelation) {
                        Log.i("WebdavHandler", "propfind: " + response.toString());
                        GetLastModified modified = (GetLastModified) response.getProperties().get(1);
                        ResourceType type = (ResourceType) response.getProperties().get(2);

                        if (type.getTypes().contains(new Property.Name(XmlUtils.NS_WEBDAV, "collection"))) {
                            Log.i("WebdavHandler", "directory: " + storageDir + extractPath(response.getHref()));
                            new File(storageDir + extractPath(response.getHref())).mkdirs();
                        } else {
                            downloadFile(response.getHref(), modified.getLastModified());
                        }
                    }
                });
            } catch (Exception e) {
                Log.e("WebdavHandler", "find", e);
            }
        }).start();
    }
}
