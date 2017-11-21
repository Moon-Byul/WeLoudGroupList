package com.example.reveu.weloudgrouplist;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import java.util.HashMap;

public class Main extends AppCompatActivity
{
    FragLogin fragmentlogin;
    FragRegister fragmentRegister;

    private static final String TAG_USERNUM = "userNum";
    private static final String TAG_ID = "ID";
    private static final String TAG_NICKNAME ="nickName";
    private static final String TAG_REGISTERDATE ="registerDate";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("WeLoud");

        fragmentlogin = new FragLogin();

        fragmentRegister = new FragRegister();
    }

    public void onFragmentChanged(int index)
    {
        if(index==1) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragmentRegister).commit();
        }
        else if(index==2)
            getSupportFragmentManager().beginTransaction().replace(R.id.container,fragmentlogin).commit();
    }

    public void login(HashMap<String, String> map)
    {
        // 정상적으로 로그인이됨
        String userNum = map.get(TAG_USERNUM);
        String id = map.get(TAG_ID);
        String nickName = map.get(TAG_NICKNAME);
        String registerDate = map.get(TAG_REGISTERDATE);

        Toast.makeText(this, getString(R.string.text_hello, nickName), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(Main.this, GroupList.class);
        intent.putExtra(TAG_USERNUM, userNum);
        intent.putExtra(TAG_ID, id);
        intent.putExtra(TAG_NICKNAME, nickName);
        intent.putExtra(TAG_REGISTERDATE, registerDate);
        startActivity(intent);
        finish();
    }
}
