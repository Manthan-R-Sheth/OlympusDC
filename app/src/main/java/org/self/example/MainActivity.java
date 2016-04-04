package org.self.example;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    Button submit;
    EditText lock;
    DataReceiver receiver;
    HashMap<String,String> usersonline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        uisetup();
        usersonline=new HashMap<>();
        IntentFilter filter = new IntentFilter(DataReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new DataReceiver();
        registerReceiver(receiver, filter);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent datasend=new Intent(MainActivity.this,DataSendService.class);
                startService(datasend);
            }
        });
    }

    private void uisetup() {
        submit=(Button)findViewById(R.id.Submit);
        lock=(EditText)findViewById(R.id.edit1);
    }

    public class DataReceiver extends BroadcastReceiver{
        public static final String ACTION_RESP =
                "com.example.intent.action.MESSAGE_PROCESSED";
        @Override
        public void onReceive(Context context, Intent intent) {
            usersonline=(HashMap<String,String>)intent.getSerializableExtra(DataSendService.PARAM_OUT_MSG);


        }
    }

}
