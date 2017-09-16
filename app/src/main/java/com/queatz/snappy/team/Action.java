package com.queatz.snappy.team;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.util.Images;

import java.io.FileNotFoundException;

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 11/23/14.
 */

@Deprecated
public class Action {
    public Team team;

    public Action(Team t) {
        team = t;
    }

    DynamicRealmObject nPendingOfferPhotoChange;

    public void addPhotoToOffer(@NonNull Activity activity, @NonNull DynamicRealmObject offer) {
        nPendingOfferPhotoChange = offer;

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(intent, Config.REQUEST_CODE_CHOOSER);
    }

    public void removePhotoFromOffer(@NonNull DynamicRealmObject offer) {
        team.realm.beginTransaction();
        offer.setBoolean(Thing.PHOTO, false);
        team.realm.commitTransaction();

        team.api.post(Config.PATH_EARTH + "/" + offer.getString(Thing.ID) + "/" + Config.PATH_PHOTO + "/" + Config.PATH_DELETE);
    }

    public DynamicRealmObject nPendingLocationPhotoChange;

    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case Config.REQUEST_CODE_CHOOSER:
                if(resultCode == Activity.RESULT_OK) {
                    final Uri photo = intent.getData();

                    if (photo == null) {
                        return;
                    }

                    if(nPendingLocationPhotoChange != null) {
                        uploadPhoto(String.format(Config.PATH_EARTH_PHOTO, nPendingLocationPhotoChange.getString(Thing.ID)), photo);
                    } else if(nPendingOfferPhotoChange != null) {
                        uploadPhoto(String.format(Config.PATH_EARTH_PHOTO, nPendingOfferPhotoChange.getString(Thing.ID)), photo);
                    }
                }

                break;
        }
    }

    private void uploadPhoto(String path, Uri photo) {
        RequestParams params = new RequestParams();

        try {
            params.put(Config.PARAM_PHOTO, team.context.getContentResolver().openInputStream(photo));
        }
        catch (FileNotFoundException e) {
            Toast.makeText(team.context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        Toast.makeText(team.context, R.string.changing_photo, Toast.LENGTH_SHORT).show();

        final String id;

        if (nPendingLocationPhotoChange != null) {
            id = nPendingLocationPhotoChange.getString(Thing.ID);
        } else if (nPendingOfferPhotoChange != null) {
            id = nPendingOfferPhotoChange.getString(Thing.ID);
        } else {
            id = null;
        }

        team.api.post(path, params, new Api.Callback() {
            @Override
            public void success(String response) {
                if (id != null) {
                    String photoUrl = Config.API_URL + String.format(Config.PATH_EARTH_PHOTO + "?s=64&auth=" + team.auth.getAuthParam(), id);
                    Images.with(team.context).invalidate(photoUrl);
                }
            }

            @Override
            public void fail(String response) {
                Toast.makeText(team.context, R.string.couldnt_set_photo, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
