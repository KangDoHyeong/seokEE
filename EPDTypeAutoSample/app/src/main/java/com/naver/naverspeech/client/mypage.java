package com.naver.naverspeech.client;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import static com.naver.naverspeech.client.commSock.REQUEST_USERINFO;
import static com.naver.naverspeech.client.commSock.gson;
import static com.naver.naverspeech.client.enter._enter;


public class mypage extends AppCompatActivity {

    private Adapter adapter;
    RequestUserInfo info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mypage);

        // 바꾸신 후 이 아래에서 초기화 진행해주시면 됩니다.
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new Adapter();
        recyclerView.setAdapter(adapter);

        new Thread(new Runnable(){
            public void run(){
                commSock.kick(REQUEST_USERINFO, "");
                String message = commSock.read();

                info = gson.fromJson(message, RequestUserInfo.class);
                runOnUiThread(new Runnable(){
                    public void run(){
                        setData(info);
                    }
                });
            }
        }).start();

        Button logoutBtn = findViewById(R.id.logout);

        logoutBtn.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                SharedPreferences.Editor auto_login = getSharedPreferences("auto_login", MODE_PRIVATE).edit();
                auto_login.putString("id", "");
                auto_login.putString("pwd", "");
                auto_login.apply();

                Intent intent = new Intent(mypage.this, loginActivity.class);
                startActivity(intent);
                _enter.finish();
                finish();
            }
        });
    }
    private void setData(RequestUserInfo info){
        TextView nickname_tv = findViewById(R.id.nickname_tv);
        TextView id_tv = findViewById(R.id.id_tv);
        TextView talkwithme1 = findViewById(R.id.talkWithMe1);
        TextView talkwithme2 = findViewById(R.id.talkWithMe2);
        TextView talkwithme3 = findViewById(R.id.talkWithMe3);
        TextView contribution1 = findViewById(R.id.contributionTV1);
        TextView contribution2 = findViewById(R.id.contributionTV2);
        TextView contribution3 = findViewById(R.id.contributionTV3);

        id_tv.setText(info.id);
        String[] twm = info.talkWithMe.split(" ");
        nickname_tv.setText(info.nickname);
        talkwithme1.setText(twm[0]);
        if(twm.length >= 2)
            talkwithme2.setText(twm[1]);
        if(twm.length >= 3)
        talkwithme3.setText(twm[2]);

        String s = String.format(Locale.KOREA,"%.2f" , info.contributionData.get(1)) + "%";
        String s2 = String.format(Locale.KOREA,"%.2f" , info.contributionData.get(0)) + "%";
        String s3 = String.format(Locale.KOREA,"%.2f" , (100-info.contributionData.get(2))) + "%";

        contribution1.setText(s);
        contribution2.setText(s2);
        contribution3.setText(s3);


        for (History h : info.histories){
            // 각 List의 값들을 data 객체에 set 해줍니다.

            if(h.getContent().equals("")) continue;

            Data data = new Data();
            data.setTitle(h.getTitle());

            String[] start = h.getDate().split(" ");
            // 5 1 2 0
            StringBuilder sb = new StringBuilder();

            int i;

            if(start[1].equals("Jan")) i = 1;
            else if(start[1].equals("Feb")) i = 2;
            else if(start[1].equals("Mar")) i = 3;
            else if(start[1].equals("Apr")) i = 4;
            else if(start[1].equals("May")) i = 5;
            else if(start[1].equals("Jun")) i = 6;
            else if(start[1].equals("Jul")) i = 7;
            else if(start[1].equals("Aug")) i = 8;
            else if(start[1].equals("Sep")) i = 9;
            else if(start[1].equals("Oct")) i =10;
            else if(start[1].equals("Nov")) i = 11;
            else i = 12;

            sb.append(start[5]).append(".").append(i).append(".").append(start[2]).append(" (").append(start[0]).append(")");

            data.setDate(sb.toString());
            data.setMember("With - "+h.getMembers());
            data.setNumber(h.getNumber());

            String content = h.getContent();
            if(content.length()>100)
                data.setContent(content.substring(0,100));
            else   data.setContent(content);

            // 각 값이 들어간 data를 adapter에 추가합니다.
            adapter.addItem(data);
        }

        // adapter의 값이 변경되었다는 것을 알려줍니다.
        adapter.notifyDataSetChanged();
    }
}
