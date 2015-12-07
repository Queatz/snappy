package com.queatz.snappy;

import android.app.AlarmManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Team;

/**
 * Created by jacob on 12/5/15.
 */
public class AdvertiseService extends Service {
    Team team;
    AlarmManager alarmManager;

    @Override
    public void onCreate() {
        super.onCreate();

        team = ((MainApplication) getApplication()).team;

        // TODO check that it's running every 10 minutes or so
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            team.advertise.enable(null);
        } else {
            if (intent.hasExtra(Config.EXTRA_ACTION)) {
                String action = intent.getStringExtra(Config.EXTRA_ACTION);

                if (action != null) switch (action) {
                    case Intent.ACTION_USER_PRESENT:
                    case Intent.ACTION_SCREEN_ON:
                    case Intent.ACTION_BOOT_COMPLETED:
                        team.advertise.enable(null);
                        break;
                    case BluetoothAdapter.ACTION_STATE_CHANGED:
                        switch (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)) {
                            case BluetoothAdapter.STATE_ON:
                            case BluetoothAdapter.STATE_CONNECTED:
                                team.advertise.enable(null);
                                break;
                        }
                        break;
                    case Intent.ACTION_SCREEN_OFF:
                    case Intent.ACTION_BATTERY_LOW:
                        team.advertise.disableDiscover();
                        break;
                }
            }
        }

        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
