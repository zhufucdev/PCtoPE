package com.zhufuc.pctope.Activities;

import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.netease.nis.bugrpt.CrashHandler;
import com.zhufuc.pctope.R;

public class UserBugReport extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_bug_report);
        final TextView content = (TextView)findViewById(R.id.user_report);
        Button confirm = (Button)findViewById(R.id.user_report_confirm);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!content.getText().toString().equals("")){
                    TelephonyManager tm = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    CrashHandler.uploadUserDefineLog("用户反馈", Build.MODEL+">"+tm.getDeviceId()+":"+content.getText().toString());
                }
                finish();
            }
        });

    }
}
