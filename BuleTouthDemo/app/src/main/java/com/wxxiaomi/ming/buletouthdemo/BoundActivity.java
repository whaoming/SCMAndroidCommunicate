package com.wxxiaomi.ming.buletouthdemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

//import com.wxxiaomi.ming.buletouthdemo.latest.BleUtil;

import com.wxxiaomi.ming.buletouthdemo.ui.RunActivity2;

import java.util.Collections;
import java.util.Comparator;

public class BoundActivity extends AppCompatActivity {

//    private BluetoothHelper helper;

//    private TextView tv_current_bluetooth_status;
    private Button btn_search_devices;
    private ListView lv_bluetooth_devices;
    private EditText et;
    /**
     * 适配器
     */
    private BluetoothDeviceAdapter deviceAdapter;
    /**
     * 询问笔录数据
     */
//    private InquiryTranscripts data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        helper = new BluetoothHelper(this.getApplicationContext());

        initView();
        initListener();

        if (BluetoothHelper.isBluetoothOpen()) {
            BluetoothHelper.searchDevices();
        }
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        et = (EditText) findViewById(R.id.et);
        btn_search_devices = (Button) findViewById(R.id.btn_search_devices);
        lv_bluetooth_devices = (ListView) findViewById(R.id.lv_bluetooth_devices);
        changeBluetoothStatus();
        deviceAdapter = new BluetoothDeviceAdapter(BoundActivity.this, BluetoothHelper.getBluetoothDevices());
        lv_bluetooth_devices.setAdapter(deviceAdapter);
    }



    /**
     * 修改界面中关于蓝牙状态的显示状态
     */
    private void changeBluetoothStatus() {
        String argStr = "当前蓝牙状态：%1$s";
        if (BluetoothHelper.isBluetoothOpen()) {
//            tv_current_bluetooth_status.setText(String.format(argStr, "已开启"));
//            btn_open_bluetooth.setVisibility(View.GONE);
//            btn_search_devices.setEnabled(true);
        } else {
//            tv_current_bluetooth_status.setText(String.format(argStr, "已关闭"));
//            btn_open_bluetooth.setVisibility(View.VISIBLE);
//            btn_search_devices.setEnabled(false);
        }
    }

    /**
     * 初始化控件监听器
     */
    private void initListener() {
//        btn_open_bluetooth.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!helper.isBluetoothOpen()) {
//                    helper.openBluetooth(BoundActivity.this);
//                }
//            }
//        });
        btn_search_devices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BluetoothHelper.searchDevices();
            }
        });
        lv_bluetooth_devices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                Log.i("wang","i:"+i);
                final BluetoothDevice device = BluetoothHelper.getBluetoothDevices().get(i);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    new AlertDialog.Builder(BoundActivity.this).setTitle("标题")
                            .setMessage("确定使用" + device.getName() + "进行打印吗？")
                            .setPositiveButton("取消", null)
                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    String text = et.getText().toString().trim();
                                    BluetoothHelper.printDocument(device,text,null);

                                }
                            }).create().show();
                } else if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    // 正在绑定
                    Toast.makeText(getApplicationContext(), "正在配对该设备，暂时无法使用", Toast.LENGTH_SHORT).show();
                } else {
                    // 没有绑定
                    new AlertDialog.Builder(BoundActivity.this).setTitle("标题")
                            .setMessage("您确定要配对该设备吗？")
                            .setPositiveButton("取消", null)
                            .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int a) {
//                                    BluetoothHelper.getBluetoothDevices().get(i).

                                    boolean b = BluetoothHelper.bondDevice(BluetoothHelper.getBluetoothDevices().get(i));
                                    Log.i("wang","绑定的结果："+b);
                                }
                            }).create().show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 6) {
            // 打开蓝牙的回调
            changeBluetoothStatus();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        initIntentFilter();
    }

    /**
     * 初始化过滤器
     */
    private void initIntentFilter() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
//        intentFilter.addAction(BluetoothAdapter.ACTION_BOND_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        registerReceiver(receiver, intentFilter);
    }

    /**
     * 标记是否正在搜索设备
     */
    private boolean isSearching = false;

    /**
     * 蓝牙广播接收器
     */
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        /**
         * 搜索设备的进度条对话框
         */
        private ProgressDialog dialog;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // 搜索到设备
                Log.i("wang","搜索到设备");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothHelper.addBluetoothDevices(device);
            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i("wang","开始搜索");
                // 开始搜索
                dialog = new ProgressDialog(context);
                dialog.setMessage("正在搜索设备，请稍候...");
                dialog.setCancelable(false);
                dialog.show();

                isSearching = true;
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.i("wang","搜索结束");
                // 搜索结束
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }

                if (isSearching) {
                    Toast.makeText(BoundActivity.this.getApplicationContext(), "搜索结束，共搜索到" + BluetoothHelper.getBluetoothDevices().size() + "台设备", Toast.LENGTH_SHORT).show();
                }
                isSearching = false;
                // 根据配对情况进行排序
                Collections.sort(BluetoothHelper.getBluetoothDevices(), new Comparator<BluetoothDevice>() {
                    @Override
                    public int compare(BluetoothDevice device, BluetoothDevice t1) {
                        return device.getBondState() - t1.getBondState();
                    }
                });

                deviceAdapter.notifyDataSetChanged();
            }else if(BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)){

            }
            else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                Log.i("wang","状态改变");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                switch (device.getBondState()) {
                    case BluetoothDevice.BOND_BONDING://正在配对
                        Log.i("wang", "正在配对......");
//                        onRegisterBltReceiver.onBltIng(device);
                        break;
                    case BluetoothDevice.BOND_BONDED://配对结束
                        Log.i("wang", "完成配对");
//                        onRegisterBltReceiver.onBltEnd(device);
                        Intent i = new Intent(BoundActivity.this,RunActivity2.class);
                        startActivity(i);
                        finish();
                        break;
                    case BluetoothDevice.BOND_NONE://取消配对/未配对
                        Log.i("wang", "取消配对");
//                        onRegisterBltReceiver.onBltNone(device);
                    default:
                        break;
                }
            }
            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                Log.i("wang", "ACTION_STATE_CHANGED");
                changeBluetoothStatus();
            }
        }
    };

    @Override
    protected void onStop() {
        super.onStop();

        unregisterReceiver(receiver);
    }
}
