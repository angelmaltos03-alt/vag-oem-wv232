package com.vagoem.polo;
import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Toast;
import java.util.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class MainActivity extends Activity {
 private static final String PREFS="vag_oem_targets";
 private static final int REQ_VEHICLE_GPS=712;
 private SharedPreferences prefs; private VagUiView ui;
 private final Deque<String> screenHistory=new ArrayDeque<>();
 private LocationManager vehicleLocationManager;
 private LocationListener vehicleLocationListener;
 @Override public void onCreate(Bundle state){super.onCreate(state);prefs=getSharedPreferences(PREFS,MODE_PRIVATE);hideSystemBars();ui=new VagUiView(this,this::handleAction);setContentView(ui);showPendingCrashReport();}
 @Override protected void onResume(){super.onResume();hideSystemBars();refreshGpsUi();updateTelemetryForScreen();}
 @Override protected void onWindowFocusChanged(boolean hasFocus){super.onWindowFocusChanged(hasFocus);if(hasFocus)hideSystemBars();}
 @Override protected void onPause(){stopVehicleTelemetry();super.onPause();}

 private void showScreen(String target){
  if(ui==null)return;
  String current=ui.getScreen();
  if(!target.equals(current))screenHistory.push(current);
  ui.setScreen(target);updateTelemetryForScreen();
 }
 private void goBack(){
  if(ui==null)return;
  String previous=screenHistory.isEmpty()?"HOME":screenHistory.pop();
  ui.setScreen(previous);updateTelemetryForScreen();
 }
 private void updateTelemetryForScreen(){
  if(ui==null)return;
  if("VEHICLE".equals(ui.getScreen())||"TRIP".equals(ui.getScreen()))startVehicleTelemetry();
  else stopVehicleTelemetry();
 }
 private void startVehicleTelemetry(){
  boolean fine=checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED;
  boolean coarse=checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED;
  if(!fine&&!coarse){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_VEHICLE_GPS);return;}
  try{
   GpsTelemetryEngine.start(this);
   vehicleLocationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
   if(vehicleLocationManager==null)return;
   if(vehicleLocationListener==null)vehicleLocationListener=new LocationListener(){
    @Override public void onLocationChanged(Location location){GpsTelemetryEngine.record(MainActivity.this,location);refreshGpsUi();}
    @Override public void onStatusChanged(String provider,int status,Bundle extras){}
    @Override public void onProviderEnabled(String provider){}
    @Override public void onProviderDisabled(String provider){}
   };
   Location last=null;
   if(fine&&vehicleLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
    last=vehicleLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    vehicleLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,2000L,3f,vehicleLocationListener);
   }
   if(vehicleLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
    Location network=vehicleLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    if(network!=null&&(last==null||network.getTime()>last.getTime()))last=network;
    vehicleLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,5000L,10f,vehicleLocationListener);
   }
   if(last!=null){GpsTelemetryEngine.record(this,last);refreshGpsUi();}
  }catch(SecurityException e){toast("Permiso de ubicación no disponible para los datos GPS.");}
  catch(Exception e){toast("No se pudo iniciar la telemetría GPS: "+e.getMessage());}
 }
 private void stopVehicleTelemetry(){
  try{if(vehicleLocationManager!=null&&vehicleLocationListener!=null)vehicleLocationManager.removeUpdates(vehicleLocationListener);}catch(Exception ignored){}
  vehicleLocationManager=null;
  GpsTelemetryEngine.stop(this);
 }
 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
  super.onRequestPermissionsResult(requestCode,permissions,grantResults);
  if(requestCode==REQ_VEHICLE_GPS){
   boolean granted=false;for(int result:grantResults)if(result==PackageManager.PERMISSION_GRANTED)granted=true;
   if(granted)startVehicleTelemetry();else toast("Sin permiso GPS solo se mostrarán los datos que ya estén guardados.");
  }
 }

 private void showPendingCrashReport(){
  if(prefs.getBoolean("crash_report_prompted",false))return;
  if(!(getApplication() instanceof VagOemApp))return;
  File report=((VagOemApp)getApplication()).getLastCrashReport();
  if(report==null)return;
  prefs.edit().putBoolean("crash_report_prompted",true).apply();
  new AlertDialog.Builder(this).setTitle("Diagnóstico de VAG OEM")
   .setMessage("Se detectó un cierre inesperado en una ejecución anterior. Puedes compartir el informe para identificar el problema. No contiene contraseñas ni datos CANBUS.")
   .setPositiveButton("Compartir informe",(dialog,which)->shareCrashReport(report))
   .setNegativeButton("Ahora no",null).show();
 }
 private void shareCrashReport(File report){
  try{
   ByteArrayOutputStream buffer=new ByteArrayOutputStream();
   try(FileInputStream input=new FileInputStream(report)){
    byte[] data=new byte[4096];int count;
    while((count=input.read(data))!=-1)buffer.write(data,0,count);
   }
   Intent send=new Intent(Intent.ACTION_SEND);
   send.setType("text/plain");
   send.putExtra(Intent.EXTRA_SUBJECT,"VAG OEM WV232 - informe de diagnóstico");
   send.putExtra(Intent.EXTRA_TEXT,buffer.toString("UTF-8"));
   startActivity(Intent.createChooser(send,"Enviar informe de diagnóstico"));
  }catch(Exception error){
   Toast.makeText(this,"No se pudo abrir el informe: "+error.getMessage(),Toast.LENGTH_LONG).show();
  }
 }

 private void refreshGpsUi(){if(ui!=null){ui.setGpsSnapshot(GpsTelemetryEngine.read(this,null));}}
 private void showGpsDiagnostics(){
  GpsTelemetryEngine.Snapshot g=GpsTelemetryEngine.read(this,null);
  String coord=Double.isFinite(g.latitude)?String.format(Locale.US,"%.6f, %.6f",g.latitude,g.longitude):"Sin posición guardada";
  String age=g.fixTime>0?new java.text.SimpleDateFormat("dd/MM HH:mm:ss",Locale.getDefault()).format(new Date(g.fixTime)):"Sin fix GPS";
  String altitude=Float.isFinite(g.altitudeMeters)?String.format(Locale.US,"%.0f m",g.altitudeMeters):"No disponible";
  String accuracy=Float.isFinite(g.accuracyMeters)?String.format(Locale.US,"± %.0f m",g.accuracyMeters):"No disponible";
  String sats=g.satellitesVisible<0?"No reportado":g.satellitesUsed+" usados / "+g.satellitesVisible+" visibles";
  String message="POSICIÓN\n"+coord+"\n\nÚLTIMO FIX\n"+age+"\n\nPRECISIÓN\n"+accuracy+"\n\nALTITUD\n"+altitude+"\n\nSATÉLITES\n"+sats+"\n\nCONSTELACIONES\n"+g.constellations+"\n\nDISTANCIA DEL VIAJE\n"+String.format(Locale.US,"%.2f km",g.tripKm)+"\n\nFuente: Android Location / GNSS. No son datos CANBUS.";
  new AlertDialog.Builder(this).setTitle("Diagnóstico GPS / GNSS").setMessage(message).setPositiveButton("Cerrar",null).show();
 }
 private void hideSystemBars(){
  getWindow().setStatusBarColor(0xFF080C13);getWindow().setNavigationBarColor(0xFF080C13);
  int flags=View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
  getWindow().getDecorView().setSystemUiVisibility(flags);
 }
 private void handleAction(String a){switch(a){
  case "HOME":screenHistory.clear();ui.setScreen("HOME");updateTelemetryForScreen();break;
  case "BACK":goBack();break;
  case "RADIO":showScreen("RADIO");break;
  case "MEDIA":showScreen("MEDIA");break;
  case "PHONE":showScreen("PHONE");break;
  case "NAVIGATION":openDefaultNavigationApp();break;
  case "OPEN_NAV":startActivity(new Intent(this,NavigationActivity.class));break;
  case "VEHICLE":showScreen("VEHICLE");break;
  case "TRIP":showScreen("TRIP");break;
  case "SETTINGS":showScreen("SETTINGS");break;
  case "APP_CONNECT":case "OPEN_CARPLAY":launchCPLink();break;
  case "OPEN_RADIO":launchTarget("RADIO","Radio");break;
  case "OPEN_MEDIA":launchTarget("MUSIC","Media");break;
  case "OPEN_PHONE":openDialer();break;
  case "OPEN_CALL_LOG":openCallLog();break;
  case "OPEN_CONTACTS":case "OPEN_FAVORITES":openContacts();break;
  case "OPEN_CANBUS":launchTarget("CANBUS","CANBUS");break;
  case "RESET_GPS_TRIP":new AlertDialog.Builder(this).setTitle("Reiniciar viaje GPS").setMessage("Se reiniciarán distancia, tiempo y velocidades promedio/máxima del viaje guardado.").setNegativeButton("Cancelar",null).setPositiveButton("Reiniciar",(d,w)->{GpsTelemetryEngine.resetTrip(this);refreshGpsUi();toast("Viaje GPS reiniciado.");}).show();break;
  case "OPEN_GPS_DIAGNOSTICS":showGpsDiagnostics();break;
  case "SOUND":case "OPEN_SOUND":if(!launchSavedTarget("DSP","DSP / sonido"))startActivity(new Intent(this,SoundStudioActivity.class));break;
  case "ANDROID_SETTINGS":if(!launchSavedTarget("OEM_SETTINGS","Ajustes del fabricante"))openSettings(Settings.ACTION_SETTINGS);break;
  case "PICK_RADIO":pick("RADIO","Selecciona la aplicación de radio");break;
  case "PICK_MUSIC":pick("MUSIC","Selecciona el reproductor de música");break;
  case "PICK_PHONE":pick("PHONE","Selecciona Teléfono / Bluetooth");break;
  case "PICK_CARPLAY":pick("CARPLAY","Selecciona CPLink / App-Connect");break;
  case "PICK_NAV":pick("NAV","Selecciona tu navegador offline");break;
  case "PICK_CANBUS":pick("CANBUS","Selecciona la aplicación CANBUS");break;
  case "PICK_SETTINGS":pick("OEM_SETTINGS","Selecciona ajustes del fabricante");break;
  case "PICK_DSP":pick("DSP","Selecciona tu aplicación DSP / sonido");break;
  case "RESET_LINKS":new AlertDialog.Builder(this).setTitle("Restablecer vínculos").setMessage("Se borrarán las aplicaciones vinculadas, pero no se desinstalarán.").setNegativeButton("Cancelar",null).setPositiveButton("Restablecer",(d,w)->{prefs.edit().clear().apply();ui.invalidate();}).show();break;
 }}
 private void openDialer(){
  String saved=prefs.getString("PHONE",null);if(saved!=null&&launch(saved,"Teléfono"))return;
  try{startActivity(new Intent(Intent.ACTION_DIAL));}catch(Exception e){toast("No hay aplicación de teléfono disponible.");}
 }
 private void openCallLog(){
  try{startActivity(new Intent(Intent.ACTION_VIEW,CallLog.Calls.CONTENT_URI));}
  catch(Exception e){openDialer();}
 }
 private void openContacts(){
  try{startActivity(new Intent(Intent.ACTION_VIEW,ContactsContract.Contacts.CONTENT_URI));}
  catch(Exception e){toast("No hay aplicación de contactos disponible.");}
 }
 private void openSettings(String a){try{startActivity(new Intent(a));}catch(Exception e){toast("No se pudieron abrir los ajustes.");}}
 private void openDefaultNavigationApp(){
  if(launchSavedTarget("NAV","Map Live / navegación original"))return;
  List<ResolveInfo> all=launchableApps();
  List<ResolveInfo> exact=new ArrayList<>();
  List<ResolveInfo> candidates=new ArrayList<>();
  for(ResolveInfo item:all){
   String label=String.valueOf(item.loadLabel(getPackageManager())).toLowerCase(Locale.ROOT);
   String pkg=item.activityInfo.packageName.toLowerCase(Locale.ROOT);
   String combined=(label+" "+pkg).replace("_"," ").replace("-"," ");
   if(combined.contains("map live")||combined.contains("maplive"))exact.add(item);
   else if(combined.contains("navigation")||combined.contains("navi")||combined.contains("maps")||combined.contains("map "))candidates.add(item);
  }
  if(exact.size()==1){String pkg=exact.get(0).activityInfo.packageName;prefs.edit().putString("NAV",pkg).apply();launch(pkg,"Map Live");return;}
  if(!exact.isEmpty()){pickFromList("NAV","Selecciona Map Live",exact);return;}
  if(!candidates.isEmpty()){pickFromList("NAV","Selecciona el navegador original (Map Live)",candidates);return;}
  new AlertDialog.Builder(this).setTitle("Vincular Map Live")
   .setMessage("No se detectó una app con nombre Map Live. Selecciónala en la lista de aplicaciones instaladas para abrirla desde el botón Navegación.")
   .setPositiveButton("Elegir aplicación",(d,w)->pick("NAV","Selecciona Map Live / Navegación"))
   .setNegativeButton("Cancelar",null).show();
 }
 private void launchCPLink(){String saved=prefs.getString("CARPLAY",null);if(saved!=null&&launch(saved,"CPLink"))return;List<ResolveInfo> matches=new ArrayList<>();for(ResolveInfo r:launchableApps()){String label=String.valueOf(r.loadLabel(getPackageManager())).toLowerCase();String pkg=r.activityInfo.packageName.toLowerCase();if(label.contains("cplink")||label.contains("cp link")||pkg.contains("cplink"))matches.add(r);}if(matches.size()==1){String pkg=matches.get(0).activityInfo.packageName;prefs.edit().putString("CARPLAY",pkg).apply();launch(pkg,"CPLink");}else pick("CARPLAY","Selecciona la aplicación CPLink");}
 private boolean launchSavedTarget(String k,String l){String pkg=prefs.getString(k,null);if(pkg==null)return false;if(launch(pkg,l))return true;prefs.edit().remove(k).apply();toast("La app vinculada ya no está disponible. Se restableció el acceso.");return false;}
 private void launchTarget(String k,String l){String pkg=prefs.getString(k,null);if(pkg==null){toast("Vincula "+l+" desde Ajustes.");showScreen("SETTINGS");}else launch(pkg,l);}
 private boolean launch(String pkg,String label){try{Intent i=getPackageManager().getLaunchIntentForPackage(pkg);if(i==null)return false;i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i);return true;}catch(Exception e){toast("No se pudo abrir "+label);return false;}}
 private void pick(String key,String title){List<ResolveInfo> apps=launchableApps();if(apps.isEmpty()){toast("No hay aplicaciones disponibles.");return;}String[] labels=new String[apps.size()];for(int n=0;n<apps.size();n++){ResolveInfo r=apps.get(n);labels[n]=String.valueOf(r.loadLabel(getPackageManager()))+"\n"+r.activityInfo.packageName;}new AlertDialog.Builder(this).setTitle(title).setItems(labels,(d,w)->{String pkg=apps.get(w).activityInfo.packageName;prefs.edit().putString(key,pkg).apply();if("CARPLAY".equals(key))launch(pkg,"CPLink");toast("Aplicación vinculada.");}).setNegativeButton("Cancelar",null).show();}
 private List<ResolveInfo> launchableApps(){Intent i=new Intent(Intent.ACTION_MAIN);i.addCategory(Intent.CATEGORY_LAUNCHER);List<ResolveInfo> a=getPackageManager().queryIntentActivities(i,0);Collections.sort(a,Comparator.comparing(x->String.valueOf(x.loadLabel(getPackageManager())),String.CASE_INSENSITIVE_ORDER));return a;}
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
 @Override public void onBackPressed(){if(ui!=null&&!"HOME".equals(ui.getScreen()))goBack();else super.onBackPressed();}
}
