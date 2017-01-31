package com.wxxiaomi.ming.buletouthdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.wxxiaomi.ming.buletouthdemo.ui.RunActivity2;

public class HomeActivity extends AppCompatActivity {

    private Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        BluetoothHelper.getInstance().init(getApplicationContext());
        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = null;
                if(BluetoothHelper.isEverDevice()){
                    intent = new Intent(HomeActivity.this,RunActivity2.class);
                }else{
                    intent = new Intent(HomeActivity.this,BoundActivity.class);
                }
                startActivity(intent);
            }
        });
    }
}
