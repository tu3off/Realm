package com.chi.realmexample.db;

import android.content.Context;

import com.chi.realmexample.model.Model;

import java.util.List;

import io.realm.Realm;

public final class RealmSync {

    private final Realm realm;

    public RealmSync(Context pContext) {
        realm = Realm.getInstance(pContext);
    }

    public Realm getRealm() {
        return realm;
    }

    public void saveModels(List<Model> pModel) {
        realm.beginTransaction();
        realm.copyToRealm(pModel);
        realm.commitTransaction();
    }

    public List<Model> readModels() {
        return realm.where(Model.class).findAll();
    }

}
