package com.zfh.staterecord;
import com.zfh.staterecord.service.RecordFileService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BootReceiver extends BroadcastReceiver {
    @Override  
    public void onReceive(Context context, Intent intent) {  
        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            /*Intent intent2 = new Intent(context, MainActivity.class);  
            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
            context.startActivity(intent2);*/
            Toast.makeText(context, "系统开机已完成", Toast.LENGTH_LONG).show();
            if(MainActivity.getAutorun(context)){//开机判断配置文件
            	Intent intentservice=new Intent(context, RecordFileService.class);
//              intentservice.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            	context.startService(intentservice);
            }       	
        }  
    }
}  