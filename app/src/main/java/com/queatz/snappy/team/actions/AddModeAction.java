package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.ui.EditText;

/**
 * Created by jacob on 10/7/17.
 */

public class AddModeAction extends AuthenticatedAction {
    @Override
    public void whenAuthenticated() {
        final View newMode = View.inflate(me().getActivity(), R.layout.new_mode, null);
        final EditText name = newMode.findViewById(R.id.name);
        final EditText about = newMode.findViewById(R.id.about);

        final AlertDialog dialog = new AlertDialog.Builder(me().getActivity())
                .setView(newMode)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.add_mode, null)
                .setCancelable(true)
                .show();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
                .setTextColor(getTeam().context.getResources().getColor(R.color.gray));

        dialog.getButton(DialogInterface.BUTTON_POSITIVE)
                .setTextColor(getTeam().context.getResources().getColor(R.color.thing_mode));

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (about.getText().toString().isEmpty()) {
                    Toast.makeText(getTeam().context, R.string.enter_name, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (about.getText().toString().isEmpty()) {
                    Toast.makeText(getTeam().context, R.string.enter_details, Toast.LENGTH_SHORT).show();
                    return;
                }

                to(new SaveModeAction(name.getText().toString(), about.getText().toString()));

                dialog.dismiss();
            }
        });

        name.post(new Runnable() {
            @Override
            public void run() {
                getTeam().view.keyboard(name);
            }
        });
    }
}
