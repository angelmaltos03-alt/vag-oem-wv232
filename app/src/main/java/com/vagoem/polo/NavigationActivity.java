package com.vagoem.polo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.maplibre.android.MapLibre;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.PolylineOptions;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

public class NavigationActivity extends Activity {
 private static final int REQ_LOCATION=710;
 private static final String STYLE="https://tiles.openfreemap.org/styles/liberty";
 private MapView mapView; private MapLibreMap map; private TextView status; private EditText search;
 private boolean styleReady=false;
 private LatLng destination;
 private OfflineRoutingEngine routeEngine;
 private final LatLng defaultCenter=new LatLng(25.5700,-103.4950);
 @Override public void onCreate(Bundle state){
  super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  MapLibre.getInstance(this); routeEngine=new OfflineRoutingEngine(this); build(state);
 }
 private void styleButton(Button b,boolean primary){\n  float d=getResources().getDisplayMetrics().density;GradientDrawable bg=new GradientDrawable();bg.setColor(primary?0xFF351820:0xFF1B2632);bg.setCornerRadius(7*d);bg.setStroke(Math.max(1,(int)d),primary?0xFFB94A5B:0xFF465564);b.setBackground(bg);b.setTextColor(Color.WHITE);b.setTextSize(10);b.setAllCaps(false);b.setPadding(7,3,7,3);\n }\n private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);return t;}
 private void build(Bundle state){
  FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(0xFF080D15);
  mapView=new MapView(this);frame.addView(mapView,new FrameLayout.LayoutParams(-1,-1));mapView.onCreate(state);
  LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.VERTICAL);top.setPadding(18,12,18,12);top.setBackgroundColor(0xF2141D27);
  TextView brand=text("NAVEGACIÓN   ·   POLO 2020",16,Color.WHITE,true);top.addView(brand);
  status=text("Preparando cartografía…",11,0xFFB8C3D0,false);top.addView(status);
  FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);tp.setMargins(14,12,14,0);frame.addView(top,tp);
  LinearLayout bottom=new LinearLayout(this);bottom.setOrientation(LinearLayout.VERTICAL);bottom.setPadding(12,10,12,10);bottom.setBackgroundColor(0xF2141D27);
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
  search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(0xFF9BA8B7);search.setHint("¿A dónde vamos?");search.setTextSize(14);search.setPadding(12,0,12,0);search.setBackgroundColor(0xFF26313D);
  row.addView(search,new LinearLayout.LayoutParams(0,48,1));
  Button go=new Button(this);go.setText("Buscar");styleButton(go,true);row.addView(go,new LinearLayout.LayoutParams(110,48));go.setOnClickListener(v->searchDestination());
  bottom.addView(row);
  LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
  Button locate=new Button(this);locate.setText("Mi ubicación");styleButton(locate,false);actions.addView(locate,new LinearLayout.LayoutParams(0,48,1));locate.setOnClickListener(v->locate());
  Button tilt=new Button(this);tilt.setText("Vista 3D");styleButton(tilt,false);actions.addView(tilt,new LinearLayout.LayoutParams(0,48,1));tilt.setOnClickListener(v->toggle3d());
  Button layers=new Button(this);layers.setText("Capas");styleButton(layers,false);actions.addView(layers,new LinearLayout.LayoutParams(0,48,1));layers.setOnClickListener(v->showLayers());
  bottom.addView(actions);
  LinearLayout extra=new LinearLayout(this);extra.setOrientation(LinearLayout.HORIZONTAL);
  Button offline=new Button(this);offline.setText("Mapas offline");styleButton(offline,false);extra.addView(offline,new LinearLayout.LayoutParams(0,44,1));offline.setOnClickListener(v->startActivity(new android.content.Intent(this,OfflineMapsActivity.class)));
  Button report=new Button(this);report.setText("Reportar");styleButton(report,false);extra.addView(report,new LinearLayout.LayoutParams(0,44,1));report.setOnClickListener(v->reportAlert());
  Button alerts=new Button(this);alerts.setText("Alertas");styleButton(alerts,false);extra.addView(alerts,new LinearLayout.LayoutParams(0,44,1));alerts.setOnClickListener(v->showAlerts());
  Button route=new Button(this);route.setText("Trazar ruta");styleButton(route,true);extra.addView(route,new LinearLayout.LayoutParams(0,44,1));route.setOnClickListener(v->routeToDestination());
  bottom.addView(extra);
  TextView attribution=text("© OpenStreetMap contributors · OpenFreeMap",9,0xFFB8C3D0,false);attribution.setGravity(Gravity.CENTER);bottom.addView(attribution);
  FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);bp.setMargins(12,0,12,12);frame.addView(bottom,bp);
  Button back=new Button(this);back.setText("‹");back.setTextSize(24);styleButton(back,false);FrameLayout.LayoutParams backp=new FrameLayout.LayoutParams(54,54,Gravity.TOP|Gravity.LEFT);backp.setMargins(18,100,0,0);frame.addView(back,backp);back.setOnClickListener(v->finish());
  setContentView(frame);
  mapView.getMapAsync(m->{map=m;map.setStyle(new Style.Builder().fromUri(STYLE),s->{styleReady=true;map.setCameraPosition(new CameraPosition.Builder().target(defaultCenter).zoom(9.2).build());status.setText("Mapa listo · La Laguna · descarga paquetes offline y guarda alertas locales");loadLocalAlerts();});});
 }
 private void loadLocalAlerts(){if(map==null)return;for(LocalAlertStore.Alert a:LocalAlertStore.all(this)){map.addMarker(new MarkerOptions().position(new LatLng(a.lat,a.lon)).title("ALERTA · "+a.type).snippet(a.note));}}
 private void reportAlert(){
  if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);status.setText("Concede ubicación y vuelve a pulsar REPORTAR.");return;}
  LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location loc=null;try{for(String provider:lm.getProviders(true)){Location x=lm.getLastKnownLocation(provider);if(x!=null&&(loc==null||x.getTime()>loc.getTime()))loc=x;}}catch(SecurityException ignored){}
  if(loc==null){status.setText("Aún no hay posición GPS. Pulsa MI UBICACIÓN y vuelve a reportar.");return;}
  final Location here=loc;String[] types={"Peligro en carretera","Bache","Obra / cierre","Inundación","Accidente","Vehículo detenido","Retén / control","Otro"};
  new AlertDialog.Builder(this).setTitle("Reportar incidencia en esta ubicación").setItems(types,(d,w)->{
   EditText note=new EditText(this);note.setSingleLine(true);note.setHint("Nota opcional");
   new AlertDialog.Builder(this).setTitle(types[w]).setView(note).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(dd,ww)->{
    String n=note.getText().toString().trim();LocalAlertStore.add(this,types[w],n,here.getLatitude(),here.getLongitude());
    if(map!=null)map.addMarker(new MarkerOptions().position(new LatLng(here.getLatitude(),here.getLongitude())).title("ALERTA · "+types[w]).snippet(n));
    status.setText("Alerta guardada localmente en este estéreo. No se ha compartido con otros conductores.");
   }).show();
  }).setNegativeButton("Cancelar",null).show();
 }
 private void showAlerts(){java.util.List<LocalAlertStore.Alert> items=LocalAlertStore.all(this);if(items.isEmpty()){new AlertDialog.Builder(this).setTitle("Alertas guardadas").setMessage("Todavía no hay alertas locales. Puedes reportar un peligro desde tu ubicación GPS.").setPositiveButton("OK",null).show();return;}
  String[] lines=new String[items.size()];for(int i=0;i<items.size();i++){LocalAlertStore.Alert a=items.get(i);lines[i]=a.type+" · "+String.format(Locale.US,"%.5f, %.5f",a.lat,a.lon)+(a.note.isEmpty()?"":"\n"+a.note);}
  new AlertDialog.Builder(this).setTitle("Alertas guardadas en este equipo").setItems(lines,(d,w)->{LocalAlertStore.Alert a=items.get(w);if(map!=null)map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(new LatLng(a.lat,a.lon),16));}).setPositiveButton("Cerrar",null).show();
 }
 private void searchDestination(){
  String q=search.getText().toString().trim();if(q.isEmpty()){search.setError("Escribe un destino");return;}
  String[] coordinateParts=q.replace(" ","").split(",");
  if(coordinateParts.length==2){try{double lat=Double.parseDouble(coordinateParts[0]),lon=Double.parseDouble(coordinateParts[1]);if(lat>=-90&&lat<=90&&lon>=-180&&lon<=180){setDestination(new LatLng(lat,lon),"Coordenadas GPS");return;}}catch(NumberFormatException ignored){}}
  if(!Geocoder.isPresent()){status.setText("Sin buscador de direcciones. Puedes introducir coordenadas latitud,longitud.");return;}
  status.setText("Buscando: "+q+"…");
  new Thread(()->{try{List<Address> a=new Geocoder(this,Locale.getDefault()).getFromLocationName(q,1);runOnUiThread(()->{if(a==null||a.isEmpty()){status.setText("No encontramos el destino. La búsqueda inicial requiere conexión.");return;}Address x=a.get(0);setDestination(new LatLng(x.getLatitude(),x.getLongitude()),q);});}catch(Exception e){runOnUiThread(()->status.setText("Búsqueda no disponible sin conexión o proveedor de geocodificación."));}},"VAG-Geocoder").start();
 }
 private void setDestination(LatLng point,String label){
  destination=point;
  if(map!=null){map.addMarker(new MarkerOptions().position(point).title(label));map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(point,14));}
  status.setText("Destino seleccionado · "+label+" · pulsa TRAZAR RUTA");
 }
 private void routeToDestination(){
  if(destination==null){status.setText("Primero busca y selecciona un destino.");return;}
  if(!routeEngine.hasRegionalTiles()){status.setText("Motor Valhalla integrado. Falta instalar el paquete de red vial offline de la región; el mapa no inventará rutas.");return;}
  if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);return;}
  Location origin=null;try{LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);for(String provider:lm.getProviders(true)){Location l=lm.getLastKnownLocation(provider);if(l!=null&&(origin==null||l.getTime()>origin.getTime()))origin=l;}}catch(SecurityException ignored){}
  if(origin==null){status.setText("Esperando una posición GPS válida para iniciar la ruta.");return;}
  final double lat=origin.getLatitude(),lon=origin.getLongitude(),toLat=destination.getLatitude(),toLon=destination.getLongitude();
  status.setText("Calculando ruta local en el equipo…");
  new Thread(()->{try{
   String raw=routeEngine.route(lat,lon,toLat,toLon);JSONObject trip=new JSONObject(raw).getJSONObject("trip");JSONObject leg=trip.getJSONArray("legs").getJSONObject(0);
   java.util.List<LatLng> points=decodePolyline6(leg.getString("shape"));
   String instruction="Ruta calculada";if(leg.has("maneuvers")&&leg.getJSONArray("maneuvers").length()>0)instruction=leg.getJSONArray("maneuvers").getJSONObject(0).optString("instruction",instruction);
   final String nextInstruction=instruction;
   final String summary=String.format(Locale.US,"Ruta offline lista · %.1f km · %.0f min",trip.getJSONObject("summary").optDouble("length",0),trip.getJSONObject("summary").optDouble("time",0)/60.0);
   runOnUiThread(()->{if(map!=null&&!points.isEmpty()){map.addPolyline(new PolylineOptions().addAll(points).color(0xFFE31B36).width(6));map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(destination,12));}status.setText(summary+" · "+nextInstruction);});
  }catch(Exception e){runOnUiThread(()->status.setText("No se pudo calcular ruta offline: "+e.getMessage()));}},"VAG-Valhalla-Route").start();
 }
 private java.util.List<LatLng> decodePolyline6(String encoded){
  java.util.List<LatLng> out=new java.util.ArrayList<>();int index=0,lat=0,lon=0;
  while(index<encoded.length()){int result=0,shift=0,b;do{b=encoded.charAt(index++)-63;result|=(b&0x1f)<<shift;shift+=5;}while(b>=0x20&&index<encoded.length());lat+=((result&1)!=0?~(result>>1):(result>>1));result=0;shift=0;do{b=encoded.charAt(index++)-63;result|=(b&0x1f)<<shift;shift+=5;}while(b>=0x20&&index<encoded.length());lon+=((result&1)!=0?~(result>>1):(result>>1));out.add(new LatLng(lat/1e6,lon/1e6));}
  return out;
 }
 private void locate(){
  if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);return;}
  try{LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);Location best=null;for(String provider:lm.getProviders(true)){Location l=lm.getLastKnownLocation(provider);if(l!=null&&(best==null||l.getTime()>best.getTime()))best=l;}if(best==null){status.setText("Esperando ubicación GPS…");return;}LatLng p=new LatLng(best.getLatitude(),best.getLongitude());if(map!=null){map.addMarker(new MarkerOptions().position(p).title("Mi ubicación"));map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(p,15));}status.setText("GPS · posición obtenida del equipo");}catch(SecurityException e){status.setText("Permiso de ubicación no concedido");}catch(Exception e){status.setText("GPS no disponible en este momento");}
 }
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ_LOCATION&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)locate();else status.setText("Permiso GPS pendiente");}
 private void toggle3d(){if(map==null)return;CameraPosition c=map.getCameraPosition();double tilt=c.tilt>20?0:55;map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder(c).tilt(tilt).build()));status.setText(tilt>0?"Perspectiva inclinada · edificios 3D según estilo":"Vista superior");}
 private void showLayers(){new AlertDialog.Builder(this).setTitle("Capas de navegación").setItems(new String[]{"Mapa estándar","Perspectiva 3D","Vista superior"},(d,w)->{if(w==1){CameraPosition c=map.getCameraPosition();map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder(c).tilt(55).build()));}else if(w==2){CameraPosition c=map.getCameraPosition();map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder(c).tilt(0).build()));}else if(map!=null)map.setStyle(new Style.Builder().fromUri(STYLE));}).show();}
 @Override protected void onStart(){super.onStart();if(mapView!=null)mapView.onStart();}
 @Override protected void onResume(){super.onResume();if(mapView!=null)mapView.onResume();}
 @Override protected void onPause(){if(mapView!=null)mapView.onPause();super.onPause();}
 @Override protected void onStop(){if(mapView!=null)mapView.onStop();super.onStop();}
 @Override protected void onLowMemory(){super.onLowMemory();if(mapView!=null)mapView.onLowMemory();}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);if(mapView!=null)mapView.onSaveInstanceState(out);}
 @Override protected void onDestroy(){if(mapView!=null)mapView.onDestroy();if(routeEngine!=null)routeEngine.close();super.onDestroy();}
}
