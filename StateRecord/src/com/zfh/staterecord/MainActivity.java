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
	// 从资源文件中添Preferences ，选择的值将会自动保存到SharePreferences
	addPreferencesFromResource(R.xml.pref_general);    
	initView();
	startService1();
	getActionBar().setDisplayShowHomeEnabled(false);//去除标题栏图标
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
				Toast.makeText(this, "悬浮窗服务：开", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "悬浮窗服务：关", Toast.LENGTH_LONG).show();
			}
		}
		if(preference.getKey().equals("auto_run")){
			if((Boolean)newValue){
				Toast.makeText(this, "开机自启：开，会首先开启后台服务", Toast.LENGTH_LONG).show();
				Intent intentservice=new Intent(this, RecordFileService.class);
				startService(intentservice);
			}else{
				Toast.makeText(this, "开机自启：关，会首先关闭后台服务", Toast.LENGTH_LONG).show();
				Intent intentservice=new Intent(this, RecordFileService.class);
				stopService(intentservice);
			}
		}
		if(preference.getKey().equals("screen_keepon")){
			if((Boolean)newValue){
				Toast.makeText(this, "保持屏幕长亮：开", Toast.LENGTH_LONG).show();				
			}else{
				Toast.makeText(this, "保持屏幕长亮：关", Toast.LENGTH_LONG).show();				
			}
		}
		if(preference.getKey().equals("screen_ori")){
			if((Boolean)newValue){
				Toast.makeText(this, "保持屏幕方向：开：功能未实现", Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(this, "保持屏幕方向：关：功能未实现", Toast.LENGTH_LONG).show();
			}
		}
		Intent sendIntent=new Intent("preference is changed");
		this.sendBroadcast(sendIntent);			//设置已改变，发送广播通知其它组件
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
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		releaseWakeLock();
	}
    
}
