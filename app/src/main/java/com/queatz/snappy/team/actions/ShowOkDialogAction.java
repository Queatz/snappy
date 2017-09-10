package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.view.View;

import com.queatz.snappy.R;

/**
 * Created by jacob on 5/18/17.
 */

public abstract class ShowOkDialogAction extends ActivityAction {
    @Override
    protected void execute() {
        View view = View.inflate(me().getActivity(), getLayout(), null);

        new AlertDialog.Builder(me().getActivity())
                .setView(view)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    protected abstract int getLayout();
}
