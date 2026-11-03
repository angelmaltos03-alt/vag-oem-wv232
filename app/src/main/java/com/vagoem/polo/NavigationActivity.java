package com.vagoem.polo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import java.util.List;
import java.util.Locale;

public class NavigationActivity extends Activity {
 private static final int REQ_LOCATION=710;
 private static final String STYLE="https://tiles.openfreemap.org/styles/liberty";
 private MapView mapView; private MapLibreMap map; private TextView status; private EditText search;
 private boolean styleReady=false;
 private final LatLng defaultCenter=new LatLng(25.5700,-103.4950);
 @Override public void onCreate(Bundle state){
  super.onCreate(state);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  MapLibre.getInstance(this); build(state);
 }
 private TextView text(String value,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(value);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);return t;}
 private void build(Bundle state){
  FrameLayout frame=new FrameLayout(this);frame.setBackgroundColor(0xFF080D15);
  mapView=new MapView(this);frame.addView(mapView,new FrameLayout.LayoutParams(-1,-1));mapView.onCreate(state);
  LinearLayout top=new LinearLayout(this);top.setOrientation(LinearLayout.VERTICAL);top.setPadding(18,12,18,12);top.setBackgroundColor(0xEE101721);
  TextView brand=text("VAG NAVIGATION   ·   POLO 2020",16,Color.WHITE,true);top.addView(brand);
  status=text("Preparando cartografía…",11,0xFFB8C3D0,false);top.addView(status);
  FrameLayout.LayoutParams tp=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP);tp.setMargins(14,12,14,0);frame.addView(top,tp);
  LinearLayout bottom=new LinearLayout(this);bottom.setOrientation(LinearLayout.VERTICAL);bottom.setPadding(12,10,12,10);bottom.setBackgroundColor(0xEE101721);
  LinearLayout row=new LinearLayout(this);row.setOrientation(LinearLayout.HORIZONTAL);
  search=new EditText(this);search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(0xFF9BA8B7);search.setHint("¿A dónde vamos?");search.setTextSize(14);search.setPadding(12,0,12,0);search.setBackgroundColor(0xFF26313D);
  row.addView(search,new LinearLayout.LayoutParams(0,48,1));
  Button go=new Button(this);go.setText("BUSCAR");row.addView(go,new LinearLayout.LayoutParams(110,48));go.setOnClickListener(v->searchDestination());
  bottom.addView(row);
  LinearLayout actions=new LinearLayout(this);actions.setOrientation(LinearLayout.HORIZONTAL);
  Button locate=new Button(this);locate.setText("MI UBICACIÓN");actions.addView(locate,new LinearLayout.LayoutParams(0,48,1));locate.setOnClickListener(v->locate());
  Button tilt=new Button(this);tilt.setText("VISTA 3D");actions.addView(tilt,new LinearLayout.LayoutParams(0,48,1));tilt.setOnClickListener(v->toggle3d());
  Button layers=new Button(this);layers.setText("CAPAS");actions.addView(layers,new LinearLayout.LayoutParams(0,48,1));layers.setOnClickListener(v->showLayers());
  bottom.addView(actions);
  TextView attribution=text("© OpenStreetMap contributors · OpenFreeMap",9,0xFFB8C3D0,false);attribution.setGravity(Gravity.CENTER);bottom.addView(attribution);
  FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);bp.setMargins(12,0,12,12);frame.addView(bottom,bp);
  Button back=new Button(this);back.setText("‹");back.setTextSize(24);FrameLayout.LayoutParams backp=new FrameLayout.LayoutParams(54,54,Gravity.TOP|Gravity.LEFT);backp.setMargins(18,100,0,0);frame.addView(back,backp);back.setOnClickListener(v->finish());
  setContentView(frame);
  mapView.getMapAsync(m->{map=m;map.setStyle(new Style.Builder().fromUri(STYLE),s->{styleReady=true;map.setCameraPosition(new CameraPosition.Builder().target(defaultCenter).zoom(8.4).build());status.setText("Mapa listo · zona de La Laguna · conexión necesaria para cartografía inicial");});});
 }
 private void searchDestination(){
  String q=search.getText().toString().trim();if(q.isEmpty()){search.setError("Escribe un destino");return;}
  if(!Geocoder.isPresent()){status.setText("Este equipo no tiene servicio de búsqueda de direcciones");return;}
  status.setText("Buscando: "+q+"…");
  new Thread(()->{try{List<Address> a=new Geocoder(this,Locale.getDefault()).getFromLocationName(q,1);runOnUiThread(()->{if(a==null||a.isEmpty()){status.setText("No encontramos el destino. La búsqueda inicial requiere conexión.");return;}Address x=a.get(0);LatLng p=new LatLng(x.getLatitude(),x.getLongitude());if(map!=null){map.addMarker(new MarkerOptions().position(p).title(q));map.animateCamera(org.maplibre.android.camera.CameraUpdateFactory.newLatLngZoom(p,14));}status.setText("Destino localizado · "+q+" · cálculo de ruta pendiente de integrar");});}catch(Exception e){runOnUiThread(()->status.setText("Búsqueda no disponible sin conexión o proveedor de geocodificación."));}},"VAG-Geocoder").start();
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
 @Override protected void onDestroy(){if(mapView!=null)mapView.onDestroy();super.onDestroy();}
}
