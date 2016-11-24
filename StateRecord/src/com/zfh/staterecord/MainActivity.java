package com.zfh.staterecord;
import com.zfh.staterecord.service.RecordFileService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;


public class MainActivity extends PreferenceActivity implements OnPreferenceChangeListener {
	private static final String TAG = "MainActivity";
	CheckBoxPreference oService,autorun,skeepon,sori;
	private WakeLock wakeLock = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// ����Դ�ļ�����Preferences ��ѡ���ֵ�����Զ����浽SharePreferences
	addPreferencesFromResource(R.xml.pref_general);    
	initView();
	startService1();
	getActionBar().setDisplayShowHomeEnabled(false);//ȥ��������ͼ��
    }
    private void initView() {
    	if(getScreen(this)){
    		takeWakeLock();
    	}
		// TODO Auto-generated method stub
    	oService = (CheckBoxPreference)findPreference("open_service");
    	oService.setOnPreferenceChangeListener(this);
    	autorun = (CheckBoxPreference)findPreference("auto_run");
    	autorun.setOnPreferenceChangeListener(this);
    	skeepon = (CheckBoxPreference)findPreference("screen_keepon");
    	skeepon.setOnPreferenceChangeListener(this);
    	sori = (CheckBoxPreference)findPreference("screen_ori");
    	sori.setOnPreferenceChangeListener(this);
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		// TODO Auto-generated method stub
		if(preference.getKey().equals("open_service")){
			if((Boolean)newValue){
				Toast.makeText(this, "���������񣺿�", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "���������񣺹�", Toast.LENGTH_LONG).show();
			}
		}
		if(preference.getKey().equals("auto_run")){
			if((Boolean)newValue){
				Toast.makeText(this, "�������������������ȿ�����̨����", Toast.LENGTH_LONG).show();
				Intent intentservice=new Intent(this, RecordFileService.class);
				startService(intentservice);
			}else{
				Toast.makeText(this, "�����������أ������ȹرպ�̨����", Toast.LENGTH_LONG).show();
				Intent intentservice=new Intent(this, RecordFileService.class);
				stopService(intentservice);
			}
		}
		if(preference.getKey().equals("screen_keepon")){
			if((Boolean)newValue){
				Toast.makeText(this, "������Ļ��������", Toast.LENGTH_LONG).show();				
			}else{
				Toast.makeText(this, "������Ļ��������", Toast.LENGTH_LONG).show();				
			}
		}
		if(preference.getKey().equals("screen_ori")){
			if((Boolean)newValue){
				Toast.makeText(this, "������Ļ���򣺿�������δʵ��", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "������Ļ���򣺹أ�����δʵ��", Toast.LENGTH_LONG).show();
			}
		}
		Intent sendIntent=new Intent("preference is changed");
		this.sendBroadcast(sendIntent);			//�����Ѹı䣬���͹㲥֪ͨ�������
		return true;
	}
	public static boolean getService(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("open_service", true);
	}
	public static boolean getAutorun(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_run", true);
	}
	public static boolean getScreen(Context context){
		return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("screen_keepon", true);
	}
	private void startService1() {
		Intent intent=new Intent(MainActivity.this, RecordFileService.class);
		startService(intent);
	}

	private void stopServices1() {
		Intent intent=new Intent(MainActivity.this, RecordFileService.class);
		stopService(intent);
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
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		releaseWakeLock();
	}
    
}
