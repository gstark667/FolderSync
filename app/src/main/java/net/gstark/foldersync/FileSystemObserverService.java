package net.gstark.foldersync;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.FileObserver;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FileSystemObserverService extends Service {
    SettingsStore settings;

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        settings = new SettingsStore(getApplicationContext());
        observe(settings.loadValue(settings.LOCALDIR_KEY, settings.external));
        return super.onStartCommand(intent, flags, startId);
    }

    public File getInternalStoragePath() {
        File parent = Environment.getExternalStorageDirectory().getParentFile();
        File external = Environment.getExternalStorageDirectory();
        Log.i("FileSystemObserver", external.getAbsolutePath());
        File[] files = parent.listFiles();
        File internal = null;
        if (files != null) {
            for (int i = 0; i < files.length; i++) {

                Log.i("FileSystemObserver", files[i].getName());
                if (files[i].getName().toLowerCase().startsWith("sdcard") && !files[i].equals(external)) {
                    internal = files[i];
                }
            }
        }

        return internal;
    }

    public File getExtenerStoragePath() {

        return Environment.getExternalStorageDirectory();
    }

    public void observe(String path) {
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                //File[]   listOfFiles = new File(path).listFiles();
                //File str = getInternalStoragePath();
                File str = new File(path);
                if (str != null) {
                    String internalPath = str.getAbsolutePath();

                    new Obsever(internalPath).startWatching();
                }
                /*str = getExtenerStoragePath();
                if (str != null) {

                    String externalPath = str.getAbsolutePath();
                    new Obsever(externalPath).startWatching();
                }*/


            }
        });
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    class Obsever extends FileObserver {
        List<SingleFileObserver> mObservers;
        String mPath;
        int mMask;

        public Obsever(String path) {
            // TODO Auto-generated constructor stub
            this(path, ALL_EVENTS);
        }

        public Obsever(String path, int mask) {
            super(path, mask);

            Log.i("Observer", path);
            mPath = path;
            mMask = mask;
            // TODO Auto-generated constructor stub

        }

        @Override
        public void startWatching() {
            // TODO Auto-generated method stub
            Log.i("Observer", "startWatching");
            if (mObservers != null)
                return;
            mObservers = new ArrayList<SingleFileObserver>();
            Stack<String> stack = new Stack<String>();
            stack.push(mPath);
            while (!stack.empty()) {
                String parent = stack.pop();
                mObservers.add(new SingleFileObserver(parent, mMask));
                File path = new File(parent);
                File[] files = path.listFiles();
                if (files == null) continue;
                for (int i = 0; i < files.length; ++i) {
                    if (files[i].isDirectory() && !files[i].getName().equals(".") && !files[i].getName().equals("..")) {
                        stack.push(files[i].getPath());
                    }
                }
            }
            for (int i = 0; i < mObservers.size(); i++) {
                mObservers.get(i).startWatching();
            }
        }

        @Override
        public void stopWatching() {
            // TODO Auto-generated method stub
            if (mObservers == null)
                return;
            for (int i = 0; i < mObservers.size(); ++i) {
                mObservers.get(i).stopWatching();
            }
            mObservers.clear();
            mObservers = null;
        }

        @Override
        public void onEvent(int event, final String path) {
            Log.i("Observer", path);
            if (event == FileObserver.OPEN) {
                Log.i("Observer", "file opened: " + path);
                //do whatever you want
            } else if (event == FileObserver.CREATE) {
                Log.i("Observer", "file created: " + path);
                //do whatever you want
            } else if (event == FileObserver.DELETE_SELF || event == FileObserver.DELETE) {

                //do whatever you want
            } else if (event == FileObserver.MOVE_SELF || event == FileObserver.MOVED_FROM || event == FileObserver.MOVED_TO) {
                //do whatever you want

            } else if (event == FileObserver.CLOSE_WRITE) {
                Log.i("Observer", "file written: " + path);
            }
        }

        private class SingleFileObserver extends FileObserver {
            private String mPath;

            public SingleFileObserver(String path, int mask) {
                super(path, mask);
                // TODO Auto-generated constructor stub
                mPath = path;
            }

            @Override
            public void onEvent(int event, String path) {
                // TODO Auto-generated method stub
                String newPath = mPath + "/" + path;
                Obsever.this.onEvent(event, newPath);
            }

        }

    }
}