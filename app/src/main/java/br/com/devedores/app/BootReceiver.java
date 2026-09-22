package br.com.devedores.app;
import android.content.*;
public class BootReceiver extends BroadcastReceiver{public void onReceive(Context c,Intent i){if(Intent.ACTION_BOOT_COMPLETED.equals(i.getAction()))AlarmScheduler.rescheduleAll(c);}}
