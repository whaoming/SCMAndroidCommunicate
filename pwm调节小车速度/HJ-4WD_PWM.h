/*****************HJ-4WD慧净四驱蓝牙智能小车PWM专用头文件*****************
*  平台：HJ-4WD/HJ-E/HL-1智能小车 + Keil U4 + STC89C52
*  名称：
*  公司：慧净电子科技有限公司
*  淘宝：http://shop37031453.taobao.com       
*  网站：www.hjmcu.com	智能小车专用网站：WWW.HLMCU.COM
*  编写：慧净
*  日期：2008-8-08
*  交流:智能车QQ:1526248688  开发板QQ:398115088
*  晶振:11.0592MHZ
*  说明：免费开源，不提供源代码分析，有问题直接到慧净论坛交流
*  论坛：http://hjdz.haotui.com 
*  免费结缘：5星好评赠送18个资料包，追加评价后再加送20G资料，网上网盘下载
*  视频教程：本小车配套详细视频教程，有需要请到慧净网站免费下载
******************************************************************/
#ifndef _LED_H_
#define _LED_H_


    //定义小车驱动模块输入IO口 
	sbit IN1=P1^2;
	sbit IN2=P1^3;
	sbit IN3=P1^6;
	sbit IN4=P1^7;
	sbit EN1=P1^4;
	sbit EN2=P1^5;
	

	/***蜂鸣器接线定义*****/
    sbit BUZZ=P2^3;

    #define Left_1_led        P3_5	 //左传感器  
	

    #define Right_1_led       P3_4	 //右传感器    
   
	 
	#define Left_moto_pwm	  P1_5	 //PWM信号端

	
	#define Right_moto_pwm	  P1_4	 //PWM信号端
	

	#define Left_moto_go      {P1_2=0,P1_3=1;}  //左电机向前走
	#define Left_moto_back    {P1_2=1,P1_3=0;} 	//左边电机向后转
	#define Left_moto_Stop    {P1_5=0;}         //左边电机停转                     
	#define Right_moto_go     {P1_6=1,P1_7=0;}	//右边电机向前走
	#define Right_moto_back   {P1_6=0,P1_7=1;}	//右边电机向后走
	#define Right_moto_Stop   {P1_4=0;}      	//右边电机停转  

	unsigned char pwm_val_left  =0;//变量定义
	unsigned char push_val_left =0;// 左电机占空比N/20
	unsigned char pwm_val_right =0;
	unsigned char push_val_right=0;// 右电机占空比N/20
	bit Right_moto_stop=1;
	bit Left_moto_stop =1;
	unsigned  int  time=0;
   
/************************************************************************/	
//延时函数	
   void delay(unsigned int k)
{    
     unsigned int x,y;
	 for(x=0;x<k;x++) 
	   for(y=0;y<2000;y++);
}
/************************************************************************/
//前速前进
     void  run(unsigned int k)
{
     push_val_left=k;	 //速度调节变量 0-20。。。0最小，20最大
	 push_val_right=k;
	 Left_moto_go ;   //左电机往前走
	 Right_moto_go ;  //右电机往前走
}

//后退函数 如果看不明白，请看慧净电子51智能小车视频教程
     void  backrun(void)
{
     push_val_left=8;	 //速度调节变量 0-20。。。0最小，20最大
	 push_val_right=8;
	 Left_moto_back;   //左电机往前走
	 Right_moto_back;  //右电机往前走
}

//左转
     void  leftrun(void)
{	 
     //push_val_left=5;
	 push_val_right=8;
	 Right_moto_go ;  //右电机往前走
     Left_moto_Stop ;  //左电机停止
}

//右转
     void  rightrun(void)
{ 
	 push_val_left=6;
	 push_val_right=4;
     Left_moto_go  ;   //左电机往前走
	 //Right_moto_Stop   ;  //右电机	
}


/************************************************************************/
/*                    PWM调制电机转速                                   */
/************************************************************************/
/*                    左电机调速                                        */
/*调节push_val_left的值改变电机转速,占空比            */
		void pwm_out_left_moto(void)
{  
   if(Left_moto_stop)
   {
    if(pwm_val_left<=push_val_left)
	       {
		     Left_moto_pwm=1; 
//		     Left_moto_pwm1=1; 
		   }
	else 
	       {
	         Left_moto_pwm=0;
//		     Left_moto_pwm1=0; 
		   }
	if(pwm_val_left>=50)
	       pwm_val_left=0;
   }
   else    
          {
           Left_moto_pwm=0;
//           Left_moto_pwm1=0; 
		  }
}
/******************************************************************/
/*                    右电机调速                                  */  
   void pwm_out_right_moto(void)
{ 
  if(Right_moto_stop)
   { 
    if(pwm_val_right<=push_val_right)
	      {
	       Right_moto_pwm=1; 
//		   Right_moto_pwm1=1; 
		   }
	else 
	      {
		   Right_moto_pwm=0;
//		   Right_moto_pwm1=0; 
		  }
	if(pwm_val_right>=50)
	       pwm_val_right=0;
   }
   else    
          {
           Right_moto_pwm=0;
//           Right_moto_pwm1=0; 
	      }
}
       
/***************************************************/
///*TIMER0中断服务子函数产生PWM信号*/
 	void timer0()interrupt 1   using 2
{
     TH0=0XFc;	  //1Ms定时
	 TL0=0X18;
	 time++;
	 pwm_val_left++;
	 pwm_val_right++;
	 pwm_out_left_moto();
	 pwm_out_right_moto();
 }	

/*********************************************************************/	

#endif