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
import com.queatz.snappy.ui.EditText;

/**
 * Created by jacob on 5/18/17.
 */

public class SendFeedbackAction extends ActivityAction {
    @Override
    protected void execute() {
        final EditText editText = new EditText(me().getActivity());
        int p = (int) Util.px(16);
        editText.setPadding(p, p, p, p);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_ENTER_ACTION);
        editText.setHint(R.string.write_feedback_here);
        editText.setSingleLine(false);

        new AlertDialog.Builder(me().getActivity()).setView(editText)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.send_feedback, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String feedback = editText.getText().toString();

                        RequestParams params = new RequestParams();
                        params.put(Config.PARAM_FEEDBACK, feedback);

                        getTeam().api.post(Config.PATH_EARTH + "/feedback", params, new Api.Callback() {
                            @Override
                            public void success(String response) {
                                Toast.makeText(me().getActivity(), R.string.thanks, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void fail(String response) {
                                Toast.makeText(me().getActivity(), "Failed to send feedback. Ironic...", Toast.LENGTH_SHORT).show();
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
