package com.mrainwater.android.bgghelper;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.android.volley.*;
import com.android.volley.toolbox.Volley;
import com.mrainwater.android.bgghelper.models.BoardGame;
import com.mrainwater.android.bgghelper.models.BoardGameDetail;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UsernameActivity extends AppCompatActivity {

    private EditText usernameInput;
    private Button submitButton;
    private RequestQueue volleyQueue;

    private XmlPullParser xmlParser;

    private ArrayList<BoardGame> boardGames;
    private ArrayList<BoardGameDetail> boardGameDetails;

    private int countOfGames = 0;
    ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        progress = new ProgressDialog(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_username);

        usernameInput = (EditText) findViewById(R.id.username_edittext);
        submitButton = (Button) findViewById(R.id.username_submit);

        volleyQueue = Volley.newRequestQueue(this);

        xmlParser = Xml.newPullParser();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setTitle("Loading");
                progress.setMessage("Getting your collection...");
                progress.setCancelable(false);
                progress.show();
                BGGAsyncTaskRunner runner = new BGGAsyncTaskRunner(volleyQueue);
                String username = usernameInput.getText().toString();
                runner.execute(getCollectionRequest(username));
            }
        });
    }

    private NetworkResponseRequest  getCollectionRequest(String username){
        String url ="http://www.boardgamegeek.com/xmlapi2/collection?excludesubtype=boardgameexpansion&username=" + username;

        return new NetworkResponseRequest (Request.Method.GET, url,
                new Response.Listener<NetworkResponse>() {
                    public void onResponse(NetworkResponse response) {
                        if(response.statusCode == 202) {
                            String username = usernameInput.getText().toString();
                            volleyQueue.add(getCollectionRequest(username));
                        }
                        else {
                            try {
                                InputStream is = new ByteArrayInputStream(response.data);
                                xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                                xmlParser.setInput(is, null);
                                parseCollectionXML(xmlParser);
                                progress.setMessage("Getting your games... ");
                                String ids = "";
                                for(int i = 0; i < boardGames.size(); i++)
                                {
                                    BoardGame next = boardGames.get(i);
                                    ids += next.objectid + ",";
                                }
                                volleyQueue.add(getGameRequest(ids));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );
    }

    private void parseCollectionXML(XmlPullParser parser) throws XmlPullParserException,IOException {
        ArrayList<BoardGame> boardGameList = null;
        int eventType = parser.getEventType();
        BoardGame currentGame = null;

        while (eventType != XmlPullParser.END_DOCUMENT) {
            String name = null;
            switch (eventType) {
                case XmlPullParser.START_DOCUMENT:
                    boardGameList = new ArrayList();
                    break;
                case XmlPullParser.START_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("item")) {
                        currentGame = new BoardGame();
                        currentGame.objectid = Integer.parseInt(parser.getAttributeValue("", "objectid"));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if (name.equalsIgnoreCase("item") && currentGame != null) {
                        boardGameList.add(currentGame);
                    }
            }
            eventType = parser.next();
        }

        boardGames = boardGameList;
    }

    private NetworkResponseRequest getGameRequest(final String id) {
        String url ="http://www.boardgamegeek.com/xmlapi2/thing?id=" + id.substring(0,id.length()-1);;

        return new NetworkResponseRequest (Request.Method.GET, url,
                new Response.Listener<NetworkResponse>() {
                    public void onResponse(NetworkResponse response) {
                        try {
                            InputStream is = new ByteArrayInputStream(response.data);
                            xmlParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                            xmlParser.setInput(is, null);
                            parseDetailXML(xmlParser, id);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                volleyQueue.add(getGameRequest(id));
                            }
                        }, 10000);
                    }
                }
        );
    }

    private void parseDetailXML(XmlPullParser parser, String id) throws XmlPullParserException,IOException {
        int eventType = parser.getEventType();
        boardGameDetails = new ArrayList<BoardGameDetail>();
        BoardGameDetail currentGame = null;

        int currentMax = 0;
        String currentPlayerRating = "";

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG:
                    String name = parser.getName();
                    if(name.equalsIgnoreCase("item")) {
                        currentGame = new BoardGameDetail();
                        currentGame.url = "https://www.boardgamegeek.com/boardgame/" + parser.getAttributeValue("","id");
                        currentMax = 0;
                        currentPlayerRating = "";
                    }
                    if (name.equalsIgnoreCase("name")) {
                        String nameType = parser.getAttributeValue("", "type");
                        if(nameType.equalsIgnoreCase("primary")) {
                            currentGame.name = parser.getAttributeValue("", "value");
                        }
                    }
                    if (name.equalsIgnoreCase("results")) {
                        currentPlayerRating = parser.getAttributeValue("", "numplayers");
                    }
                    if(name.equalsIgnoreCase("result"))
                    {
                        String value = parser.getAttributeValue("", "value");
                        if(value.equalsIgnoreCase("Best")) {
                            int numVotes = Integer.parseInt(parser.getAttributeValue("", "numvotes"));
                            if(numVotes > currentMax) {
                                currentMax = numVotes;
                                currentGame.numPlayers = currentPlayerRating;
                            }
                        }
                    }
                    if(name.equalsIgnoreCase("playingtime"))
                    {
                        currentGame.playingTime = Integer.parseInt(parser.getAttributeValue("", "value"));
                    }
                    break;
                case XmlPullParser.END_TAG:
                    name = parser.getName();
                    if(name.equalsIgnoreCase("item")) {
                        boardGameDetails.add(currentGame);
                    }
                    break;
            }
            eventType = parser.next();
        }

        launchIntent();
    }

    private void launchIntent() {
        progress.dismiss();
        Intent intent = new Intent(this, PlayerSelectActivity.class);
        intent.putExtra(PlayerSelectActivity.BOARD_GAME_LIST, boardGameDetails);
        startActivity(intent);
    }

    private class BGGAsyncTaskRunner extends AsyncTask<NetworkResponseRequest, String, String> {

        public BGGAsyncTaskRunner(RequestQueue queue) {
            volleyQueue = queue;
        }

        protected String doInBackground(NetworkResponseRequest... params) {
            volleyQueue.add(params[0]);
            return "";
        }

        protected void onPostExecute(String result) {
            int i = 0;
        }

        protected void onPreExecute() {
        }

        protected void onProgressUpdate(String... text) {
        }
    }
}

