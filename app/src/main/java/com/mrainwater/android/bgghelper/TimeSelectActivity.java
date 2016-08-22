package com.mrainwater.android.bgghelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Range;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mrainwater.android.bgghelper.PlayerSelectActivity;
import com.mrainwater.android.bgghelper.R;
import com.mrainwater.android.bgghelper.models.BoardGameDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class TimeSelectActivity extends AppCompatActivity {

    public ArrayList<BoardGameDetail> linkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeselect);

        Intent intent = getIntent();
        final ArrayList<BoardGameDetail> list = (ArrayList<BoardGameDetail>)intent.getSerializableExtra(PlayerSelectActivity.BOARD_GAME_LIST);

        TextView title = (TextView)findViewById(R.id.question_title);
        title.setVisibility(View.VISIBLE);
        title.setText("How much time do you have?");

        LinearLayout scrollView = (LinearLayout)findViewById(R.id.scrollview_layout);
        scrollView.removeAllViews();
        ArrayList<Integer> times = new ArrayList<Integer>();
        for(int i = 0; i < list.size(); i++) {
            Range<Integer> range;
            if(!times.contains(list.get(i).playingTime)) {
                times.add(list.get(i).playingTime);
            }
        }

        Collections.sort(times, new Comparator<Integer>() {
            public int compare(Integer int1, Integer int2)
            {
                return  int1.compareTo(int2);
            }
        });

        for(int i = 0; i < times.size(); i++) {
            Button myButton = new Button(this);
            myButton.setLayoutParams(new ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.WRAP_CONTENT));
            myButton.setText(times.get(i).toString() + " minutes");
            myButton.setTag(times.get(i).toString());
            myButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String)v.getTag();
                    linkList = new ArrayList<BoardGameDetail>();
                    for(int i = 0; i < list.size(); i ++) {
                        if(list.get(i).playingTime == Integer.parseInt(tag)) {
                            linkList.add(list.get(i));
                        }
                    }
                    generateLinks();
                }
            });

            scrollView.addView(myButton);
        }
    }

    private void generateLinks() {
        Intent intent = new Intent(this, GameSelectActivity.class);
        intent.putExtra(PlayerSelectActivity.BOARD_GAME_LIST, linkList);
        startActivity(intent);
    }
}
