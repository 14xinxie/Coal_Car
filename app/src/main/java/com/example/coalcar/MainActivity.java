package com.example.coalcar;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends Activity {

	private GestureDetector gestureDetector;

	private final static int REQUEST_CONNECT_DEVICE = 1;    //宏定义查询设备句柄

	private final static String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";   //SPP服务UUID号

	private InputStream is;    //输入流，用来接收蓝牙数据

	private BluetoothDevice device = null;     //蓝牙设备
	private BluetoothSocket socket = null;      //蓝牙通信socket

	private ActionBar actionBar;
	private SharedPreferences sp;
	private Editor editor;
	private String ifSwitch ;

	private Button btn_connect;

	private Button btn_stop;
	private BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();    //获取本地蓝牙适配器，即蓝牙设备


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);   //设置画面为主画面 main.xml


		btn_connect=(Button)findViewById(R.id.connect_button);

		btn_stop=(Button)findViewById(R.id.stop_button);

		actionBar = getActionBar();
		sp = getSharedPreferences("config", 0);
		editor = sp.edit();


		ifSwitch=sp.getString("switch",null);
		if(TextUtils.isEmpty(ifSwitch))
		{
			editor.putString("switch", "on");
			editor.apply();
		}


		gestureDetector = new GestureDetector(getApplicationContext(), new OnGestureListener()
		{

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {
			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
									float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {
			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
								   float velocityY) {
				if(Math.abs(velocityY)<200){
					return false;
				}
				//向下滑动
				if(Math.abs(e2.getRawX() - e1.getRawX())<400&&(e2.getRawY() - e1.getRawY())>40){
					actionBar.show();

				}
				//向上滑动
				if(Math.abs(e2.getRawX() - e1.getRawX())<400&&(e1.getRawY() - e2.getRawY())>40){
					actionBar.hide();

				}
				return true;
			}

			@Override
			public boolean onDown(MotionEvent e) {

				return false;
			}
		});

		//如果打开本地蓝牙设备不成功，提示信息，结束程序
		if (bluetooth == null){
			Toast.makeText(this, "无法打开手机蓝牙，请确认手机是否有蓝牙功能！", Toast.LENGTH_LONG).show();
			finish();
			return;
		}

		// 设置设备可以被搜索
		new Thread(){
			public void run()
			{
				if(bluetooth.isEnabled()==false){
					bluetooth.enable();
				}
			}
		}.start();
	}

	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}



	/**
	 * 暂停按钮点击事件
	 * @param view
	 */

	public void stop(View view){

		if(sp.getBoolean("isConnected", false)==true)
		{
			if(sp.getString("switch", null).equals("on"))
			{
				sendCommand("7");
				editor.putString("switch", "off");
				editor.apply();
				btn_stop.setBackgroundResource(R.drawable.stop);
			}
			else
			{
				sendCommand("8");
				editor.putString("switch", "on");
				editor.apply();
				btn_stop.setBackgroundResource(R.drawable.start);
			}
		}else
		{

			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 左转按钮的点击事件
	 */
	public void turn_left(View v)
	{
		if(sp.getBoolean("isConnected", false)==true)
			sendCommand("1");
		else
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
	}


	/**
	 * 右转按钮的点击事件
	 */
	public void turn_right(View v)
	{
		if(sp.getBoolean("isConnected", false)==true)
			sendCommand("2");
		else
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 向前按钮的点击事件
	 * @param v
	 */
	public void forward(View v)
	{
		if(sp.getBoolean("isConnected", false)==true)
			sendCommand("3");
		else
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
	}


	/**
	 * 向后按钮的点击事件
	 */

	public void backward(View v)
	{
		if(sp.getBoolean("isConnected", false)==true)
			sendCommand("4");
		else
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
	}


	/**
	 * 加速按钮的点击事件
	 */
	public void speed_up(View v)
	{
		if(sp.getBoolean("isConnected", false)==true)
			sendCommand("5");
		else
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
	}

	/**
	 * 减速按钮的点击事件
	 */

	public void slow_down(View v)
	{
		if(sp.getBoolean("isConnected", false)==true)
			sendCommand("6");
		else
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
	}


	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options, menu);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

			case R.id.quit:
				editor.putBoolean("isConnected", false);
				editor.apply();
				finish();
				break;
			case R.id.about:
				Intent intent = new Intent(MainActivity.this,AboutActivity.class);
				startActivity(intent);
				break;

			default:
				break;
		}

		return super.onOptionsItemSelected(item);
	}


	/**
	 * 向单片机蓝牙模块发送数据
	 * @param command
     */
	public void sendCommand(String command){
		int i ;
		int n = 0;
		if(socket!=null) {
			try {
				OutputStream os = socket.getOutputStream();
				byte[] bos = command.getBytes();
				for(i=0;i<bos.length;i++){
					if(bos[i]==0x0a)n++;
				}
				byte[] bos_new = new byte[bos.length+n];
				n=0;
				for(i=0;i<bos.length;i++){ //手机中换行为0a,将其改为0d0a后再发送
					if(bos[i]==0x0a){
						bos_new[n]=0x0d;
						n++;
						bos_new[n]=0x0a;
					}else{
						bos_new[n]=bos[i];
					}
					n++;
				}
				os.write(bos_new);
				Toast.makeText(getApplicationContext(), "发送数据"+command+"成功！", Toast.LENGTH_SHORT).show();
			} catch (IOException e) {

				Toast.makeText(getApplicationContext(), "发送数据"+command+"失败！", Toast.LENGTH_SHORT).show();
				e.printStackTrace();
			}
		}
		else {
			Toast.makeText(getApplicationContext(), "请与采矿车连接", Toast.LENGTH_SHORT).show();
		}
	}

	//接收活动结果，响应startActivityForResult()
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch(requestCode){
			case REQUEST_CONNECT_DEVICE:     //连接结果，由DeviceListActivity设置返回
				// 响应返回结果
				if (resultCode == Activity.RESULT_OK) {   //连接成功，由DeviceListActivity设置返回
					// MAC地址，由DeviceListActivity设置返回
					String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
					// 得到蓝牙设备句柄
					device = bluetooth.getRemoteDevice(address);

					// 用服务号得到socket
					try{
						socket = device.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
					}catch(IOException e){
						Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
					}
					//连接socket

					try{
						socket.connect();
						Toast.makeText(this, "连接"+device.getName()+"成功！", Toast.LENGTH_SHORT).show();
						editor.putBoolean("isConnected", true);
						editor.apply();
						btn_connect.setBackgroundResource(R.drawable.switch_off);
					}catch(IOException e){
						try{
							Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
							socket.close();
						}catch(IOException ee){
							Toast.makeText(this, "连接失败！", Toast.LENGTH_SHORT).show();
						}
						return;
					}

					//打开接收线程
					try{
						is = socket.getInputStream();   //得到蓝牙数据输入流
					}catch(IOException e){
						Toast.makeText(this, "接收数据失败！", Toast.LENGTH_SHORT).show();
						return;
					}
				}
				break;
			default:break;
		}
	}


	//关闭程序掉用处理部分
	public void onDestroy(){
		super.onDestroy();
		if(socket!=null) {//关闭连接socket

			try{
				socket.close();
				btn_stop.setBackgroundResource(R.drawable.start);
				btn_connect.setBackgroundResource(R.drawable.switch_on);
			}catch(IOException e){
				e.printStackTrace();
			}
		}


		bluetooth.disable();  //关闭蓝牙服务

	}



	//连接按键响应函数
	public void onConnectButtonClicked(View v){
		if(bluetooth.isEnabled()==false){  //如果蓝牙服务不可用则提示
			Toast.makeText(getApplicationContext()
					, " 打开蓝牙中...", Toast.LENGTH_LONG).show();
			return;
		}


		//如未连接设备则打开DeviceListActivity进行设备搜索
		if(socket==null){
			Intent serverIntent = new Intent(this, DeviceListActivity.class); //跳转程序设置
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);  //设置返回宏定义
		}
		else{

			//关闭连接socket
			try{

				is.close();
				socket.close();
				btn_connect.setBackgroundResource(R.drawable.switch_on);
				btn_stop.setBackgroundResource(R.drawable.start);
				editor.putBoolean("isConnected", false);
				editor.apply();

			}catch(IOException e){
				e.printStackTrace();
			}

		}
		return;
	}



}