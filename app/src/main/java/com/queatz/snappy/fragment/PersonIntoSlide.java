package com.queatz.snappy.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.queatz.snappy.MainActivity;
import com.queatz.snappy.R;
import com.queatz.snappy.team.Team;
import com.queatz.snappy.ui.TextView;

/**
 * Created by jacob on 10/23/14.
 */
public class PersonIntoSlide extends Fragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.person_into, container, false);
        final Team team = ((MainActivity) getActivity()).team;

        ViewGroup interests = (ViewGroup) view.findViewById(R.id.interests);

        for(int i = 0; i < interests.getChildCount(); i++) {
            TextView interest = (TextView) interests.getChildAt(i);

            final String s = interest.getText().toString();
            interest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    team.view.search(s);
                }
            });

            interest.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    delete(s);

                    return true;
                }
            });
        }

        return view;
    }

    private void delete(String into) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(into)
                .setPositiveButton("Remove", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .setNegativeButton("Keep", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                })
                .show();
    }
}
