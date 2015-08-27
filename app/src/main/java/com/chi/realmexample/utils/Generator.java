package com.chi.realmexample.utils;

import android.os.Handler;
import android.os.Looper;

import com.chi.realmexample.model.Model;
import com.chi.realmexample.model.SubModel;

import java.util.concurrent.Executors;

import io.realm.RealmList;

public final class Generator {

    private Generator() {
    }

    public interface OnGenerateCallback {
        void onGenerate(RealmList<Model> pRealmList);
    }

    public static void generateDemoModels(final OnGenerateCallback pGenerateCallback, final int pCount) {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                final RealmList<Model> models = new RealmList<>();
                for (int i = 0; i < pCount; i++) {
                    final Model model = new Model();
                    final RealmList<SubModel> subModels = generateDemoSubModels(pCount);
                    model.setSubModels(subModels);
                    model.setA0(String.valueOf(i));
                    model.setA1(String.valueOf(i));
                    model.setA2(String.valueOf(i));
                    model.setA3(String.valueOf(i));
                    model.setA4(String.valueOf(i));
                    model.setI0(i);
                    model.setI1(i);
                    model.setI2(i);
                    model.setI3(i);
                    model.setI4(i);
                    models.add(model);
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        pGenerateCallback.onGenerate(models);
                    }
                });
            }
        });

    }

    private static RealmList<SubModel> generateDemoSubModels(int pCount) {
        final RealmList<SubModel> subModels = new RealmList<>();
        pCount = pCount / 2;
        for (int i = 0; i < pCount; i++) {
            final SubModel subModel = new SubModel();
            subModel.setA0(String.valueOf(i));
            subModel.setA1(String.valueOf(i));
            subModel.setA2(String.valueOf(i));
            subModel.setA3(String.valueOf(i));
            subModel.setA4(String.valueOf(i));
            subModel.setI0(i);
            subModel.setI1(i);
            subModel.setI2(i);
            subModel.setI3(i);
            subModel.setI4(i);
            subModels.add(subModel);
        }
        return subModels;
    }

}
