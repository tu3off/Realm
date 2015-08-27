package com.chi.realmexample.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.chi.realmexample.model.Model;

import java.util.List;
import java.util.concurrent.Executor;

import io.realm.Realm;

public final class RealmSyncAsync {

    private final Realm readableRealm;
    private final Executor executor;
    private final Context context;
    private final Handler mainHandler;

    public RealmSyncAsync(Executor pExecutor, Context pContext) {
        readableRealm = Realm.getInstance(pContext);
        executor = pExecutor;
        context = pContext;
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public Realm getReadableRealm() {
        return readableRealm;
    }

    public interface OnBaseSaveCallback {
        void onSave();
    }

    public void saveModels(final OnBaseSaveCallback pCallback, final List<Model> pModels) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                final Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealm(pModels);
                realm.commitTransaction();
                realm.close();
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        pCallback.onSave();
                    }
                });
            }
        });
    }

    public List<Model> readModels() {
        return readableRealm.where(Model.class).findAll();
    }

}
