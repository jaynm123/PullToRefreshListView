package com.jaynm.pulltorefreshscrollviewdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }


    public void listviewOnclick(View view) {
        switch (view.getId()) {
            case R.id.listview:
                startActivity(ListViewActivity.class);
                break;
            case R.id.swipe:
                startActivity(SwipeListViewActivity.class);
                break;
        }
    }

    public void startActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
    }
}
