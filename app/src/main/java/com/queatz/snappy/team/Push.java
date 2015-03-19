package com.queatz.snappy.team;

import android.widget.Toast;

import com.queatz.snappy.Config;
import com.queatz.snappy.Util;
import com.queatz.snappy.things.Contact;
import com.queatz.snappy.things.Join;
import com.queatz.snappy.things.Message;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Date;

/**
 * Created by jacob on 3/19/15.
 */
public class Push {
    public Team team;

    public Push(Team t) {
        team = t;
    }

    public void got(JSONObject push) {
        try {
            String personFirstName;
            String partyName;

            switch (push.getString("action")) {
                case Config.PUSH_ACTION_MESSAGE:
                    personFirstName = URLDecoder.decode(push.getJSONObject("message").getJSONObject("from").getString("firstName"), "UTF-8");
                    String message = URLDecoder.decode(push.getJSONObject("message").getString("message"), "UTF-8");

                    Toast.makeText(team.context, personFirstName + ": " + message, Toast.LENGTH_LONG).show();

                    team.api.get(String.format(Config.PATH_MESSAGES_ID, push.getJSONObject("message").getString("id")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            try {
                                JSONObject o = new JSONObject(response);

                                Message m = team.things.put(Message.class, o);

                                if(m != null)
                                    team.local.updateContactsForMessage(m);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
                case Config.PUSH_ACTION_JOIN_REQUEST:
                    personFirstName = URLDecoder.decode(push.getJSONObject("person").getString("firstName"), "UTF-8");
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");

                    Toast.makeText(team.context, personFirstName + " requested to join " + partyName, Toast.LENGTH_LONG).show();

                    team.api.get(String.format(Config.PATH_JOIN_ID, push.getString("join")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            try {
                                JSONObject o = new JSONObject(response);

                                team.things.put(Join.class, o);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
                case Config.PUSH_ACTION_JOIN_ACCEPTED:
                    partyName = URLDecoder.decode(push.getJSONObject("party").getString("name"), "UTF-8");
                    Date partyDate = Util.stringToDate(push.getJSONObject("party").getString("date"));

                    Toast.makeText(team.context, "You're in! " + partyName + " starts " + Util.relDate(partyDate) + ".", Toast.LENGTH_LONG).show();

                    team.api.get(String.format(Config.PATH_JOIN_ID, push.getString("join")), new Api.Callback() {
                        @Override
                        public void success(String response) {
                            try {
                                JSONObject o = new JSONObject(response);

                                team.things.put(Join.class, o);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void fail(String response) {

                        }
                    });

                    break;
                case Config.PUSH_ACTION_FOLLOW:
                    personFirstName = URLDecoder.decode(push.getJSONObject("person").getString("firstName"), "UTF-8");

                    Toast.makeText(team.context, personFirstName + " started following you", Toast.LENGTH_LONG).show();


                    break;
            }
        }
        catch (UnsupportedEncodingException | JSONException e) {
            e.printStackTrace();
        }
    }
}
