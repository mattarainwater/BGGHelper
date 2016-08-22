package com.mrainwater.android.bgghelper;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.Html;

import com.mrainwater.android.bgghelper.models.BoardGameDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PlayerSelectActivity extends AppCompatActivity {

    public static String BOARD_GAME_LIST = "boardgames";

    public ArrayList<BoardGameDetail> mainList;
    public ArrayList<BoardGameDetail> timeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playerselect);

        Intent intent = getIntent();
        ArrayList<BoardGameDetail> list = (ArrayList<BoardGameDetail>)intent.getSerializableExtra(PlayerSelectActivity.BOARD_GAME_LIST);

        mainList = list;

        generatePlayerButtons();
    }

    private void generatePlayerButtons() {
        TextView title = (TextView)findViewById(R.id.question_title);
        title.setVisibility(View.VISIBLE);
        title.setText("How many players do you have?");

        LinearLayout scrollView = (LinearLayout)findViewById(R.id.scrollview_layout);
        scrollView.removeAllViews();
        ArrayList<String> playerCounts = new ArrayList<String>();
        for(int i = 0; i < mainList.size(); i++) {
            if(!playerCounts.contains(mainList.get(i).numPlayers)) {
                playerCounts.add(mainList.get(i).numPlayers);
            }
        }
        Collections.sort(playerCounts, new Comparator<String>() {
            public int compare(String string1, String string2)
            {
                return  string1.compareTo(string2);
            }
        });
        for(int i = 0; i < playerCounts.size(); i++) {
            Button myButton = new Button(this);
            myButton.setLayoutParams(new ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.WRAP_CONTENT));
            myButton.setText(playerCounts.get(i));
            myButton.setTag(playerCounts.get(i));
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String)v.getTag();
                    timeList = new ArrayList<BoardGameDetail>();
                    for(int i = 0; i < mainList.size(); i ++) {
                        if(mainList.get(i).numPlayers.equalsIgnoreCase(tag)) {
                            timeList.add(mainList.get(i));
                        }
                    }
                    generateTimeButtons();
                }
            });

            scrollView.addView(myButton);
        }
    }

    private void generateTimeButtons() {
        Intent intent = new Intent(this, TimeSelectActivity.class);
        intent.putExtra(PlayerSelectActivity.BOARD_GAME_LIST, timeList);
        startActivity(intent);
    }
}
