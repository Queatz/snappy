package com.queatz.snappy.team;

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
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.queatz.snappy.AdvertiseBroadcastReceiver;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.Person;
import com.queatz.snappy.shared.Config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by jacob on 12/3/15.
 */
public class Advertise {
    public Team team;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private ScanCallback mLeScanCallback;
    private AdvertiseCallback mAdvertiseCallback;

    private BluetoothGattServer mGattServer;
    private Collection<BluetoothGatt> mBluetoothGatts = new HashSet<>();

    class BlePerson {
        String deviceAddress;
        String personId;
        String personName;
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
        // Note: can go down to 18 eventually...
        return Build.VERSION.SDK_INT >= 21 && team.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * Enable everything, both advertising and discovering.
     *
     * @param activity If provided and Bluetooth is not enabled, the user will be prompted to enable Bluetooth.
     * @return Whether or not advertising was enabled successfully
     */
    public boolean enable(@Nullable Activity activity) {
        if (!capable()) {
            return false;
        }

        if (team.auth.me() == null) {
            return false;
        }

        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) team.context.getSystemService(Context.BLUETOOTH_SERVICE);
        }

        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
        }

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.setName(team.auth.me().getName());

            if (activity == null) {
                return false;
            }

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            activity.startActivityForResult(enableBtIntent, Config.REQUEST_CODE_ENABLE_BT);
        } else {
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

        com.queatz.snappy.things.Person me = team.auth.me();

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

            BluetoothGattService service = new BluetoothGattService(Config.UUID_SERVICE, BluetoothGattService.SERVICE_TYPE_PRIMARY);

            BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(
                    Config.UUID_CHARACTERISTIC_PROFILE_ID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            characteristic.setValue(me.getId());

            BluetoothGattCharacteristic characteristic2 = new BluetoothGattCharacteristic(
                    Config.UUID_CHARACTERISTIC_PROFILE_ID,
                    BluetoothGattCharacteristic.PROPERTY_READ,
                    BluetoothGattCharacteristic.PERMISSION_READ);
            characteristic2.setValue(me.getFirstName());

            service.addCharacteristic(characteristic2);
            mGattServer.addService(service);
        }

        if (mAdvertiseCallback == null) {
            AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                    .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
                    .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                    .build();

            AdvertiseData advertiseData = new AdvertiseData.Builder().build();

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
                    foundDevice(result.getDevice());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.w(Config.LOG_TAG, "advertise - scan failed " + errorCode);
                }
            };

            ScanFilter scanFilter = new ScanFilter.Builder()
                    .setServiceUuid(new ParcelUuid(Config.UUID_SERVICE))
                    .build();

            ScanSettings scanSettings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                    .setReportDelay(Config.REPORTING_DELAY)
                    .build();

            List<ScanFilter> scanFilters = new ArrayList<>();
            scanFilters.add(scanFilter);

            BluetoothLeScanner scanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (scanner != null) {
                scanner.startScan(scanFilters, scanSettings, mLeScanCallback);
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
                mBluetoothAdapter = null;
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
    }

    public void hidePerson(@NonNull String deviceAddress) {
        if (devices.containsKey(deviceAddress)) {
            devices.get(deviceAddress).lastHidden = new Date();
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

    private void foundDevice(BluetoothDevice device) {
        if (alreadyFoundPersonRecently(device)) {
            return;
        }

        BluetoothGatt gatt = device.connectGatt(team.context, false, new BluetoothGattCallback() {
            String personName;
            String personId;

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        gatt.discoverServices();
                        break;
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                gatt.readCharacteristic(gatt.getService(Config.UUID_SERVICE).getCharacteristic(Config.UUID_CHARACTERISTIC_PROFILE_ID));
                gatt.readCharacteristic(gatt.getService(Config.UUID_SERVICE).getCharacteristic(Config.UUID_CHARACTERISTIC_PROFILE_FIRST_NAME));
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                if (Config.UUID_CHARACTERISTIC_PROFILE_ID.equals(characteristic.getUuid())) {
                    personId = characteristic.getStringValue(0);
                } else if (Config.UUID_CHARACTERISTIC_PROFILE_FIRST_NAME.equals(characteristic.getUuid())) {
                    personName = characteristic.getStringValue(0);
                }

                if (personId != null && personName != null) {
                    foundPerson(personId, personName, gatt.getDevice().getAddress());
                    gatt.disconnect();
                    gatt.close();
                    mBluetoothGatts.remove(gatt);
                }
            }
        });

        mBluetoothGatts.add(gatt);
    }

    private boolean alreadyFoundPersonRecently(BluetoothDevice device) {
        return devices.containsKey(device.getAddress()) && isRecent(devices.get(device.getAddress()).lastSeen);
    }

    private boolean isRecent(@NonNull Date date) {
        return date.after(new Date(new Date().getTime() - Config.ADVERTISE_TIMEOUT));
    }

    private void foundPerson(String personId, String personName, String address) {
        BlePerson blePerson;

        if (devices.containsKey(address)) {
            blePerson = devices.get(address);
            blePerson.lastSeen = new Date();
            blePerson.personId = personId;
            blePerson.personName = personName;
        } else {
            blePerson = new BlePerson(address, personId, personName, new Date());
        }

        devices.put(address, blePerson);
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

    private void showNotification(BlePerson blePerson) {
        NotificationCompat.Builder builder;
        Intent resultIntent;
        Intent deleteIntent;
        PendingIntent deletePendingIntent;

        deleteIntent = new Intent(team.context, AdvertiseBroadcastReceiver.class);
        deleteIntent.getExtras().putString("deviceAddress", blePerson.deviceAddress);
        deletePendingIntent = PendingIntent.getBroadcast(team.context, 0, deleteIntent, 0);

        builder = new NotificationCompat.Builder(team.context)
                .setDefaults(0)
                .setAutoCancel(false)
                .setSmallIcon(R.drawable.icon_system)
                .setPriority(Notification.PRIORITY_LOW)
                .setContentTitle(blePerson.personName)
                .setContentText(team.context.getString(R.string.is_here))
                .setDeleteIntent(deletePendingIntent)
                        .setDefaults(0);

        resultIntent = new Intent(team.context, Person.class);
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
}
