package com.zfh.staterecord.service;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.zfh.staterecord.MainActivity;
import com.zfh.staterecord.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.widget.Toast;

public class RecordFileService extends Service {
	private static final String TAG = "RecordFileService";
	private File desDir = new File(Environment.getExternalStorageDirectory().toString()+File.separator+"RecordFile");
	private File desFile = null;
	private PrintStream out = null;
	private int levelState = -100;
	private int status = -100;
	private WakeLock wakeLock = null;
	
	private BroadcastReceiver receiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {//收到电池状态变化广播				
				int health = intent.getIntExtra("health", 0);
				boolean present = intent.getBooleanExtra("present", false);
				int level = intent.getIntExtra("level", 0);
				int scale = intent.getIntExtra("scale", 0);
				int icon_small = intent.getIntExtra("icon-small", 0);
				int plugged = intent.getIntExtra("plugged", 0);
				int voltage = intent.getIntExtra("voltage", 0);
				int temperature = intent.getIntExtra("temperature", 0);
				String technology = intent.getStringExtra("technology");
				String statusString = "";
				switch (intent.getIntExtra("status", 0)) {
				case BatteryManager.BATTERY_STATUS_UNKNOWN:
					statusString = "unknown";
					break;
				case BatteryManager.BATTERY_STATUS_CHARGING:
					statusString = "充电中";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					statusString = "耗电中";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					statusString = "not charging";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					statusString = "电量已满";
					break;
				default:
					break;
				}
				String healthString = "";
				switch (health) {
				case BatteryManager.BATTERY_HEALTH_UNKNOWN:
					healthString = "unknown";
					break;
				case BatteryManager.BATTERY_HEALTH_GOOD:
					healthString = "good";
					break;
				case BatteryManager.BATTERY_HEALTH_OVERHEAT:
					healthString = "overheat";
					break;
				case BatteryManager.BATTERY_HEALTH_DEAD:
					healthString = "dead";
					break;
				case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
					healthString = "voltage";
					break;
				case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
					healthString = "unspecified failure";
					break;
				}
				String acString = "";
				switch (plugged) {
				case BatteryManager.BATTERY_PLUGGED_AC:
					acString = "AC充电器";
					break;
				case BatteryManager.BATTERY_PLUGGED_USB:
					acString = "USB";
					break;
				}
				Log.i("cat", statusString);
				Log.i("cat", healthString);
				Log.i("cat", String.valueOf(present));
				Log.i("cat", String.valueOf(level));
				Log.i("cat", String.valueOf(scale));
				Log.i("cat", String.valueOf(icon_small));
				Log.i("cat", acString);
				Log.i("cat", String.valueOf(voltage));
				Log.i("cat", String.valueOf(temperature));
				Log.i("cat", technology);
				// 电池电量，数字
				Log.d("Battery电量", "" + intent.getIntExtra("level", 0));
				// 电池最大容量
				Log.d("Battery容量", "" + intent.getIntExtra("scale", 0));
				// 电池伏数
				Log.d("Battery电压", "" + intent.getIntExtra("voltage", 0));
				// 电池温度
				Log.d("Battery温度", "" + intent.getIntExtra("temperature", 0));
				// 电池状态，返回是一个数字
				// BatteryManager.BATTERY_STATUS_CHARGING 表示是充电状态
				// BatteryManager.BATTERY_STATUS_DISCHARGING 放电中
				// BatteryManager.BATTERY_STATUS_NOT_CHARGING 未充电
				// BatteryManager.BATTERY_STATUS_FULL 电池满
				Log.d("Battery状态", "ss" + intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_CHARGING));
				// 充电类型 BatteryManager.BATTERY_PLUGGED_AC 表示是充电器，不是这个值，表示是 USB
				Log.d("Battery充电方式", "" + intent.getIntExtra("plugged", 0));
				// 电池健康情况，返回也是一个数字
				// BatteryManager.BATTERY_HEALTH_GOOD 良好
				// BatteryManager.BATTERY_HEALTH_OVERHEAT 过热
				// BatteryManager.BATTERY_HEALTH_DEAD 没电
				// BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE 过电压
				// BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE 未知错误
				Log.d("Battery健康状态", "" + intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN));
				if(level!=levelState||intent.getIntExtra("status", 0)!=status){	//电池电量有变化或充放电转换
					printf("\t"+getRecordDate()+"\t\t\t"+acString+statusString+"\t\t\t\t\t\t\t\t\t"+intent.getIntExtra("level", 0)+"%");
					levelState = level;				
					status = intent.getIntExtra("status", 0);
					Toast.makeText(context, "电池状态改变已记录", Toast.LENGTH_LONG).show();
				}
			}
			if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)){//关机广播
				Toast.makeText(context, "关机中。。。。。。", Toast.LENGTH_LONG).show();
				printf("\t"+getRecordDate()+"\t\t\t"+"-----------------------------关机------------------------------");
			}
			if(intent.getAction().equals("preference is changed")){
				screenKeep();
			}
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {		
		// TODO Auto-generated method stub
		super.onCreate();
		Toast.makeText(getBaseContext(), "服务已启动", Toast.LENGTH_LONG).show();
		
		Notification notification = new Notification(R.drawable.a,"启动服务发出通知", System.currentTimeMillis());
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent , 0);
		notification.setLatestEventInfo(this, "电池状态记录中", "此处通知用来提高优先级", contentIntent);
		startForeground(1339, notification);
		
		screenKeep();
		initview();//变量及各Viwe定义
		initRegister();	//广播注册
	}

	private void initview() {
		// TODO Auto-generated method stub
		desFile = new File(Environment.getExternalStorageDirectory().toString()+File.separator+"RecordFile"
				+File.separator+"battery_"+getFileDate()+".txt");
		printf("\t"+getRecordDate()+"\t\t\t"+"----------------------------记录开始------------------------");
	}

	private IntentFilter filter1;
	private IntentFilter filter2;
	private IntentFilter filter3;
	private void initRegister() {		
		// TODO Auto-generated method stub
		this.filter1 = new IntentFilter();
		filter1.addAction(Intent.ACTION_BATTERY_CHANGED);
		this.registerReceiver(this.receiver, this.filter1);
		this.filter2 = new IntentFilter();
		filter2.addAction(Intent.ACTION_SHUTDOWN);
		this.registerReceiver(this.receiver, this.filter2);
		this.filter3 = new IntentFilter();
		filter3.addAction("preference is changed");
		this.registerReceiver(this.receiver, this.filter3);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(receiver);
		stopForeground(true);
		releaseWakeLock();
		Toast.makeText(getBaseContext(), "服务被关闭，请确认!", Toast.LENGTH_LONG).show();
	}
	//获取电池变化时记录时间
	public String getRecordDate(){
		Date newTime = new Date();
		SimpleDateFormat time=new SimpleDateFormat("MM-dd HH:mm:ss");
		return time.format(newTime);
	}
	//以时间命名文件
	public String getFileDate(){
		Date newTime = new Date();
		SimpleDateFormat time=new SimpleDateFormat("MMddHHmm_ss");
		return time.format(newTime);
	}
	public void printf(String str){
		if(!desDir.exists()){
			desDir.mkdirs();
		}
		try {				
			out = new PrintStream(new FileOutputStream(desFile,true));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		out.println(str);
		out.close();
	}
	private void takeWakeLock()		//屏幕唤醒
    {
        	if (wakeLock == null){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);                         
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wakeLock");               
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }
    }
    private void releaseWakeLock(){	//释放屏幕
    	if (wakeLock != null)
        {
            wakeLock.release();
            wakeLock = null;
        }
    }
    private void screenKeep(){
    	if(MainActivity.getScreen(this)){
			takeWakeLock();
		}
		if(!MainActivity.getScreen(this)){
			releaseWakeLock();
		}
    }
}
