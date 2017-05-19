package com.queatz.snappy.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.team.TeamActivity;
import com.queatz.snappy.ui.EditText;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by jacob on 9/12/15.
 */
public class NewUpto extends TeamActivity {
    Team team;
    Uri mPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        team = ((MainApplication) getApplication()).team;

        setContentView(R.layout.upto_new);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if(intent == null) {
            finish();
            return;
        }

        switch (intent.getAction()) {
            case Intent.ACTION_SEND:
                mPhoto = intent.getData();

                if(mPhoto == null) {
                    mPhoto = (Uri) intent.getExtras().get(Intent.EXTRA_STREAM);
                }

                setupView();
                break;
            default:
                finish();
                break;
        }
    }

    private void setupView() {
        ImageView photo = (ImageView) findViewById(R.id.photo);

        final Matrix matrix = Util.transformationFromExif(mPhoto);

        Picasso.with(this).load(mPhoto).transform(new Transformation() {
            @Override
            public Bitmap transform(Bitmap source) {
                if(matrix == null) {
                    return source;
                }

                Bitmap bitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                source.recycle();

                return bitmap;
            }

            @Override
            public String key() {
                return matrix == null ? "" : matrix.toString();
            }
        }).into(photo);

        findViewById(R.id.post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
                finish();
            }
        });
    }

    private void save() {
        team.action.postSelfUpdate(mPhoto, ((EditText) findViewById(R.id.message)).getText().toString());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        team.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
