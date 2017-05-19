package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.queatz.snappy.R;

/**
 * Created by jacob on 5/18/17.
 */

public class SignoutAction extends ActivityAction {
    @Override
    public void execute() {
        new AlertDialog.Builder(me().getActivity())
                .setMessage(R.string.signout_of)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.signout, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        getTeam().auth.signout(me().getActivity());
                    }
                })
                .show();
    }
}
