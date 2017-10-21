package com.queatz.snappy.team.actions;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 10/7/17.
 */

class ChangePhotoAction extends AuthenticatedAction {

    private final DynamicRealmObject thing;

    public ChangePhotoAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    public void whenAuthenticated() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        me().getActivity().startActivityForResult(intent, Config.REQUEST_CODE_SEND_CHAT_PHOTO);
        getTeam().callbacks.set(Config.REQUEST_CODE_SEND_CHAT_PHOTO, new PreferenceManager.OnActivityResultListener() {
            @Override
            public boolean onActivityResult(int requestCode, int resultCode, Intent intent) {
                emit(resultCode == Activity.RESULT_OK);
                if(resultCode == Activity.RESULT_OK) {
                    final Uri photo = intent.getData();

                    if (photo == null) {
                        return false;
                    }

                    try {
                        InputStream inputStream = getTeam().context.getContentResolver().openInputStream(photo);

                        if (inputStream == null) {
                            return false;
                        }

                        byte[] bytes = new byte[inputStream.available()];
                        inputStream.read(bytes, 0, inputStream.available());

                        inputStream.close();

                        uploadPhoto(String.format(Config.PATH_EARTH_PHOTO, thing.getString(Thing.ID)), photo);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return false;
                    }
                }

                return false;
            }
        });
    }

    private void uploadPhoto(String path, Uri photo) {
        RequestParams params = new RequestParams();

        try {
            params.put(Config.PARAM_PHOTO, getTeam().context.getContentResolver().openInputStream(photo));
        }
        catch (FileNotFoundException e) {
            Toast.makeText(getTeam().context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Toast.makeText(getTeam().context, R.string.changing_photo, Toast.LENGTH_SHORT).show();

        final String id = thing.getString(Thing.ID);

        getTeam().api.post(path, params, new Api.Callback() {
            @Override
            public void success(String response) {
            }

            @Override
            public void fail(String response) {
                Toast.makeText(getTeam().context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
