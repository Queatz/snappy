package com.queatz.snappy.team;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.makeramen.roundedimageview.RoundedTransformationBuilder;
import com.queatz.snappy.AdvertiseBroadcastReceiver;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Functions;
import com.queatz.snappy.util.Images;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 12/3/15.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Advertise {
    public Team team;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback mLeScanCallback;
    private AdvertiseCallback mAdvertiseCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallbackOld;

    private BluetoothGattServer mGattServer;
    private BluetoothGattService mService;
    private BluetoothGattCharacteristic mPersonIdCharacteristic;
    private BluetoothGattCharacteristic mPersonFirstNameCharacteristic;

    private Collection<BluetoothGatt> mBluetoothGatts = new HashSet<>();

    private Handler handler;

    class BlePerson {
        String deviceAddress;
        String personId;
        String personName;
        String imageUrl;
        Date lastSeen;
        Date lastHidden;

        public BlePerson(String deviceAddress, String personId, String personName, Date lastSeen) {
            this.deviceAddress = deviceAddress;
            this.personId = personId;
            this.personName = personName;
            this.lastSeen = lastSeen;
            this.lastHidden = null;
        }
    }

    enum ReadStep {
        DISCOVERING_SERVICES,
        READING_ID_CHARACTERISITC,
        READING_NAME_CHARACTERISTIC,
        COMPLETED
    }

    private Map<String, BlePerson> devices = new HashMap<>();

    public Advertise(Team team) {
        this.team = team;
    }

    public List<String> people() {
        List<String> personIds = new ArrayList<>();

        for (BlePerson blePerson : devices.values()) {
            if (isRecent(blePerson.lastSeen)) {
                personIds.add(blePerson.personId);
            }
        }

        return personIds;
    }

    public boolean capable() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                team.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Enable everything, both advertising and discovering.
     *
     * @param activity If provided and Bluetooth is not enabled, the user will be prompted to enable Bluetooth.
     * @return Whether or not advertising was enabled successfully
     */
    public boolean enable(@Nullable Activity activity) {
        handler = new Handler(team.context.getMainLooper());

        if (!capable()) {
            return false;
        }

        DynamicRealmObject me = team.auth.me();

        if (me == null) {
            return false;
        }

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) team.context.getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }


        if (mPersonIdCharacteristic == null) {
            mPersonIdCharacteristic = new BluetoothGattCharacteristic(
                    Config.UUID_CHARACTERISTIC_PROFILE_ID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            mPersonIdCharacteristic.setValue(me.getString(Thing.ID));
        }

        if (mPersonFirstNameCharacteristic == null && me.getString(Thing.FIRST_NAME) != null) {
            mPersonFirstNameCharacteristic = new BluetoothGattCharacteristic(
                    Config.UUID_CHARACTERISTIC_PROFILE_FIRST_NAME,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            mPersonFirstNameCharacteristic.setValue(me.getString(Thing.FIRST_NAME));
        }

        if (mService == null) {
            mService = new BluetoothGattService(Config.UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

            if (mPersonIdCharacteristic != null) {
                mService.addCharacteristic(mPersonIdCharacteristic);
            }

            if (mPersonFirstNameCharacteristic != null) {
                mService.addCharacteristic(mPersonFirstNameCharacteristic);
            }
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {

            if (activity == null || !Config.REQUIRE_BLUETOOTH) {
                return false;
            }

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, Config.REQUEST_CODE_ENABLE_BT);
        } else {
            mBluetoothAdapter.setName(Functions.getFullName(team.auth.me()));
            enable();
        }

        return true;
    }

    private void enable() {
        enableAdvertise();
        enableDiscover();
    }

    private void enableAdvertise() {
        if (!capable()) {
            return;
        }

        DynamicRealmObject me = team.auth.me();

        if (me == null) {
            return;
        }

        if (mGattServer == null) {
            mGattServer = mBluetoothManager.openGattServer(team.context, new BluetoothGattServerCallback() {
                @Override
                public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                }

                @Override
                public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic) {
                    mGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, characteristic.getValue());
                }
            });

            mGattServer.addService(mService);
        }

        if (mAdvertiseCallback == null) {
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                    .setConnectable(true)
                    .build();

            AdvertiseData advertiseData = new AdvertiseData.Builder()
                    .addServiceUuid(new ParcelUuid(Config.UUID_SERVICE))
                    .build();

            mAdvertiseCallback = new AdvertiseCallback() {
                @Override
                public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                    Log.w(Config.LOG_TAG, "advertise - advertise start success");
                }

                @Override
                public void onStartFailure(int errorCode) {
                    Log.w(Config.LOG_TAG, "advertise - advertise start failure: " + errorCode);
                }
            };

            BluetoothLeAdvertiser advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

            if (advertiser != null) {
                advertiser.startAdvertising(advertiseSettings, advertiseData, mAdvertiseCallback);
            } else {
                Log.w(Config.LOG_TAG, "advertise - advertiser null");
            }
        }
    }

    private void enableDiscover() {
        if (!capable()) {
            return;
        }

        if (mLeScanCallback == null) {
            mLeScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.w(Config.LOG_TAG, "advertise - scan success");
                    foundDevice(result.getDevice());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.w(Config.LOG_TAG, "advertise - scan failed " + errorCode);

                    if (errorCode == SCAN_FAILED_FEATURE_UNSUPPORTED) {
                        enableDiscoverOld();
                    }
                }
            };

            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(Config.UUID_SERVICE))
                    .build();

            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .setReportDelay(Config.REPORTING_DELAY)
                    .build();

            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(scanFilter);

            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (scanner != null) {
                scanner.startScan(scanFilters, scanSettings, mLeScanCallback);
            } else {
                Log.w(Config.LOG_TAG, "advertise - scanner null, trying deprecated method...");

                enableDiscoverOld();
            }
        }
    }

    private void enableDiscoverOld() {
        if (mLeScanCallbackOld == null) {
            mLeScanCallbackOld = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    foundDevice(device);
                }
            };

            boolean success = mBluetoothAdapter.startLeScan(
                    new UUID[] { Config.UUID_SERVICE },
                    mLeScanCallbackOld
            );

            if (success) {
                Log.w(Config.LOG_TAG, "advertise - scanner started using old method");
            } else {
                Log.w(Config.LOG_TAG, "advertise - scanner failed completely");
            }
        }
    }

    public void disable() {
        disableDiscover();
        disableAdvertise();
    }

    public void disableAdvertise() {
        if (mAdvertiseCallback != null) {
            BluetoothLeAdvertiser advertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();

            if (advertiser != null) {
                advertiser.stopAdvertising(mAdvertiseCallback);
                mAdvertiseCallback = null;
            }
        }

        if (mGattServer != null) {
            mGattServer.close();
            mGattServer = null;
        }
    }

    public void disableDiscover() {
        Iterator<BluetoothGatt> bluetoothGattIterator = mBluetoothGatts.iterator();
        while (bluetoothGattIterator.hasNext()) {
            BluetoothGatt gatt = bluetoothGattIterator.next();
            gatt.close();
            bluetoothGattIterator.remove();
        }

        if (mLeScanCallback != null) {
            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (scanner != null) {
                scanner.stopScan(mLeScanCallback);
                mLeScanCallback = null;
            }
        }

        if (mLeScanCallbackOld != null) {
            if (mBluetoothAdapter != null) {
                mBluetoothAdapter.stopLeScan(mLeScanCallbackOld);
                mLeScanCallbackOld = null;
            }
        }
    }

    public void hidePerson(@NonNull String personId) {
        if (devices.containsKey(personId)) {
            devices.get(personId).lastHidden = new Date();
        }
    }

    public void onActivityResult(final Activity activity, int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Config.REQUEST_CODE_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    enable();
                }
                break;
        }
    }

    private void foundDevice(final BluetoothDevice device) {
        Log.e(Config.LOG_TAG, "advertise - device found " + device);

        handler.post(new Runnable() {
            @Override
            public void run() {
                doFoundDevice(device);
            }
        });
    }

    private void doFoundDevice(final BluetoothDevice device) {
        for (BluetoothGatt gatt : mBluetoothGatts) {
            if (gatt.getDevice().getAddress().equals(device.getAddress())) {
                return;
            }
        }

        Log.e(Config.LOG_TAG, "advertise - dodevicefound " + device);

        BluetoothGattCallback callback = new BluetoothGattCallback() {
            String personName;
            String personId;

            BluetoothGatt gatt;
            BluetoothGattService service;
            ReadStep step;

            void close() {
                if (gatt == null) {
                    return;
                }

                mBluetoothGatts.remove(gatt);
                gatt.close();
                step = null;
            }

            void readNextCharacteristic() {
                if (step == null) {
                    return;
                }

                switch (step) {
                    case DISCOVERING_SERVICES:
                        if (gatt.readCharacteristic(service.getCharacteristic(Config.UUID_CHARACTERISTIC_PROFILE_ID))) {
                            step = ReadStep.READING_ID_CHARACTERISITC;
                        } else {
                            close();
                        }

                        break;
                    case READING_ID_CHARACTERISITC:
                        if (gatt.readCharacteristic(service.getCharacteristic(Config.UUID_CHARACTERISTIC_PROFILE_FIRST_NAME))) {
                            step = ReadStep.READING_NAME_CHARACTERISTIC;
                        } else {
                            close();
                        }

                        break;
                    case READING_NAME_CHARACTERISTIC:
                        if (personId != null && personName != null) {
                            foundPerson(personId, personName, gatt.getDevice().getAddress());
                        } else {
                            Log.e(Config.LOG_TAG, String.format("Person details not complete: name = %s id = %s ", personName, personId));
                        }

                        step = ReadStep.COMPLETED;

                        close();

                        break;
                    default:
                        close();
                }
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (step != null) {
                    return;
                }

                this.gatt = gatt;

                Log.e(Config.LOG_TAG, "advertise - onConnectionStateChange " + gatt.getDevice() + " - newState = " + newState);

                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED:
                        close();
                        break;
                    case BluetoothProfile.STATE_CONNECTED:
                        step = ReadStep.DISCOVERING_SERVICES;
                        gatt.discoverServices();
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                Log.w(Config.LOG_TAG, "advertise - onServicesDiscovered " + gatt.getDevice() + " - getService = " + gatt.getService(Config.UUID_SERVICE));

                BluetoothGattService service = gatt.getService(Config.UUID_SERVICE);

                if (service == null) {
                    close();
                    return;
                }

                this.service = service;
                readNextCharacteristic();
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.e(Config.LOG_TAG, "advertise - onCharacteristicRead " + gatt.getDevice() + " - characteristic = " + characteristic.getUuid());

                if (Config.UUID_CHARACTERISTIC_PROFILE_ID.equals(characteristic.getUuid())) {
                    personId = characteristic.getStringValue(0);
                } else if (Config.UUID_CHARACTERISTIC_PROFILE_FIRST_NAME.equals(characteristic.getUuid())) {
                    personName = characteristic.getStringValue(0);
                }

                readNextCharacteristic();
            }
        };

        BluetoothGatt gatt;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            gatt = device.connectGatt(team.context, false, callback, BluetoothDevice.TRANSPORT_LE);
        } else {
            gatt = device.connectGatt(team.context, false, callback);
        }

        mBluetoothGatts.add(gatt);
    }

    private boolean alreadyFoundPersonRecently(String personId) {
        return devices.containsKey(personId) && isRecent(devices.get(personId).lastSeen);
    }

    private boolean isRecent(@NonNull Date date) {
        return date.after(new Date(new Date().getTime() - Config.ADVERTISE_TIMEOUT));
    }

    private void foundPerson(String personId, String personName, String address) {
        Log.e(Config.LOG_TAG, "advertise - found person " + personId + " - " + personName);

        if (alreadyFoundPersonRecently(personId)) {
            return;
        }

        BlePerson blePerson;

        if (devices.containsKey(personId)) {
            blePerson = devices.get(personId);
            blePerson.lastSeen = new Date();
            blePerson.personId = personId;
            blePerson.personName = personName;
        } else {
            blePerson = new BlePerson(address, personId, personName, new Date());
        }

        devices.put(personId, blePerson);
        updateNotifications();
    }

    private void updateNotifications() {
        for (BlePerson blePerson : devices.values()) {
            if (isRecent(blePerson.lastSeen)) {
                if (blePerson.lastHidden == null || blePerson.lastHidden.before(new Date(new Date().getTime() - Config.ADVERTISE_HIDE_TIMEOUT))) {
                    showNotification(blePerson);
                }
            } else {
                hideNotification(blePerson);
            }
        }
    }

    private void hideNotification(BlePerson blePerson) {
        team.push.clear("person/" + blePerson.personId + "/advertise");
    }

    private void showNotification(final BlePerson blePerson) {
        NotificationCompat.Builder builder;
        Intent deleteIntent;
        final PendingIntent deletePendingIntent;

        Bundle deleteExtras = new Bundle();
        deleteExtras.putString("personId", blePerson.personId);

        deleteIntent = new Intent(team.context, AdvertiseBroadcastReceiver.class);
        deleteIntent.putExtras(deleteExtras);
        deletePendingIntent = PendingIntent.getBroadcast(team.context, 0, deleteIntent, 0);

        if (blePerson.imageUrl == null) {
            builder = new NotificationCompat.Builder(team.context)
                    .setDefaults(0)
                    .setAutoCancel(false)
                    .setSmallIcon(R.drawable.icon_system)
                    .setPriority(Notification.PRIORITY_LOW)
                    .setContentTitle(blePerson.personName)
                    .setVibrate(new long[] {0, 175, 175, 75})
                    .setContentText(team.context.getString(R.string.is_here))
                    .setDeleteIntent(deletePendingIntent);

            showNotification(builder, blePerson);

            loadImage(blePerson);
        } else {
            Images.with(team.context)
                    .load(Functions.getImageUrlForSize(blePerson.imageUrl, 64))
                    .transform(new RoundedTransformationBuilder().oval(true).build())
                    .into(new Target() {
                        @Override
                        public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(team.context)
                                            .setDefaults(0)
                                            .setAutoCancel(false)
                                            .setSmallIcon(R.drawable.icon_system)
                                            .setLargeIcon(bitmap)
                                            .setPriority(Notification.PRIORITY_LOW)
                                            .setContentTitle(blePerson.personName)
                                            .setContentText(team.context.getString(R.string.is_here))
                                            .setDeleteIntent(deletePendingIntent);

                                    showNotification(builder, blePerson);
                                }
                            });
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
        }
    }

    private void showNotification(NotificationCompat.Builder builder, BlePerson blePerson) {
        Intent resultIntent = new Intent(team.context, Person.class);
        Bundle extras = new Bundle();
        extras.putString("person", blePerson.personId);
        resultIntent.putExtras(extras);
        builder.setContentIntent(team.push.newIntentWithStack(resultIntent));

        if(Build.VERSION.SDK_INT >= 21) {
            builder
                    .setColor(team.context.getResources().getColor(R.color.red))
                    .setCategory(Notification.CATEGORY_SOCIAL);
        }

        team.push.show("person/" + blePerson.personId + "/advertise", builder.build());
    }

    private void loadImage(final BlePerson blePerson) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                team.api.get(Config.PATH_EARTH + "/" + blePerson.personId, new Api.Callback() {
                    @Override
                    public void success(String response) {
                        DynamicRealmObject person = team.things.put(response);

                        blePerson.imageUrl = person.getString(Thing.IMAGE_URL);
                        showNotification(blePerson);
                    }

                    @Override
                    public void fail(String response) {

                    }
                });
            }
        });
    }
}
