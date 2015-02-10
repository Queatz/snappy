package com.queatz.snappy.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.queatz.snappy.MainApplication;
import com.queatz.snappy.R;
import com.queatz.snappy.activity.ViewActivity;
import com.queatz.snappy.team.Team;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * Created by jacob on 2/8/15.
 */
public class PartyAdapter extends ArrayAdapter<JSONObject> {
    public PartyAdapter(Context context, List<JSONObject> values) {
        super(context, R.layout.party_card, values);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        }
        else {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.party_card, parent, false);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Team team = ((MainApplication) getContext().getApplicationContext()).team;

                    team.view.push(ViewActivity.Transition.EXAMINE, ViewActivity.Transition.INSTANT, team.view.mUptoView);
                }
            });
        }

        try {
            ((TextView) view.findViewById(R.id.name)).setText(getItem(position).getString("name"));
            ((TextView) view.findViewById(R.id.location_text)).setText(getItem(position).getString("location"));
            ((TextView) view.findViewById(R.id.time_text)).setText(getItem(position).getString("time"));
            ((TextView) view.findViewById(R.id.details)).setText(getItem(position).getString("details"));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        int incount = new Random().nextInt() % 8;

        if(incount < 1)
            view.findViewById(R.id.whos_in_layout).setVisibility(View.GONE);
        else {
            view.findViewById(R.id.whos_in_layout).setVisibility(View.VISIBLE);

            LinearLayout whosin = ((LinearLayout) view.findViewById(R.id.whos_in));

            ImageView awho = (ImageView) whosin.getChildAt(0);
            ViewGroup.LayoutParams lps = awho.getLayoutParams();

            whosin.removeAllViews();

            for (int i = 0; i < incount; i++) {
                awho = new ImageView(getContext());
                awho.setLayoutParams(lps);
                awho.setImageResource(R.drawable.profile);
                whosin.addView(awho);
            }
        }

        return view;
    }
}
