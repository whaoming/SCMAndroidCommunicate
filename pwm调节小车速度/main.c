#include<AT89X52.H>		      //包含51单片机头文件，内部有各种寄存器定义
#include<HJ-4WD_PWM.H>		  //包含HL-1蓝牙智能小车驱动IO口定义等函数
#include <stdlib.h>
#include   <STDIO.H>
    		 unsigned int k;
		
			 typedef unsigned int  uint16;
			   sbit k1=P3^4;
			   sbit k2=P3^5;
			   char str[5] =  {0xff,0x01,0x02,0x03,0xff};
char buffer [4];
int b;
int a=0;
void delay_1ms(uint16 t)
{
	uint16 x,y;
	for(x=t;x>0;x--)
		for(y=120;y>0;y--);
}
void sendMing(char me[])
{
	unsigned char i = 0;
	while(i<=4)
	{
		SBUF = me[i];
		while(!TI);				// 等特数据传送
		TI = 0;					// 清除数据传送标志
		i++;
	}
}

void  demo()
{
 		sprintf(buffer, "%x", k*2);
		b = strtol( buffer, NULL, 16 );
		str[3]	 = (b & 0xff)>>0;//取低8位
		str[2] = (b & 0xff00)>>8;
		// temp = PWM_T;
		sendMing(str);
		
}

//主函数
	void main(void)
{	
	
	unsigned char i;
	k = 15;

		 	TMOD=0X21;
        	TH0= 0XFc;		  //1ms定时
         	TL0= 0X18;
           	TR0= 1;
        	ET0= 1;
	       
		   SCON=0x50;
		TH1=0xFD;	   		//设置定时器1初始值
		TL1=0xFD;		   //设置定时器1初始值
		TR1=1;	
		    EA = 1;		     //开总中断

	 	 while(1)
		 {
		 	a++;
		 	 run(k);
		 	if(!k1)
			{
				if(k<50){
					k++;
					//P1_6=0;
				}
				delay_1ms(40);	
			}
			if(!k2)
			{
				if(k>0){
					k--;
				}	
				delay_1ms(40);
			}
			if(a==20000)
			{
				demo();
				a=0;
			}
		 
		 }
}