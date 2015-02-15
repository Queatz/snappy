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
        }

        try {
            JSONObject party = getItem(position);
            ((TextView) view.findViewById(R.id.name)).setText(party.getString("name"));
            ((TextView) view.findViewById(R.id.location_text)).setText(party.getString("location"));
            ((TextView) view.findViewById(R.id.time_text)).setText(party.getString("time"));

            String details = party.getString("details");

            if(details.isEmpty())
                view.findViewById(R.id.details).setVisibility(View.GONE);
            else {
                view.findViewById(R.id.details).setVisibility(View.VISIBLE);
                ((TextView) view.findViewById(R.id.details)).setText(details);
            }
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

        incount = Math.abs(new Random().nextInt() % 5);

        ((ImageView) view.findViewById(R.id.backdrop)).setImageResource(new int[] {
                R.drawable.backdrop_location,
                R.drawable.backdrop_location_2,
                R.drawable.backdrop_location_3,
                R.drawable.backdrop_location_4,
                R.drawable.backdrop_location_5
        }[incount]);

        if(position < 2) {
            ((TextView) view.findViewById(R.id.action_join)).setText(getContext().getText(R.string.mark_party_full));
        }
        else {
            ((TextView) view.findViewById(R.id.action_join)).setText(getContext().getText(R.string.request_to_join));
        }

        view.findViewById(R.id.action_requested).setVisibility(position < 1 ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.updates).setVisibility(position < 2 ? View.VISIBLE : View.GONE);

        return view;
    }
}
