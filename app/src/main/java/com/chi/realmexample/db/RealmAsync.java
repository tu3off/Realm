package com.chi.realmexample.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.chi.realmexample.model.Model;

import java.util.List;
import java.util.concurrent.Executor;

import io.realm.Realm;

public final class RealmAsync implements Runnable {

    private Realm realm;
    private Handler handler;
    private final Handler mainHandler;
    private final Context context;

    public RealmAsync(Executor pExecutor, Context pContext) {
        context = pContext;
        mainHandler = new Handler(Looper.getMainLooper());
        pExecutor.execute(this);
    }

    @Override
    public void run() {
        Looper.prepare();
        try {
            realm = Realm.getInstance(context);
            handler = new Handler();
            Looper.loop();
        } finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    public interface OnBaseSaveCallback {
        void onSave();
    }

    public interface OnBaseReadCallback<T> {
        void onRead(T object);
    }

    public interface OnAsyncCloseCallback {
        void onClose();
    }

    public void close(final OnAsyncCloseCallback pCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm.close();
                try {
                    Looper.myLooper().quit();
                } finally {
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            pCallback.onClose();
                        }
                    });
                }
            }
        });
    }

    public void saveModels(final OnBaseSaveCallback pCallback, final List<Model> pModels) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                realm.beginTransaction();
                realm.copyToRealm(pModels);
                realm.commitTransaction();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pCallback.onSave();
                    }
                });
            }
        });
    }

    public void readModels(final OnBaseReadCallback<List<Model>> pCallback) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                final List<Model> models = realm.where(Model.class).findAll();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pCallback.onRead(models);
                    }
                });
            }
        });
    }

}
