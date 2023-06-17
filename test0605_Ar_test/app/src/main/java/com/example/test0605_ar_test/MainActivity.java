package com.example.test0605_ar_test;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView tv_wait;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 關掉標題列
        this.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Button arButton = (Button) findViewById(R.id.arButton);
        Button myFavorite = (Button) findViewById(R.id.myFavorite);
        Button clearBtn = (Button) findViewById(R.id.clearBtn);
        tv_wait = (TextView) findViewById(R.id.tv_wait);

        pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
//        pref.getBoolean("isFavorite",false);

        arButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_wait.setText("讀取中，請稍候...");
                Intent intent = new Intent(MainActivity.this, ArActivity.class);
                startActivity(intent);
            }
        });

        myFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tv_wait.setText("讀取中，請稍候...");
                Intent intent = new Intent(MainActivity.this, myFavorite.class);
//                mIntent = getIntent();
//                mBundle = mIntent.getExtras();
                if ((pref.getBoolean("isFavorite",false)==false)&&(pref.getBoolean("redFavorite",false)==false)&&(pref.getBoolean("yellowFavorite",false)==false)&&(pref.getBoolean("blueFavorite",false)==false)&&(pref.getBoolean("whiteFavorite",false)==false)) {
                    tv_wait.setText("尚無收藏，請先實體掃描");
                    return;
                }
                startActivity(intent);
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示")
                        .setMessage("確定要清除嗎？")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                pref = getSharedPreferences("Favorite_DATA",MODE_PRIVATE);
                                pref.edit()
                                        .clear()
                                        .apply();
                                tv_wait.setText("已清除收藏，歡迎再度收藏");
                            }
                        })
                        .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });

                AlertDialog dialog = builder.create();
                dialog.show();


            }
        });
    }
}
