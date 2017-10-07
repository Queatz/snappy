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

import io.realm.DynamicRealmObject;

/**
 * Created by jacob on 5/18/17.
 */

public class ReportThingAction extends ActivityAction {
    private final DynamicRealmObject thing;

    public ReportThingAction(DynamicRealmObject thing) {
        this.thing = thing;
    }

    @Override
    protected void execute() {
        final EditText editText = new EditText(me().getActivity());
        int p = (int) Util.px(16);
        editText.setPadding(p, p, p, p);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setHint(R.string.what_went_wrong);
        editText.setSingleLine(false);

        new AlertDialog.Builder(me().getActivity()).setView(editText)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String report = editText.getText().toString();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_MESSAGE, report);

                        getTeam().api.post(Config.PATH_EARTH + "/" + Config.PATH_ME + "/report/" + thing.getString(Thing.ID), params, new Api.Callback() {
                            @Override
                            public void success(String response) {
                                Toast.makeText(me().getActivity(), R.string.thanks_for_report, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void fail(String response) {
                                Toast.makeText(me().getActivity(), "Failed to report this person", Toast.LENGTH_SHORT).show();
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
