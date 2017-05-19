package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.queatz.snappy.R;
import com.queatz.snappy.Util;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.team.Api;
import com.queatz.snappy.team.Thing;
import com.queatz.snappy.ui.EditText;

/**
 * Created by jacob on 5/18/17.
 */

public class ChangeAboutAction extends AuthenticatedAction {
    @Override
    public void whenAuthenticated() {
        final EditText editText = new EditText(me().getActivity());
        int p = (int) Util.px(16);
        editText.setPadding(p, p, p, p);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setSingleLine(false);
        editText.setHint(R.string.what_are_you_into);

        String about = getTeam().auth.me().getString(Thing.ABOUT);

        if(about == null || about.isEmpty()) {
            about = "";
        }

        editText.setText(about);

        new AlertDialog.Builder(me().getActivity()).setView(editText)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String about = editText.getText().toString();

                        getTeam().realm.beginTransaction();
                        getTeam().auth.me().setString(Thing.ABOUT, about);
                        getTeam().realm.commitTransaction();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_ABOUT, about);

                        getTeam().api.post(Config.PATH_EARTH + "/" + Config.PATH_ME, params, new Api.Callback() {
                            @Override
                            public void success(String response) {

                            }

                            @Override
                            public void fail(String response) {
                                Toast.makeText(getTeam().context, "Couldn't change what you're into", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .show();

        editText.post(new Runnable() {
            @Override
            public void run() {
                getTeam().view.keyboard(editText);
            }
        });
    }
}
