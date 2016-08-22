package com.mrainwater.android.bgghelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mrainwater.android.bgghelper.models.BoardGameDetail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class GameSelectActivity extends AppCompatActivity {

    public ArrayList<BoardGameDetail> linkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeselect);

        Intent intent = getIntent();
        final ArrayList<BoardGameDetail> list = (ArrayList<BoardGameDetail>)intent.getSerializableExtra(PlayerSelectActivity.BOARD_GAME_LIST);

        TextView title = (TextView)findViewById(R.id.question_title);
        title.setVisibility(View.GONE);

        LinearLayout scrollView = (LinearLayout)findViewById(R.id.scrollview_layout);

        Collections.sort(list, new Comparator<BoardGameDetail>() {
            public int compare(BoardGameDetail detail1, BoardGameDetail detail2)
            {
                return  detail1.name.compareTo(detail2.name);
            }
        });
        scrollView.removeAllViews();

        for(int i = 0; i < list.size(); i++) {
            TextView link = new TextView(this);
            link.setLayoutParams(new ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.WRAP_CONTENT));
            link.setMovementMethod(LinkMovementMethod.getInstance());
            link.setClickable(true);
            String text = "<a href='" + list.get(i).url + "'> " + list.get(i).name + " </a>";
            link.setText(Html.fromHtml(text));
            scrollView.addView(link);
        }
    }
}
