//package com.wxxiaomi.ming.buletouthdemo.latest;
//
//import android.annotation.TargetApi;
//import android.app.Activity;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothGatt;
//import android.bluetooth.BluetoothGattCallback;
//import android.bluetooth.BluetoothGattCharacteristic;
//import android.bluetooth.BluetoothGattDescriptor;
//import android.bluetooth.BluetoothGattService;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.BluetoothProfile;
//import android.content.Context;
//import android.content.pm.PackageManager;
//import android.os.Build;
//import android.os.Handler;
//import android.util.Log;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
///**
// * Created by shaolin on 6/17/16.
// */
//@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
//public class BleUtil {
//
//    private static final String TAG = "BleUtil";
//    private static final long SCAN_PERIOD = 10000;
//
//    public static String characterUUID1 = "0000fff2-0000-1000-8000-00805f9b34fb";//APP发送命令
//    public static String characterUUID2 = "0000fff1-0000-1000-8000-00805f9b34fb";//BLE用于回复命令
//    private static String descriptorUUID = "00002902-0000-1000-8000-00805f9b34fb";//BLE设备特性的UUID
//
//    public static byte[] workModel = {0x02, 0x01};
//
//    private Context mContext;
//    private static BleUtil mInstance;
//
//    //作为中央来使用和处理数据；
//    private BluetoothGatt mGatt;
//
//    private BluetoothManager manager;
//    private BTUtilListener mListener;
//    private BluetoothDevice mCurDevice;
//    private BluetoothAdapter mBtAdapter;
//    private List<BluetoothDevice> listDevice;
//    private List<BluetoothGattService> serviceList;//服务
//    private List<BluetoothGattCharacteristic> characterList;//特征
//
//    private BluetoothGattService service;
//    private BluetoothGattCharacteristic character1;
//    private BluetoothGattCharacteristic character2;
//
//
//    public static synchronized BleUtil getInstance() {
//        if (mInstance == null) {
//            mInstance = new BleUtil();
//        }
//        return mInstance;
//    }
//
//
//    public void setContext(Context context) {
//        mContext = context;
//        init();
//    }
//
//    public void init() {
//        listDevice = new ArrayList<>();
//        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            showToast("BLE不支持此设备!");
//            ((Activity) mContext).finish();
//        }
//        manager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
//        //注：这里通过getSystemService获取BluetoothManager，
//        //再通过BluetoothManager获取BluetoothAdapter。BluetoothManager在Android4.3以上支持(API level 18)。
//        if (manager != null) {
//            mBtAdapter = manager.getAdapter();
//        }
//        if (mBtAdapter == null || !mBtAdapter.isEnabled()) {
//            mBtAdapter.enable();
//            /*Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            mContext.startActivity(enableBtIntent);*/
//        }
//    }
//
//    //扫描设备的回调
//    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//
//        @Override
//        public void onLeScan(final BluetoothDevice device, int rssi,
//                             byte[] scanRecord) {
//            ((Activity) mContext).runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (!listDevice.contains(device)) {
//                        //不重复添加
//                        listDevice.add(device);
//                        mListener.onLeScanDevices(listDevice);
//                        Log.e(TAG, "device:" + device.toString());
//                    }
//                }
//            });
//        }
//    };
//
//    //扫描设备
//    public void scanLeDevice(final boolean enable) {
//        if (enable) {
//            Handler mHandler = new Handler();
//            mHandler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    stopScan();
//                    Log.e(TAG, "run: stop");
//                }
//            }, SCAN_PERIOD);
//            startScan();
//            Log.e(TAG, "start");
//        } else {
//            stopScan();
//            Log.e(TAG, "stop");
//        }
//    }
//
//    //开始扫描BLE设备
//    private void startScan() {
//        mBtAdapter.startLeScan(mLeScanCallback);
//        mListener.onLeScanStart();
//    }
//
//    //停止扫描BLE设备
//    private void stopScan() {
//        mBtAdapter.stopLeScan(mLeScanCallback);
//        mListener.onLeScanStop();
//    }
//
//    //返回中央的状态和周边提供的数据
//    private BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status,
//                                            int newState) {
//            Log.e(TAG, "onConnectionStateChange");
//            switch (newState) {
//                case BluetoothProfile.STATE_CONNECTED:
//                    Log.e(TAG, "STATE_CONNECTED");
//                    mListener.onConnected(mCurDevice);
//                    gatt.discoverServices(); //搜索连接设备所支持的service
//                    break;
//                case BluetoothProfile.STATE_DISCONNECTED:
//                    mListener.onDisConnected(mCurDevice);
//                    disConnGatt();
//                    Log.e(TAG, "STATE_DISCONNECTED");
//                    break;
//                case BluetoothProfile.STATE_CONNECTING:
//                    mListener.onConnecting(mCurDevice);
//                    Log.e(TAG, "STATE_CONNECTING");
//                    break;
//                case BluetoothProfile.STATE_DISCONNECTING:
//                    mListener.onDisConnecting(mCurDevice);
//                    Log.e(TAG, "STATE_DISCONNECTING");
//                    break;
//            }
//            super.onConnectionStateChange(gatt, status, newState);
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            Log.d(TAG, "onServicesDiscovered");
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                serviceList = gatt.getServices();
//                for (int i = 0; i < serviceList.size(); i++) {
//                    BluetoothGattService theService = serviceList.get(i);
//
//                    Log.e(TAG, "ServiceName:" + theService.getUuid());
//                    characterList = theService.getCharacteristics();
//                    for (int j = 0; j < characterList.size(); j++) {
//                        String uuid = characterList.get(j).getUuid().toString();
//                        Log.e(TAG, "---CharacterName:" + uuid);
//                        if (uuid.equals(characterUUID1)) {
//                            character1 = characterList.get(j);
//                        } else if (uuid.equals(characterUUID2)) {
//                            character2 = characterList.get(j);
//                            setNotification();
//                        }
//                    }
//                }
//            }
//            super.onServicesDiscovered(gatt, status);
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.e(TAG, "onCharacteristicRead");
//            super.onCharacteristicRead(gatt, characteristic, status);
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            Log.e(TAG, "onCharacteristicWrite");
//            super.onCharacteristicWrite(gatt, characteristic, status);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            Log.e(TAG, "onCharacteristicChanged");
////            这里是可以监听到设备自身或者手机改变设备的一些数据修改h通知
//            receiveData(characteristic);
//            super.onCharacteristicChanged(gatt, characteristic);
//        }
//    };
//
//    //获取设备指定的特征中的特性,其中对其进行监听, setCharacteristicNotification与上面的回调onCharacteristicChanged进行一一搭配
//    private void setNotification() {
//        mGatt.setCharacteristicNotification(character2, true);
//        BluetoothGattDescriptor descriptor = character2.getDescriptor(UUID.fromString(descriptorUUID));
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        mGatt.writeDescriptor(descriptor);
//    }
//
//    //接收数据,对其进行处理
//    private void receiveData(BluetoothGattCharacteristic ch) {
//        byte[] bytes = ch.getValue();
//        int cmd = bytes[0];
//        int agree = bytes[1];
//        switch (cmd) {
//            case 1:
//                mListener.onStrength(agree);
//                Log.e(TAG, "手机通知BLE设备强度:" + agree);
//                break;
//            case 2:
//                mListener.onModel(agree);
//                Log.e(TAG, "工作模式:" + agree);
//                break;
//            case 3:
//                mListener.onStrength(agree);
//                Log.e(TAG, "设备自身通知改变强度:" + agree);
//                break;
//        }
//    }
//
//    //连接设备
//    public void connectLeDevice(int devicePos) {
//        mBtAdapter.stopLeScan(mLeScanCallback);
//        mCurDevice = listDevice.get(devicePos);
//        checkConnGatt();
//    }
//
//    //发送进入工作模式请求
//    public void sendWorkModel() {
//        if (character1 != null) {
//            character1.setValue(workModel);
//            mGatt.writeCharacteristic(character1);
//        }
//    }
//
//    //发送强度
//    public void sendStrength(String strength) {
////        byte[] strengthModel = {0x01, (byte) strength};
//        if (character1 != null) {
//            character1.setValue(strength.getBytes());
//            mGatt.writeCharacteristic(character1);
//        }
//    }
//
//    //检查设备是否连接了
//    private void checkConnGatt() {
//        if (mGatt == null) {
//            mGatt = mCurDevice.connectGatt(mContext, true, mGattCallback);
//            mListener.onConnecting(mCurDevice);
//        } else {
//            mGatt.connect();
//            mGatt.discoverServices();
//        }
//    }
//
//    //  断开设备连接
//    private void disConnGatt() {
//        if (mGatt != null) {
//            mGatt.disconnect();
//            mGatt.close();
//            mGatt = null;
//            listDevice = new ArrayList<>();
//            mListener.onLeScanDevices(listDevice);
//        }
//    }
//
//    private void showToast(String message) {
//        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
//    }
//
//    public void setBTUtilListener(BTUtilListener listener) {
//        mListener = listener;
//    }
//
//    public interface BTUtilListener {
//        void onLeScanStart(); // 扫描开始
//
//        void onLeScanStop();  // 扫描停止
//
//        void onLeScanDevices(List<BluetoothDevice> listDevice); //扫描得到的设备
//
//        void onConnected(BluetoothDevice mCurDevice); //设备的连接
//
//        void onDisConnected(BluetoothDevice mCurDevice); //设备断开连接
//
//        void onConnecting(BluetoothDevice mCurDevice); //设备连接中
//
//        void onDisConnecting(BluetoothDevice mCurDevice); //设备连接失败
//
//        void onStrength(int strength); //给设备设置强度
//
//        void onModel(int model); //设备模式
//    }
//}