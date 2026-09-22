package br.com.devedores.app;
import android.app.*;import android.content.*;import android.os.Build;import android.provider.Settings;import java.util.*;
public class AlarmScheduler {
 public static final String ACTION="br.com.devedores.app.ALARM";
 public static boolean canExact(Context c){return Build.VERSION.SDK_INT<31||((AlarmManager)c.getSystemService(Context.ALARM_SERVICE)).canScheduleExactAlarms();}
 public static void openExactSettings(Context c){if(Build.VERSION.SDK_INT>=31){try{c.startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:"+c.getPackageName())));}catch(Exception ignored){c.startActivity(new Intent(Settings.ACTION_SETTINGS));}}}
 static PendingIntent pi(Context c,String key,String type,String id,String title,int index){Intent i=new Intent(c,LoanAlarmReceiver.class).setAction(ACTION).putExtra("type",type).putExtra("id",id).putExtra("title",title).putExtra("index",index);return PendingIntent.getBroadcast(c,key.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 public static void schedule(Context c,String type,String id,String title,long when){schedule(c,type,id,title,when,-1);}
 public static void schedule(Context c,String type,String id,String title,long when,int index){if(when<=System.currentTimeMillis())return;AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);PendingIntent p=pi(c,type+":"+id+":"+index,type,id,title,index);if(Build.VERSION.SDK_INT>=31&&canExact(c))am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,p);else if(Build.VERSION.SDK_INT>=23)am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,p);else am.set(AlarmManager.RTC_WAKEUP,when,p);}
 public static void cancel(Context c,String type,String id,int index){AlarmManager am=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);PendingIntent p=pi(c,type+":"+id+":"+index,type,id,"",index);am.cancel(p);p.cancel();}
 public static void cancelLoanAlarms(Context c,String id,int count){for(int i=0;i<count;i++)cancel(c,"loan",id,i);}
 public static void scheduleAllInstallments(Context c,Models.Client cl,Models.Loan l){if(!canExact(c)&&Build.VERSION.SDK_INT>=31)return;for(int i=0;i<l.installments;i++)if(!l.installmentPaid(i)){schedule(c,"loan",l.id,cl.name+" • "+l.title+" • parcela "+(i+1),l.dueAt(i),i);}}
 public static void rescheduleAll(Context c){DataStore ds=new DataStore(c);if(Build.VERSION.SDK_INT>=31&&!canExact(c))return;for(Models.Client cl:ds.clients)for(Models.Loan l:cl.loans)scheduleAllInstallments(c,cl,l);for(Models.Reminder r:ds.reminders)if(!r.done)schedule(c,"reminder",r.id,r.title,r.when);}
}
