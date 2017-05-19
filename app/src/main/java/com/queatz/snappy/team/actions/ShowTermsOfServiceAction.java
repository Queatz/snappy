package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.view.View;

import com.queatz.snappy.R;

/**
 * Created by jacob on 5/18/17.
 */

public class ShowTermsOfServiceAction extends ShowOkDialogAction {
    @Override
    protected int getLayout() {
        return R.layout.terms_of_service;
    }
}
