package com.wxxiaomi.ming.buletouthdemo.ui;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.github.anastr.speedviewlib.DeluxeSpeedView;
import com.github.anastr.speedviewlib.PointerSpeedometer;
import com.wxxiaomi.ming.buletouthdemo.BluetoothHelper;
import com.wxxiaomi.ming.buletouthdemo.R;

public class RunActivity2 extends AppCompatActivity {

    PointerSpeedometer pointerSpeedometer;
    private Handler hanlder = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.i("wang","初始化完成，链接成功");
                    break;
                case 333:
                    pointerSpeedometer.speedTo((int)msg.obj);
                break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_run2);
        pointerSpeedometer = (PointerSpeedometer) findViewById(R.id.pointerSpeedometer);
       // pointerSpeedometer.speedTo(90);
//        deluxeSpeedView.setWithEffects(true);
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
                    boolean ona = BluetoothHelper.printDocument(BluetoothHelper.getCurrentDevice(), "ONA",hanlder);
                    if(ona){
                        hanlder.sendEmptyMessage(1);
                    }
                }
            }
        }.start();

    }


    @Override
    protected void onDestroy() {
        BluetoothHelper.Destory();
        super.onDestroy();
    }
}
