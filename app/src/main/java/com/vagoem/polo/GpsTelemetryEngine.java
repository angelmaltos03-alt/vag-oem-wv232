package com.vagoem.polo;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.GnssStatus;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;

/**
 * Local GPS/GNSS telemetry aggregator. Stores trip statistics on-device.
 * Values are derived only from Android Location/GNSS callbacks; no CAN data is inferred.
 */
public final class GpsTelemetryEngine {
 private static final String PREF="vag_gps_telemetry";
 private static final String TRIP="trip_";
 private static GnssStatus.Callback callback;
 private static int satellitesVisible=-1, satellitesUsed=-1;
 private static String constellationSummary="NO DISPONIBLE";
 private GpsTelemetryEngine(){}

 public static synchronized void start(Context context){
  try{
   LocationManager manager=(LocationManager)context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
   if(manager==null||callback!=null)return;
   callback=new GnssStatus.Callback(){
    @Override public void onSatelliteStatusChanged(GnssStatus status){
     int visible=status.getSatelliteCount(),used=0;
     boolean gps=false,glonass=false,galileo=false,beidou=false;
     for(int i=0;i<visible;i++){
      if(status.usedInFix(i))used++;
      switch(status.getConstellationType(i)){
       case GnssStatus.CONSTELLATION_GPS:gps=true;break;
       case GnssStatus.CONSTELLATION_GLONASS:glonass=true;break;
       case GnssStatus.CONSTELLATION_GALILEO:galileo=true;break;
       case GnssStatus.CONSTELLATION_BEIDOU:beidou=true;break;
      }
     }
     synchronized(GpsTelemetryEngine.class){
      satellitesVisible=visible;satellitesUsed=used;
      StringBuilder b=new StringBuilder();
      if(gps)b.append("GPS ");
      if(glonass)b.append("GLONASS ");
      if(galileo)b.append("GALILEO ");
      if(beidou)b.append("BEIDOU ");
      constellationSummary=b.length()==0?"SIN DATOS":b.toString().trim();
     }
    }
   };
   manager.registerGnssStatusCallback(callback,new Handler(Looper.getMainLooper()));
  }catch(Exception ignored){}
 }

 public static synchronized void stop(Context context){
  try{
   LocationManager manager=(LocationManager)context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
   if(manager!=null&&callback!=null)manager.unregisterGnssStatusCallback(callback);
  }catch(Exception ignored){}
  callback=null;
 }

 public static synchronized void record(Context context,Location location){
  if(location==null)return;
  SharedPreferences p=context.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);
  long previousTime=p.getLong("last_time",0L);
  double previousLat=Double.longBitsToDouble(p.getLong("last_lat",Double.doubleToLongBits(Double.NaN)));
  double previousLon=Double.longBitsToDouble(p.getLong("last_lon",Double.doubleToLongBits(Double.NaN)));
  float accuracy=location.hasAccuracy()?location.getAccuracy():Float.MAX_VALUE;
  boolean reliable=accuracy<=30f;
  double distance=p.getFloat(TRIP+"distance_m",0f);
  if(reliable&&previousTime>0&&location.getTime()>previousTime&&location.getTime()-previousTime<15000&&Double.isFinite(previousLat)&&Double.isFinite(previousLon)){
   float[] result=new float[1];
   Location.distanceBetween(previousLat,previousLon,location.getLatitude(),location.getLongitude(),result);
   if(result[0]>=2f&&result[0]<=250f)distance+=result[0];
  }
  float speed=location.hasSpeed()?location.getSpeed()*3.6f:Float.NaN;
  float max=p.getFloat(TRIP+"max_speed",0f);
  if(Float.isFinite(speed)&&speed>=0&&speed<250)max=Math.max(max,speed);
  long first=p.getLong(TRIP+"start_time",0L);
  if(first==0L)first=location.getTime();
  long duration=Math.max(0L,(location.getTime()-first)/1000L);
  float average=duration>0?(float)(distance/1000d/(duration/3600d)):0f;
  SharedPreferences.Editor e=p.edit().putFloat(TRIP+"distance_m",(float)distance)
   .putFloat(TRIP+"max_speed",max).putFloat(TRIP+"avg_speed",average)
   .putLong(TRIP+"start_time",first).putLong("last_time",location.getTime())
   .putLong("last_lat",Double.doubleToLongBits(location.getLatitude()))
   .putLong("last_lon",Double.doubleToLongBits(location.getLongitude()))
   .putFloat("last_accuracy",accuracy).putLong("last_fix_time",location.getTime());
  if(location.hasAltitude())e.putFloat("last_altitude",(float)location.getAltitude());
  if(Float.isFinite(speed))e.putFloat("last_speed",speed);
  if(location.hasBearing())e.putFloat("last_bearing",location.getBearing());
  e.apply();
 }

 public static synchronized Snapshot read(Context context,Location location){
  SharedPreferences p=context.getApplicationContext().getSharedPreferences(PREF,Context.MODE_PRIVATE);
  return new Snapshot(location,p.getFloat("last_altitude",Float.NaN),p.getFloat("last_accuracy",Float.NaN),
   p.getFloat("last_speed",Float.NaN),p.getFloat("last_bearing",Float.NaN),
   p.getFloat(TRIP+"distance_m",0f)/1000f,p.getFloat(TRIP+"max_speed",0f),
   p.getFloat(TRIP+"avg_speed",0f),p.getLong(TRIP+"start_time",0L),
   satellitesVisible,satellitesUsed,constellationSummary);
 }

 public static final class Snapshot{
  public final double latitude,longitude;
  public final float altitudeMeters,accuracyMeters,speedKmh,bearingDegrees;
  public final double tripKm;
  public final float maxSpeedKmh,averageSpeedKmh;
  public final long tripStartTime;
  public final int satellitesVisible,satellitesUsed;
  public final String constellations;
  Snapshot(Location l,float altitude,float accuracy,float speed,float bearing,double km,float max,float avg,long start,int visible,int used,String systems){
   latitude=l==null?Double.NaN:l.getLatitude();longitude=l==null?Double.NaN:l.getLongitude();
   altitudeMeters=altitude;accuracyMeters=accuracy;speedKmh=speed;bearingDegrees=bearing;
   tripKm=km;maxSpeedKmh=max;averageSpeedKmh=avg;tripStartTime=start;
   satellitesVisible=visible;satellitesUsed=used;constellations=systems;
  }
  public String summary(){
   return String.format(Locale.US,"GPS %.5f, %.5f · %.1f km · precisión %.0f m",latitude,longitude,tripKm,accuracyMeters);
  }
 }
}
