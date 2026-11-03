package com.vagoem.polo;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class LocalAlertStore {
 public static final class Alert {
  public String id,type,note; public double lat,lon; public long created;
  Alert(String i,String t,String n,double a,double o,long c){id=i;type=t;note=n;lat=a;lon=o;created=c;}
 }
 private static final String PREF="vag_local_road_alerts",KEY="items";
 private LocalAlertStore(){}
 public static synchronized List<Alert> all(Context c){
  List<Alert> out=new ArrayList<>();
  try{JSONArray a=new JSONArray(c.getSharedPreferences(PREF,0).getString(KEY,"[]"));for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);out.add(new Alert(o.optString("id"),o.optString("type"),o.optString("note"),o.optDouble("lat"),o.optDouble("lon"),o.optLong("created")));}}catch(Exception ignored){}
  return out;
 }
 public static synchronized void add(Context c,String type,String note,double lat,double lon){
  List<Alert> items=all(c);JSONArray a=new JSONArray();try{
   JSONObject o=new JSONObject();o.put("id",Long.toString(System.currentTimeMillis()));o.put("type",type);o.put("note",note==null?"":note);o.put("lat",lat);o.put("lon",lon);o.put("created",System.currentTimeMillis());a.put(o);
   for(Alert x:items){JSONObject j=new JSONObject();j.put("id",x.id);j.put("type",x.type);j.put("note",x.note);j.put("lat",x.lat);j.put("lon",x.lon);j.put("created",x.created);a.put(j);}
   c.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).apply();
  }catch(Exception ignored){}
 }
}