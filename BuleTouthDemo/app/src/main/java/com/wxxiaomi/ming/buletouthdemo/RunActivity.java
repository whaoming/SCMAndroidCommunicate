package com.wxxiaomi.ming.buletouthdemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wxxiaomi.ming.buletouthdemo.ui.RunActivity2;

import java.util.Collections;
import java.util.Comparator;

public class RunActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView tv;
    private Button ONA;
    private Button ONB;
    private Button ONC;
    private Button OND;
    private Button ONF;
    private Button send;
    private EditText et;
    private Handler hanlder = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.i("wang","初始化完成，链接成功");
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run);
        tv = (TextView) findViewById(R.id.tv);
        ONA = (Button) findViewById(R.id.ONA);
        ONA.setOnClickListener(this);
        ONB = (Button) findViewById(R.id.ONB);
        ONB.setOnClickListener(this);
        ONC = (Button) findViewById(R.id.ONC);
        ONC.setOnClickListener(this);
        OND = (Button) findViewById(R.id.OND);
        OND.setOnClickListener(this);
        ONF = (Button) findViewById(R.id.ONF);
        ONF.setOnClickListener(this);
        send = (Button) findViewById(R.id.send);
        send.setOnClickListener(this);

        et = (EditText) findViewById(R.id.et);
        registe();
        init();


    }

    private void init() {
        Log.i("wang","假装这个是progress：正在初始化");
        //此时应该测试链接
        new Thread(){
            @Override
            public void run() {
                boolean b = BluetoothHelper.bondDevice(BluetoothHelper.getCurrentDevice());
                if(b){
                    boolean ona = BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(), "ONA",null);
                    if(ona){
                        hanlder.sendEmptyMessage(1);
                    }
                }
            }
        }.start();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ONA:
                BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(),"ONA",null);
                break;
            case R.id.ONB:
                BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(),"ONB",null);
                break;
            case R.id.ONC:
                BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(),"ONC",null);
                break;
            case R.id.OND:
                BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(),"OND",null);
                break;
            case R.id.ONF:
                BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(),"ONF",null);
                break;
            case R.id.send:
                String text = et.getText().toString().trim();
                if(!TextUtils.isEmpty(text)){
                    BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(),text,null);
                }

                break;
        }
    }

    @Override
    protected void onDestroy() {
        BluetoothHelper.Destory();
        super.onDestroy();
    }

    public void registe(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, intentFilter);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        /**
         * 搜索设备的进度条对话框
         */

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                Log.i("wang","收到广播：BluetoothDevice.ACTION_ACL_DISCONNECTED");
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                Log.i("wang","收到广播:BluetoothDevice.ACTION_ACL_DISCONNECTED");
            }
        }
    };
}
