package com.chi.realmexample.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.chi.realmexample.R;
import com.chi.realmexample.db.RealmAsync;
import com.chi.realmexample.db.RealmSync;
import com.chi.realmexample.db.RealmSyncAsync;
import com.chi.realmexample.model.Model;
import com.chi.realmexample.utils.Generator;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;

public final class MainActivity extends BaseActivity {

    private static final int THREAD_POOL_SIZE = 5;

    private enum Operation {
        READ, WRITE;
    }

    private RadioGroup rgOperations;
    private RadioGroup rgNumberOfRecords;
    private TextView tvNumberOfRecords;
    private RealmConfiguration realmConfiguration;
    private RealmSync realmSync;
    private RealmAsync realmAsync;
    private RealmSyncAsync realmSyncAsync;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        initRealm();
        final ChooseRealmConfigurationListener chooseRealmConfigurationListener
                = new ChooseRealmConfigurationListener();
        rgOperations = (RadioGroup) findViewById(R.id.rgOperations);
        rgOperations.setOnCheckedChangeListener(chooseRealmConfigurationListener);
        rgNumberOfRecords = (RadioGroup) findViewById(R.id.rgNumberOfRecords);
        tvNumberOfRecords = (TextView) findViewById(R.id.tvNumberOfRecords);
        findViewById(R.id.btSync).setOnClickListener(chooseRealmConfigurationListener);
        findViewById(R.id.btSyncAsync).setOnClickListener(chooseRealmConfigurationListener);
        findViewById(R.id.btAsync).setOnClickListener(chooseRealmConfigurationListener);
        findViewById(R.id.btClear).setOnClickListener(chooseRealmConfigurationListener);
    }

    private Operation getOperation() {
        switch (rgOperations.getCheckedRadioButtonId()) {
            case R.id.rbRead:
                return Operation.READ;
            case R.id.rbWrite:
                return Operation.WRITE;
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean isReadOperation(Operation pOperation) {
        switch (pOperation) {
            case READ:
                return true;
            case WRITE:
                return false;
            default:
                throw new IllegalArgumentException();
        }
    }

    private int getNumberOfRecords() {
        switch (rgNumberOfRecords.getCheckedRadioButtonId()) {
            case R.id.rb10:
                return 10;
            case R.id.rb100:
                return 1_00;
            case R.id.rb1000:
                return 1_000;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void syncRealm(Operation pOperation) {
        if (isReadOperation(pOperation)) {
            final long timestamp = System.currentTimeMillis();
            realmSync.readModels();
            toast("sync read: " + (System.currentTimeMillis() - timestamp));
        } else {
            Generator.generateDemoModels(new RealmSyncCallbacks(), getNumberOfRecords());
        }
    }

    private void syncAsyncRealm(Operation pOperation) {
        if (isReadOperation(pOperation)) {
            final long timestamp = System.currentTimeMillis();
            realmSyncAsync.readModels();
            toast("syncAsync read: " + (System.currentTimeMillis() - timestamp));
        } else {
            Generator.generateDemoModels(new RealmSyncAsyncCallbacks(), getNumberOfRecords());
        }
    }

    private void asyncRealm(final Operation pOperation) {
        if (isReadOperation(pOperation)) {
            realmAsync.readModels(new RealmAsyncCallbacks());
        } else {
            Generator.generateDemoModels(new RealmSyncAsyncCallbacks(), getNumberOfRecords());
        }
    }

    private void initRealm() {
        realmSync = new RealmSync(getApplicationContext());
        realmAsync = new RealmAsync(executor, getApplicationContext());
        realmSyncAsync = new RealmSyncAsync(executor, getApplicationContext());
    }

    private void clearRealm() {
        if (realmSync != null) {
            realmConfiguration = realmSync.getRealm().getConfiguration();
            realmSync.getRealm().close();
            realmSyncAsync.getReadableRealm().close();
            realmAsync.close(new RealmCloseCallback());
        }

    }

    private final class ChooseRealmConfigurationListener implements View.OnClickListener,
            RadioGroup.OnCheckedChangeListener {

        @Override
        public void onClick(View v) {
            final Operation operation = getOperation();
            switch (v.getId()) {
                case R.id.btSync:
                    syncRealm(operation);
                    break;
                case R.id.btSyncAsync:
                    syncAsyncRealm(operation);
                    break;
                case R.id.btAsync:
                    asyncRealm(operation);
                    break;
                case R.id.btClear:
                    clearRealm();
                    break;
                default:
                    throw new IllegalArgumentException();
            }
        }

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            if (checkedId == R.id.rbWrite) {
                rgNumberOfRecords.setVisibility(View.VISIBLE);
                tvNumberOfRecords.setVisibility(View.VISIBLE);
            } else {
                rgNumberOfRecords.setVisibility(View.GONE);
                tvNumberOfRecords.setVisibility(View.GONE);
            }
        }
    }

    private final class RealmSyncCallbacks implements Generator.OnGenerateCallback {

        @Override
        public void onGenerate(RealmList<Model> pRealmList) {
            final long timestamp = System.currentTimeMillis();
            realmSync.saveModels(pRealmList);
            toast("sync save: " + (System.currentTimeMillis() - timestamp) + " ms");
        }

    }

    private final class RealmSyncAsyncCallbacks implements Generator.OnGenerateCallback,
            RealmSyncAsync.OnBaseSaveCallback {

        private long timestamp;

        @Override
        public void onGenerate(RealmList<Model> pRealmList) {
            timestamp = System.currentTimeMillis();
            realmSyncAsync.saveModels(this, pRealmList);
        }

        @Override
        public void onSave() {
            toast("syncAsync Save: " + ((System.currentTimeMillis() - timestamp)) + " ms");
        }
    }

    private final class RealmAsyncCallbacks implements Generator.OnGenerateCallback,
            RealmAsync.OnBaseReadCallback<List<Model>>, RealmAsync.OnBaseSaveCallback {

        private long timestamp;

        private RealmAsyncCallbacks() {
            timestamp = System.currentTimeMillis();
        }

        @Override
        public void onRead(List<Model> object) {
            toast("async read: " + (System.currentTimeMillis() - timestamp) + " ms");
        }

        @Override
        public void onSave() {
            toast("async save: " + (System.currentTimeMillis() - timestamp) + " ms");
        }

        @Override
        public void onGenerate(RealmList<Model> pRealmList) {
            timestamp = System.currentTimeMillis();
            realmAsync.saveModels(this, pRealmList);
        }
    }

    private final class RealmCloseCallback implements RealmAsync.OnAsyncCloseCallback {

        @Override
        public void onClose() {
            try {
                realmSync = null;
                realmAsync = null;
                realmSyncAsync = null;
                System.gc();
                Realm.deleteRealm(realmConfiguration);
                toast("cleared");
                initRealm();
            } catch (Exception pException) {
                toast(pException.getMessage());
            }
        }
    }
}
