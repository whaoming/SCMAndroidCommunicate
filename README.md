# SCMAndroidCommunicate
Android与51单片机的通信
# 前言
本篇文章将围绕App与单片机的蓝牙通信来说说lz最近进行开发的一些方案与思考
此文分为三部分:

 - 单片机的PWM与串口通信
 - Android的蓝牙开发
 - 单片机与Android App的通信方案

# 预览
![这里写图片描述](http://img.blog.csdn.net/20170131200520434?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXExMjI2MjcwMTg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
![这里写图片描述](http://img.blog.csdn.net/20170131200539794?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvcXExMjI2MjcwMTg=/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)

视频观看：
> http://v.youku.com/v_show/id_XMjQ5MTgyMTAwMA==.html



# 环境
## 单片机

 - 单片机：STC89C52
 - c语言
 - 编写环境：Keil uVision4
 - 烧录：stc-isp-15xx-v6.82

## Android
 - android sdk 16+
 - Android studio 1.0+

# 单片机的PWM与串口通信
## PWM
我相信PWM的概念大家都应该，如果还很模糊也可以去查查，可以看看这篇文章

> http://www.eepw.com.cn/article/275890.htm

那么我们这里要说的，就是怎么通过程序去模拟pwm信号(有些单片机自带pwm处理，就无需写程序)，从程序的方面讲，我们要模拟PWM，就是让高电平持续一小段时间，然后再让低电平持续一段时间，也就是改变占空比。
那么再单片机中，这种关于频率的事情一般都是通过定时器来实现的，那么我的方案是这样的：
**设置一个全局变量t，PWM_T，每当定时器中断的时候使t自增1，当t等于100的时候，使之高电平，并让t等于0，当t等于PWM_T的时候，使之低电平，这样，我们就可以通过改变PWM_T的值来改变占空比，从而实现通过目标的电压，使之达到调节的效果(例如调节led灯的亮度，调节电机的速度等)**

```
/****************************************************
               定时器0中断模拟PWM
               调节led的亮度
****************************************************/
int t = 0;
int PWM_T = 0;   //占空比控制变
void main()
{
	TMOD = 0x22;   //定时器0，工作模式2，8位定时模式
	TH0=210;     //写入预置初值（取值1-255，数越大PWM频率越高）
	TL0=210;     //写入预置值 （取值1-255，数越大PWM频率越高）
	TR0=1;       //启动定时器
	ET0=1;       //允许定时器0中断
	EA=1;        //允许总中断
	P1=0xff; 	 //初始化P1，输出端口
	PWM_T=30;
	while(1)      
	{   	
		if(!up)   //当up按键按下的时候
		{
			if(PWM_T<100)
			{
				PWM_T+=1;
			}
			delay_1ms(20);
		}
		if(!down)  //当down按键按下的时候
		{
			if(PWM_T>0)
			{
				PWM_T-=1;
			}
			delay_1ms(20);
		}
	 }  
}

timer0() interrupt 1  
{ 
	t++;    //每次定时器溢出加1
	if(t==100)   //PWM周期 100个单位
	{
		t=0;  //使t=0，开始新的PWM周期
		P1=0x00;  //输出端口,使之低电平
	} 
	if(PWM_T==t)  //按照当前占空比切换输出为高电平
	{  
		P1=0xff;    //输出端口，使之高电平    
	}
}
```
## 串口通信
上面我们说了PWM调速，那么要达到app实时显示速度，就必须要单片机把速度传输给手机(在这里先用占空比模拟实时速度，道理是一样的，春节快递停了，测速模块还没到)，那么我的首选方案肯定是单片机通过蓝牙串口发送给app，app接收并进行显示，这里我的蓝牙模块是hc-06。串口通信很容易，但在这个过程中我发现难的地方是数据格式的定义和数据的解析，也就是说要统一使用16进制，还是10进制，数据的头节点和尾节点的定义，或者说数据每一位所代表的参数，在这里先埋个伏笔，文章的后面会对我自己的方案进行介绍.

# Android蓝牙开发
那么android为我们提供的关于蓝牙的api其实已经很强大了，通常的步骤为：

 1. 打开蓝牙
 2. 搜索蓝牙设备
 3. 进行配对
 4. 连接
 5. 数据的发送与接收
## 开启蓝牙

```
private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
/**
* 打开蓝牙
*/
public static void openBluetooth(@NonNull Activity activity) {
	if (INSTANCE.bluetoothAdapter == null) {
            // 设备不支持蓝牙
            Toast.makeText(INSTANCE.context.getApplicationContext(), "您的设备似乎不支持蓝牙", Toast.LENGTH_SHORT).show();
            return;
	}
	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	activity.startActivityForResult(enableBtIntent, 6);
}

/**
* 关闭蓝牙
*/
public static void closeBluetooth() {
	if (INSTANCE.bluetoothAdapter != null) {
		INSTANCE.bluetoothAdapter.disable();
	}
}

/**
* 判断蓝牙是否打开
* @return boolean 蓝牙是否打开
*/
public static boolean isBluetoothOpen() {
	return INSTANCE.bluetoothAdapter != null && INSTANCE.bluetoothAdapter.isEnabled();
}
```

## 搜索附近的蓝牙设备
那么搜索蓝牙设备当然也是调用系统的api即可，然后系统通过广播接收者的方式告诉你，我找到设备了，下面po出代码
```
/**
* 搜索蓝牙设备
*/
public static void searchDevices() {
	INSTANCE.bluetoothDevices.clear();
	if (INSTANCE.bluetoothAdapter != null) {
		// 寻找蓝牙设备，android会将查找到的设备以广播形式发出去
		INSTANCE.bluetoothAdapter.startDiscovery();
	}
}
```
下面是所要接收的广播

```

    /**
     * 初始化过滤器
     */
    private void initIntentFilter() {
        // 设置广播信息过滤
        IntentFilter intentFilter = new IntentFilter();
        //搜索到设备
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        //蓝牙状态改变
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        //绑定状态改变
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        // 注册广播接收器，接收并处理搜索结果
        registerReceiver(receiver, intentFilter);
    }
```
那么当接收到广播的时候，只需调用BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)就可以取出对应的搜索的蓝牙设备
## 蓝牙配对
```
 /**
     * 绑定设备
     *
     * @param device BluetoothDevice对象
     * @return 是否绑定成功
     */
    public static boolean bondDevice(BluetoothDevice device) {
        if (INSTANCE.bluetoothAdapter == null) {
            return false;
        }
        // 取消蓝牙设备搜索
        INSTANCE.bluetoothAdapter.cancelDiscovery();
        try {
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                // 设备未配对，进行配对操作
                Method method = BluetoothDevice.class.getMethod("createBond");
                method.invoke(device);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
```
## 蓝牙连接

```
	BluetoothSocket socket;
	//获取一个uuid值
	UUID uuid = uuidCandidates.get(candidate++);
	//根据android不同sdk调用不同的api
	if (secure) {
		socket= device.createRfcommSocketToServiceRecord(uuid);
	} else {
		socket= device.createInsecureRfcommSocketToServiceRecord(uuid);
	}
```
## 数据的发送与接收
参考了网上很多关于蓝牙数据通信的做法，好多都是每发送一次数据都关闭socket，但是那样我觉得并不好，因为socket的开启与关闭都是比较耗费资源的，那么我的方案是开启一个线程保持socket连接进行蓝牙数据的接收与发送。

```
public class TouchMsgThread extends Thread {
    private  BluetoothSocket socket;
    private  InputStream inputStream;
    private  OutputStream outputStream;
    private  Handler handler;
    public TouchMsgThread(BluetoothSocket socket, Handler handler) {
        this.socket = socket;
        InputStream input = null;
        OutputStream output = null;
        this.handler = handler;
        try {
            input = socket.getInputStream();
            output = socket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.inputStream = input;
        this.outputStream = output;
    }
    public void run() {
        while (true) {
            try {
                int count = 5;
                byte[] bytes = new byte[count];
                int readCount = 0; // 已经成功读取的字节的个数
	              while (readCount < count) {
                    readCount += inputStream.read(bytes, readCount, count - readCount);
               }
                int s = BinaryToHexString(bytes);
                Message message=handler.obtainMessage();
                message.what = 333;
                message.obj=s;
                handler.sendMessage(message);
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }
    public void write(byte[] bytes) {
        try {
            byte[] b = {-1,1,2,3,-1};
            outputStream.write(b);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void cancel() {
        try {
            if(outputStream!=null){
                outputStream.close();
                outputStream = null;
            }
            if(inputStream!=null){
                inputStream.close();
                inputStream = null;
            }
            //socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //数据的检验，这里暂时先忽略
    public static int BinaryToHexString(byte[] bytes) {
        int result = 0;
        String temp = "";
        for(int i=0;i<5;i++){
            byte b = bytes[i];
            if(i==2){
                temp = Integer.toHexString((b & 0xff));
            }
            if(i==3){
                String hex = Integer.toHexString((b & 0xff));
                result = Integer.parseInt(temp+hex, 16);
            }
        }
        return result;
    }
}
```

# 单片机与Android的通信方案
## 制定协议
那么上面我们已经讲了单片机与Android怎么样通过蓝牙进行信息交互了，但是在实际应用中，二者之间传递的信息类型太多了，比如实时速度，电量，还有车子灯光打开，或者修改车子密码等等信息，那么单片机或者app要怎么去判断传递过来的是哪种信息呢？那么我们就必须去制定一套数据协议，这里看看我的方案，协议规定：

| 包头   | 类型位 | 数据位  | 数据位   |  结束位   |  
|:-----:|:-----:|:------:|:-------:|:--------:|  
 0xFF | 0x** | 0x** | 0x** | 0xFF |  
那么我们的数据位可以分别代表高二位和低二位，那么通常情况下这种方案就可以满足我们的需求了。举个例子：  
| 类型位  | 数据位 | 数据位  | 功能 |  
|:------:|:-----:|:------:|:-------:|  
| 0X00   |   0X02 | 0X00   | 前进    |  
|0X00   |    0X01 | 0X00   | 后退|  
|0X00     |  0X03 | 0X00  |  左转|  
|0X00   |    0X04 | 0X00   | 右转|  
|0X00     |  0X00 | 0X00  |  停止|  
|0X02     | 0x00| 0X01   |  车灯亮|  
|0X02    | 0x00|  0X02   |  车灯灭|  
|0X03   |  雷达数据高位| 雷达数据低位 |  发送雷达数据 |  

## 协议的解析

