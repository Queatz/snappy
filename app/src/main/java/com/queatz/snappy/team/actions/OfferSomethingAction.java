package com.queatz.snappy.team.actions;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.queatz.snappy.R;
import com.queatz.snappy.shared.Config;
import com.queatz.snappy.ui.EditText;
import com.queatz.snappy.ui.TimeSlider;

/**
 * Created by jacob on 5/18/17.
 */

public class OfferSomethingAction extends ActivityAction {
    @Override
    protected void execute() {
        final View newOffer = View.inflate(me().getActivity(), R.layout.new_offer, null);

        final EditText experienceDetails = (EditText) newOffer.findViewById(R.id.details);
        final TimeSlider priceSlider = (TimeSlider) newOffer.findViewById(R.id.price);
        final EditText perUnit = (EditText) newOffer.findViewById(R.id.perWhat);
        final View highlight = newOffer.findViewById(R.id.highlight);

        priceSlider.setPercent(getFreePercent());
        priceSlider.setTextCallback(new TimeSlider.TextCallback() {
            @Override
            public String getText(float percent) {
                Integer price = getPrice(percent);

                if (price == null) {
                    return getTeam().context.getString(R.string.ask);
                }

                if (price < 0) {
                    highlight.setBackgroundResource(R.color.purple);
                    priceSlider.setTextColor(R.color.purple);
                    experienceDetails.setHint(me().getActivity().getResources().getString(R.string.what_do_you_want));
                } else {
                    highlight.setBackgroundResource(R.color.green);
                    priceSlider.setTextColor(R.color.green);
                    experienceDetails.setHint(me().getActivity().getResources().getString(R.string.what_do_you_offer));
                }

                if (price == 0) {
                    return getTeam().context.getString(R.string.no_bounty);
                }

                return  me().getActivity().getString(R.string.for_amount, "$" + Integer.toString(Math.abs(price)));
            }
        });

        final AlertDialog dialog = new AlertDialog.Builder(me().getActivity())
                .setView(newOffer)
                .setNegativeButton(R.string.nope, null)
                .setPositiveButton(R.string.add, null)
                .setCancelable(true)
                .show();

        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (experienceDetails.getText().toString().isEmpty()) {
                    Toast.makeText(getTeam().context, "Enter description", Toast.LENGTH_SHORT).show();
                    return;
                }

                to(new AddOfferAction(
                        experienceDetails.getText().toString(),
                        null,
                        getPrice(priceSlider.getPercent()),
                        perUnit.getText().toString()
                ));

                dialog.dismiss();
            }
        });

        experienceDetails.post(new Runnable() {
            @Override
            public void run() {
                getTeam().view.keyboard(experienceDetails);
            }
        });
    }

    private float getFreePercent() {
        if (Config.HOSTING_ENABLED_TRUE.equals(me().getTeam().buy.hostingEnabled())) {
            return -Config.PAID_OFFER_PRICE_MIN / (float) (-Config.PAID_OFFER_PRICE_MIN + Config.PAID_OFFER_PRICE_MAX) * 0.9f;
        } else {
            return -Config.FREE_OFFER_PRICE_MIN / (float) (-Config.FREE_OFFER_PRICE_MIN + Config.FREE_OFFER_PRICE_MAX) * 0.9f;
        }
    }

    private Integer getPrice(float percent) {
        if (percent > 0.9f) {
            return null;
        } else {
            percent /= 0.9f;
        }

        Integer price;


        if (Config.HOSTING_ENABLED_TRUE.equals(me().getTeam().buy.hostingEnabled())) {
            price = (int) (percent * (Config.PAID_OFFER_PRICE_MAX - Config.PAID_OFFER_PRICE_MIN) + Config.PAID_OFFER_PRICE_MIN);
        } else {
            price = (int) (percent * (Config.FREE_OFFER_PRICE_MAX - Config.FREE_OFFER_PRICE_MIN) + Config.FREE_OFFER_PRICE_MIN);
        }

        if (Math.abs(price) < 200) {
            price = (int) Math.floor(price / 10) * 10;
        } else if (Math.abs(price) < 1000) {
            price = (int) Math.floor(price / 50) * 50;
        } else {
            price = (int) Math.floor(price / 100) * 100;
        }

        return price;
    }
}
