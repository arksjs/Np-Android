package com.lyentech.np;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lyentech.sdk.GreeNp;

import org.json.JSONObject;

/**
 * @author by jason-何伟杰，2022/5/25
 * des:应用常驻页
 * note:如果不方便在BaseActivity插入代码，可在app常驻主页MainActivity插入
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        findViewById(R.id.tvEvent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                JSONObject js = new JSONObject();
                try {
//                    js.put("id", 3);
                    js.put("name", "tom");
//                    js.put("can", true);
                    js.put("s", "g");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //上报事件video_source
//                GreeNp.trackEvent("video_source", js.toString());//例1
//                GreeNp.trackEvent("video_source", "1");//例2
                GreeNp.trackEvent("video_source","xiaomi");
//                GreeNp.trackEvent("video_source");//例3
                startActivity(new Intent(MainActivity.this, OtherActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}