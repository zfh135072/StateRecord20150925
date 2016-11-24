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
			if (intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {//�յ����״̬�仯�㲥				
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
					statusString = "�����";
					break;
				case BatteryManager.BATTERY_STATUS_DISCHARGING:
					statusString = "�ĵ���";
					break;
				case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
					statusString = "not charging";
					break;
				case BatteryManager.BATTERY_STATUS_FULL:
					statusString = "��������";
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
					acString = "AC�����";
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
				// ��ص���������
				Log.d("Battery����", "" + intent.getIntExtra("level", 0));
				// ����������
				Log.d("Battery����", "" + intent.getIntExtra("scale", 0));
				// ��ط���
				Log.d("Battery��ѹ", "" + intent.getIntExtra("voltage", 0));
				// ����¶�
				Log.d("Battery�¶�", "" + intent.getIntExtra("temperature", 0));
				// ���״̬��������һ������
				// BatteryManager.BATTERY_STATUS_CHARGING ��ʾ�ǳ��״̬
				// BatteryManager.BATTERY_STATUS_DISCHARGING �ŵ���
				// BatteryManager.BATTERY_STATUS_NOT_CHARGING δ���
				// BatteryManager.BATTERY_STATUS_FULL �����
				Log.d("Battery״̬", "ss" + intent.getIntExtra("status", BatteryManager.BATTERY_STATUS_CHARGING));
				// ������� BatteryManager.BATTERY_PLUGGED_AC ��ʾ�ǳ�������������ֵ����ʾ�� USB
				Log.d("Battery��緽ʽ", "" + intent.getIntExtra("plugged", 0));
				// ��ؽ������������Ҳ��һ������
				// BatteryManager.BATTERY_HEALTH_GOOD ����
				// BatteryManager.BATTERY_HEALTH_OVERHEAT ����
				// BatteryManager.BATTERY_HEALTH_DEAD û��
				// BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE ����ѹ
				// BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE δ֪����
				Log.d("Battery����״̬", "" + intent.getIntExtra("health", BatteryManager.BATTERY_HEALTH_UNKNOWN));
				if(level!=levelState||intent.getIntExtra("status", 0)!=status){	//��ص����б仯���ŵ�ת��
					printf("\t"+getRecordDate()+"\t\t\t"+acString+statusString+"\t\t\t\t\t\t\t\t\t"+intent.getIntExtra("level", 0)+"%");
					levelState = level;				
					status = intent.getIntExtra("status", 0);
					Toast.makeText(context, "���״̬�ı��Ѽ�¼", Toast.LENGTH_LONG).show();
				}
			}
			if(intent.getAction().equals(Intent.ACTION_SHUTDOWN)){//�ػ��㲥
				Toast.makeText(context, "�ػ��С�����������", Toast.LENGTH_LONG).show();
				printf("\t"+getRecordDate()+"\t\t\t"+"-----------------------------�ػ�------------------------------");
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
		Toast.makeText(getBaseContext(), "����������", Toast.LENGTH_LONG).show();
		
		Notification notification = new Notification(R.drawable.a,"�������񷢳�֪ͨ", System.currentTimeMillis());
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,0,intent , 0);
		notification.setLatestEventInfo(this, "���״̬��¼��", "�˴�֪ͨ����������ȼ�", contentIntent);
		startForeground(1339, notification);
		
		screenKeep();
		initview();//��������Viwe����
		initRegister();	//�㲥ע��
	}

	private void initview() {
		// TODO Auto-generated method stub
		desFile = new File(Environment.getExternalStorageDirectory().toString()+File.separator+"RecordFile"
				+File.separator+"battery_"+getFileDate()+".txt");
		printf("\t"+getRecordDate()+"\t\t\t"+"----------------------------��¼��ʼ------------------------");
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
		Toast.makeText(getBaseContext(), "���񱻹رգ���ȷ��!", Toast.LENGTH_LONG).show();
	}
	//��ȡ��ر仯ʱ��¼ʱ��
	public String getRecordDate(){
		Date newTime = new Date();
		SimpleDateFormat time=new SimpleDateFormat("MM-dd HH:mm:ss");
		return time.format(newTime);
	}
	//��ʱ�������ļ�
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
	private void takeWakeLock()		//��Ļ����
    {
        	if (wakeLock == null){
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);                         
                wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "wakeLock");               
                wakeLock.setReferenceCounted(false);
                wakeLock.acquire();
            }
    }
    private void releaseWakeLock(){	//�ͷ���Ļ
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
