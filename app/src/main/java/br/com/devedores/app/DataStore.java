package br.com.devedores.app;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class DataStore {
    private static final String P="devedores_db_v4";
    private final SharedPreferences sp;
    private final Context context;
    public List<Models.Client> clients=new ArrayList<>();
    public List<Models.Reminder> reminders=new ArrayList<>();
    public DataStore(Context c){context=c.getApplicationContext();sp=context.getSharedPreferences(P,Context.MODE_PRIVATE);load();}
    public void load(){clients.clear();reminders.clear();try{
        String clientRaw=sp.getString("clients","");String reminderRaw=sp.getString("reminders","");
        if(clientRaw==null||clientRaw.isEmpty()){
            SharedPreferences legacy=context.getSharedPreferences("devedores_db",Context.MODE_PRIVATE);
            clientRaw=legacy.getString("clients","[]"); reminderRaw=legacy.getString("reminders","[]");
        }
        JSONArray a=new JSONArray(clientRaw==null||clientRaw.isEmpty()?"[]":clientRaw);for(int i=0;i<a.length();i++)clients.add(Models.Client.fromJson(a.getJSONObject(i)));
        JSONArray r=new JSONArray(reminderRaw==null||reminderRaw.isEmpty()?"[]":reminderRaw);for(int i=0;i<r.length();i++)reminders.add(Models.Reminder.fromJson(r.getJSONObject(i)));
    }catch(Exception ignored){}}
    public void save(){try{JSONArray a=new JSONArray();for(Models.Client c:clients)a.put(c.toJson());JSONArray r=new JSONArray();for(Models.Reminder x:reminders)r.put(x.toJson());sp.edit().putString("clients",a.toString()).putString("reminders",r.toString()).commit();}catch(Exception ignored){}}
    public Models.Client client(String id){for(Models.Client c:clients)if(c.id.equals(id))return c;return null;}
    public Models.Loan loan(String id){for(Models.Client c:clients)for(Models.Loan l:c.loans)if(l.id.equals(id))return l;return null;}
    public void removeClient(String id){for(int i=clients.size()-1;i>=0;i--)if(clients.get(i).id.equals(id)){clients.remove(i);break;}save();}
    public String exportJson(){try{JSONObject all=new JSONObject();JSONArray a=new JSONArray();for(Models.Client c:clients)a.put(c.toJson());JSONArray r=new JSONArray();for(Models.Reminder x:reminders)r.put(x.toJson());all.put("version",4);all.put("clients",a);all.put("reminders",r);return all.toString(2);}catch(Exception e){return "{}";}}
    public boolean importJson(String s){try{JSONObject all=new JSONObject(s);JSONArray a=all.optJSONArray("clients");JSONArray r=all.optJSONArray("reminders");if(a==null)return false;clients.clear();reminders.clear();for(int i=0;i<a.length();i++)clients.add(Models.Client.fromJson(a.getJSONObject(i)));if(r!=null)for(int i=0;i<r.length();i++)reminders.add(Models.Reminder.fromJson(r.getJSONObject(i)));save();return true;}catch(Exception e){return false;}}
}
