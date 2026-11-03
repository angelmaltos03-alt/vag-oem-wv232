package com.vagoem.polo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.*;

import org.maplibre.android.MapLibre;
import org.maplibre.android.annotations.Marker;
import org.maplibre.android.annotations.MarkerOptions;
import org.maplibre.android.annotations.Polyline;
import org.maplibre.android.annotations.PolylineOptions;
import org.maplibre.android.camera.CameraPosition;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.Style;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.json.JSONObject;

/**
 * VAG Navigation — full-screen MapLibre map with a compact VW-style search
 * surface, floating map tools and an adaptive route-preview sheet.
 *
 * The route preview is a calculated route only. Continuous GPS guidance,
 * traffic overlays and voice navigation are intentionally not claimed here.
 */
public class NavigationActivity extends Activity {
 private static final int REQ_LOCATION=710;
 private static final String STYLE="https://tiles.openfreemap.org/styles/liberty";
 private static final int BG=0xF2141D27;
 private static final int BG_DARK=0xFF0B111A;
 private static final int BG_CARD=0xFF1C2631;
 private static final int RED=0xFFE34A60;
 private static final int RED_DARK=0xFF351820;
 private static final int WHITE=0xFFF1F4F7;
 private static final int MUTED=0xFF9BA8B7;
 private static final int STROKE=0xFF3D4A57;

 private MapView mapView;
 private MapLibreMap map;
 private FrameLayout root;
 private LinearLayout bottomSheet;
 private TextView status, sheetTitle, sheetSubtitle;
 private EditText search;
 private boolean styleReady=false;
 private boolean pendingRoute=false;
 private LatLng destination;
 private LatLng currentLocation;
 private String destinationLabel="";
 private String routeSummary="";
 private String routeDistance="";
 private String routeDuration="";
 private String nextInstruction="";
 private Marker destinationMarker;
 private Marker locationMarker;
 private Polyline routeCasing;
 private Polyline routeLine;
 private OfflineRoutingEngine routeEngine;
 private final LatLng defaultCenter=new LatLng(25.5700,-103.4950);

 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  getWindow().setStatusBarColor(BG_DARK);
  getWindow().setNavigationBarColor(BG_DARK);
  getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  MapLibre.getInstance(this);
  routeEngine=new OfflineRoutingEngine(this);
  build(state);
 }

 private int dp(float value){
  return Math.round(value*getResources().getDisplayMetrics().density);
 }

 private GradientDrawable rounded(int color,int stroke,int radius){
  GradientDrawable bg=new GradientDrawable();
  bg.setColor(color);
  bg.setCornerRadius(dp(radius));
  if(stroke!=0)bg.setStroke(dp(1),stroke);
  return bg;
 }

 private GradientDrawable sheetBackground(){
  GradientDrawable bg=new GradientDrawable();
  bg.setColor(BG);
  bg.setCornerRadii(new float[]{dp(19),dp(19),dp(19),dp(19),0,0,0,0});
  bg.setStroke(dp(1),0x443D4855);
  return bg;
 }

 private TextView label(String value,int size,int color,boolean bold){
  TextView t=new TextView(this);
  t.setText(value);
  t.setTextSize(size);
  t.setTextColor(color);
  t.setGravity(Gravity.CENTER_VERTICAL);
  t.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));
  return t;
 }

 private TextView action(String text,boolean primary,Runnable callback){
  TextView b=label(text,9,primary?WHITE:0xFFD7DEE8,true);
  b.setGravity(Gravity.CENTER);
  b.setMaxLines(2);
  b.setPadding(dp(5),0,dp(5),0);
  b.setBackground(rounded(primary?RED_DARK:0xFF202B37,primary?0xFFB84A5D:STROKE,7));
  b.setClickable(true);
  b.setFocusable(true);
  b.setElevation(dp(2));
  b.setOnClickListener(v->{if(callback!=null)callback.run();});
  return b;
 }

 private TextView floating(String glyph,Runnable callback){
  TextView b=label(glyph,19,WHITE,true);
  b.setGravity(Gravity.CENTER);
  b.setBackground(rounded(0xF21A2430,0xFF4A5867,25));
  b.setClickable(true);
  b.setFocusable(true);
  b.setElevation(dp(5));
  b.setOnClickListener(v->{if(callback!=null)callback.run();});
  return b;
 }

 private void addWeighted(LinearLayout row,View view,int weight,int height,int margin){
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,dp(height),weight);
  lp.setMargins(dp(margin),0,dp(margin),0);
  row.addView(view,lp);
 }

 private void build(Bundle state){
  root=new FrameLayout(this);
  root.setBackgroundColor(BG_DARK);

  mapView=new MapView(this);
  root.addView(mapView,new FrameLayout.LayoutParams(-1,-1));
  mapView.onCreate(state);

  buildSearchSurface();
  buildMapControls();
  buildBottomSheet();

  setContentView(root);

  mapView.getMapAsync(m->{
   map=m;
   map.setStyle(new Style.Builder().fromUri(STYLE),s->{
    styleReady=true;
    map.setCameraPosition(new CameraPosition.Builder()
      .target(defaultCenter).zoom(11.1).build());
    updateStatus("MAPA LISTO  ·  LA LAGUNA");
    loadLocalAlerts();
    if(destination!=null)placeDestinationMarker();
   });
  });
 }

 private void buildSearchSurface(){
  LinearLayout surface=new LinearLayout(this);
  surface.setOrientation(LinearLayout.VERTICAL);
  surface.setPadding(dp(9),dp(8),dp(9),dp(7));
  surface.setBackground(rounded(0xF216202B,0xFF3D4A57,10));
  surface.setElevation(dp(8));

  LinearLayout row=new LinearLayout(this);
  row.setOrientation(LinearLayout.HORIZONTAL);
  row.setGravity(Gravity.CENTER_VERTICAL);

  TextView back=label("‹",27,RED,false);
  back.setGravity(Gravity.CENTER);
  back.setBackground(rounded(0xFF202B37,STROKE,8));
  back.setOnClickListener(v->finish());
  row.addView(back,new LinearLayout.LayoutParams(dp(42),dp(43)));

  search=new EditText(this);
  search.setSingleLine(true);
  search.setTextColor(WHITE);
  search.setHintTextColor(0xFF9BA8B7);
  search.setHint("Buscar lugar o destino");
  search.setTextSize(14);
  search.setPadding(dp(12),0,dp(8),0);
  search.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
  search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
  search.setBackground(rounded(0xFF26313D,0xFF3D4A57,7));
  LinearLayout.LayoutParams searchLp=new LinearLayout.LayoutParams(0,dp(43),1);
  searchLp.setMargins(dp(8),0,dp(8),0);
  row.addView(search,searchLp);
  search.setOnEditorActionListener((v,id,event)->{
   if(id==EditorInfo.IME_ACTION_SEARCH||(event!=null&&event.getKeyCode()==android.view.KeyEvent.KEYCODE_ENTER)){
    searchDestination();return true;
   }
   return false;
  });

  TextView go=label("IR",10,WHITE,true);
  go.setGravity(Gravity.CENTER);
  go.setBackground(rounded(RED_DARK,0xFFB84A5D,7));
  go.setClickable(true);
  go.setOnClickListener(v->searchDestination());
  row.addView(go,new LinearLayout.LayoutParams(dp(48),dp(43)));
  surface.addView(row);

  LinearLayout meta=new LinearLayout(this);
  meta.setOrientation(LinearLayout.HORIZONTAL);
  meta.setGravity(Gravity.CENTER_VERTICAL);
  TextView dot=label("●",8,RED,true);
  meta.addView(dot,new LinearLayout.LayoutParams(dp(14),dp(20)));
  status=label("CARGANDO CARTOGRAFÍA",8,MUTED,true);
  meta.addView(status,new LinearLayout.LayoutParams(0,dp(20),1));
  TextView branding=label("VAG NAVIGATION",8,0xFFB5BFCA,true);
  branding.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL);
  meta.addView(branding);
  surface.addView(meta);

  FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-2,Gravity.TOP|Gravity.CENTER_HORIZONTAL);
  lp.setMargins(dp(14),dp(10),dp(14),0);
  root.addView(surface,lp);
 }

 private void buildMapControls(){
  LinearLayout controls=new LinearLayout(this);
  controls.setOrientation(LinearLayout.VERTICAL);
  controls.setGravity(Gravity.CENTER);
  controls.setElevation(dp(4));

  TextView locate=floating("◎",this::locate);
  controls.addView(locate,new LinearLayout.LayoutParams(dp(46),dp(46)));
  addControlGap(controls,8);

  TextView tilt=floating("3D",this::toggle3d);
  tilt.setTextSize(11);
  controls.addView(tilt,new LinearLayout.LayoutParams(dp(46),dp(46)));
  addControlGap(controls,8);

  TextView layers=floating("☷",this::showLayers);
  controls.addView(layers,new LinearLayout.LayoutParams(dp(46),dp(46)));

  FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(46),-2,Gravity.RIGHT|Gravity.TOP);
  lp.setMargins(0,dp(112),dp(14),0);
  root.addView(controls,lp);
 }

 private void addControlGap(LinearLayout parent,int height){
  View gap=new View(this);
  parent.addView(gap,new LinearLayout.LayoutParams(1,dp(height)));
 }

 private void buildBottomSheet(){
  bottomSheet=new LinearLayout(this);
  bottomSheet.setOrientation(LinearLayout.VERTICAL);
  bottomSheet.setPadding(dp(15),dp(9),dp(15),dp(8));
  bottomSheet.setBackground(sheetBackground());
  bottomSheet.setElevation(dp(12));
  rebuildBottomSheet();

  FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM);
  lp.setMargins(dp(10),0,dp(10),dp(8));
  root.addView(bottomSheet,lp);
 }

 private void rebuildBottomSheet(){
  if(bottomSheet==null)return;
  bottomSheet.removeAllViews();

  View handle=new View(this);
  handle.setBackground(rounded(0xFF718091,0,4));
  LinearLayout.LayoutParams handleLp=new LinearLayout.LayoutParams(dp(38),dp(4));
  handleLp.gravity=Gravity.CENTER_HORIZONTAL;
  handleLp.bottomMargin=dp(8);
  bottomSheet.addView(handle,handleLp);

  LinearLayout heading=new LinearLayout(this);
  heading.setOrientation(LinearLayout.HORIZONTAL);
  heading.setGravity(Gravity.CENTER_VERTICAL);
  TextView title=label(destination==null?"EXPLORA LA LAGUNA":(routeSummary.isEmpty()?"DESTINO SELECCIONADO":"VISTA PREVIA DE RUTA"),10,0xFFE7ECF1,true);
  heading.addView(title,new LinearLayout.LayoutParams(0,dp(21),1));
  TextView badge=label(routeSummary.isEmpty()?"MAPA · OPENFREEMAP":"VALHALLA · OFFLINE",8,0xFFFF8794,true);
  badge.setGravity(Gravity.CENTER);
  badge.setPadding(dp(9),0,dp(9),0);
  badge.setBackground(rounded(0xFF321B23,0xFF76404D,12));
  heading.addView(badge,new LinearLayout.LayoutParams(-2,dp(22)));
  bottomSheet.addView(heading);

  if(routeSummary.isEmpty()){
   String titleText=destination==null?"¿A dónde vamos?":destinationLabel;
   TextView destinationText=label(titleText,17,WHITE,true);
   destinationText.setSingleLine(true);
   destinationText.setEllipsize(android.text.TextUtils.TruncateAt.END);
   LinearLayout.LayoutParams destLp=new LinearLayout.LayoutParams(-1,dp(27));
   destLp.topMargin=dp(2);
   bottomSheet.addView(destinationText,destLp);

   String hint=destination==null
     ?"Busca una dirección arriba o introduce coordenadas GPS."
     :"Destino marcado. Calcula una ruta local cuando el paquete vial esté instalado.";
   TextView desc=label(hint,9,MUTED,false);
   desc.setSingleLine(true);
   desc.setEllipsize(android.text.TextUtils.TruncateAt.END);
   bottomSheet.addView(desc,new LinearLayout.LayoutParams(-1,dp(19)));
  }else{
   LinearLayout metrics=new LinearLayout(this);
   metrics.setOrientation(LinearLayout.HORIZONTAL);
   metrics.setGravity(Gravity.CENTER_VERTICAL);
   TextView distance=metricBox(routeDistance,"DISTANCIA");
   metrics.addView(distance,new LinearLayout.LayoutParams(0,dp(47),1));
   View divider=new View(this);divider.setBackgroundColor(0x553D4855);
   metrics.addView(divider,new LinearLayout.LayoutParams(dp(1),dp(30)));
   TextView duration=metricBox(routeDuration,"TIEMPO ESTIMADO");
   metrics.addView(duration,new LinearLayout.LayoutParams(0,dp(47),1));
   bottomSheet.addView(metrics);
   if(!nextInstruction.isEmpty()){
    TextView instruction=label("↗  "+nextInstruction,10,0xFFD7DEE8,true);
    instruction.setSingleLine(true);
    instruction.setEllipsize(android.text.TextUtils.TruncateAt.END);
    bottomSheet.addView(instruction,new LinearLayout.LayoutParams(-1,dp(23)));
   }
  }

  LinearLayout actions=new LinearLayout(this);
  actions.setOrientation(LinearLayout.HORIZONTAL);
  actions.setGravity(Gravity.CENTER_VERTICAL);
  LinearLayout.LayoutParams actionsLp=new LinearLayout.LayoutParams(-1,dp(42));
  actionsLp.topMargin=dp(7);

  addWeighted(actions,action("MAPAS OFFLINE",false,()->startActivity(new Intent(this,OfflineMapsActivity.class))),1,42,3);
  addWeighted(actions,action("REPORTAR",false,this::reportAlert),1,42,3);
  addWeighted(actions,action("ALERTAS",false,this::showAlerts),1,42,3);

  if(routeSummary.isEmpty()){
   TextView route=action(destination==null?"BUSCAR DESTINO":"CALCULAR RUTA",true,this::routeToDestination);
   if(destination==null)route.setAlpha(0.72f);
   addWeighted(actions,route,1,42,3);
  }else{
   addWeighted(actions,action("NUEVA RUTA",true,this::clearDestination),1,42,3);
  }
  bottomSheet.addView(actions,actionsLp);

  TextView attribution=label("© OpenStreetMap contributors  ·  OpenFreeMap",8,0xFF8996A5,false);
  attribution.setGravity(Gravity.CENTER);
  LinearLayout.LayoutParams attrLp=new LinearLayout.LayoutParams(-1,dp(15));
  attrLp.topMargin=dp(3);
  bottomSheet.addView(attribution,attrLp);
 }

 private View metricBox(String value,String caption){
  LinearLayout wrap=new LinearLayout(this);
  wrap.setOrientation(LinearLayout.VERTICAL);
  wrap.setGravity(Gravity.CENTER);
  TextView number=label(value,19,WHITE,true);
  number.setGravity(Gravity.CENTER);
  TextView sub=label(caption,7,MUTED,true);
  sub.setGravity(Gravity.CENTER);
  wrap.addView(number,new LinearLayout.LayoutParams(-1,dp(27)));
  wrap.addView(sub,new LinearLayout.LayoutParams(-1,dp(14)));
  return wrap;
 }

 private void updateStatus(String value){
  if(status!=null)status.setText(value);
 }

 private Location latestLocation(){
  if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED&&checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)return null;
  try{
   LocationManager lm=(LocationManager)getSystemService(LOCATION_SERVICE);
   Location best=null;
   for(String provider:lm.getProviders(true)){
    Location found=lm.getLastKnownLocation(provider);
    if(found!=null&&(best==null||found.getTime()>best.getTime()))best=found;
   }
   return best;
  }catch(SecurityException e){return null;}
 }

 private void hideKeyboard(){
  try{
   InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
   imm.hideSoftInputFromWindow(search.getWindowToken(),0);
   search.clearFocus();
  }catch(Exception ignored){}
 }

 private void searchDestination(){
  String q=search.getText().toString().trim();
  if(q.isEmpty()){search.setError("Escribe un destino");return;}
  hideKeyboard();
  clearRouteOnly();
  String[] coordinateParts=q.replace(" ","").split(",");
  if(coordinateParts.length==2){
   try{
    double lat=Double.parseDouble(coordinateParts[0]),lon=Double.parseDouble(coordinateParts[1]);
    if(lat>=-90&&lat<=90&&lon>=-180&&lon<=180){
     setDestination(new LatLng(lat,lon),"Coordenadas GPS");return;
    }
   }catch(NumberFormatException ignored){}
  }
  if(!Geocoder.isPresent()){
   updateStatus("GEOCODIFICADOR NO DISPONIBLE · USA COORDENADAS");
   return;
  }
  updateStatus("BUSCANDO DESTINO…");
  new Thread(()->{
   try{
    List<Address> results=new Geocoder(this,Locale.getDefault()).getFromLocationName(q,1);
    runOnUiThread(()->{
     if(results==null||results.isEmpty()){
      updateStatus("NO SE ENCONTRÓ EL DESTINO · PRUEBA CON COORDENADAS");
      return;
     }
     Address a=results.get(0);
     String resolved=a.getFeatureName()!=null?a.getFeatureName():q;
     setDestination(new LatLng(a.getLatitude(),a.getLongitude()),resolved);
    });
   }catch(Exception e){
    runOnUiThread(()->updateStatus("BÚSQUEDA NO DISPONIBLE · REQUIERE CONEXIÓN"));
   }
  },"VAG-Geocoder").start();
 }

 private void setDestination(LatLng point,String title){
  destination=point;
  destinationLabel=title==null||title.trim().isEmpty()?"Destino":title;
  routeSummary="";
  routeDistance="";
  routeDuration="";
  nextInstruction="";
  placeDestinationMarker();
  if(map!=null)map.animateCamera(CameraUpdateFactory.newLatLngZoom(point,14.3));
  updateStatus("DESTINO MARCADO");
  rebuildBottomSheet();
 }

 private void placeDestinationMarker(){
  if(map==null||destination==null)return;
  if(destinationMarker!=null)map.removeMarker(destinationMarker);
  destinationMarker=map.addMarker(new MarkerOptions().position(destination).title(destinationLabel));
 }

 private void clearRouteOnly(){
  if(map!=null){
   if(routeCasing!=null){map.removePolyline(routeCasing);routeCasing=null;}
   if(routeLine!=null){map.removePolyline(routeLine);routeLine=null;}
  }
  routeSummary="";
  routeDistance="";
  routeDuration="";
  nextInstruction="";
 }

 private void clearDestination(){
  clearRouteOnly();
  if(map!=null&&destinationMarker!=null){map.removeMarker(destinationMarker);destinationMarker=null;}
  destination=null;
  destinationLabel="";
  search.setText("");
  updateStatus("MAPA LISTO  ·  LA LAGUNA");
  rebuildBottomSheet();
 }

 private void routeToDestination(){
  if(destination==null){
   updateStatus("PRIMERO BUSCA UN DESTINO");
   search.requestFocus();
   return;
  }
  if(!routeEngine.hasRegionalTiles()){
   updateStatus("FALTA EL PAQUETE DE RUTAS VALHALLA DE LA REGIÓN");
   new AlertDialog.Builder(this).setTitle("Paquete de rutas pendiente")
     .setMessage("El mapa visual está disponible, pero todavía no hay una red vial Valhalla instalada para calcular recorridos sin conexión.")
     .setPositiveButton("MAPAS OFFLINE", (d,w)->startActivity(new Intent(this,OfflineMapsActivity.class)))
     .setNegativeButton("Cerrar",null).show();
   return;
  }
  Location origin=latestLocation();
  if(origin==null){
   pendingRoute=true;
   requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);
   updateStatus("CONCEDE UBICACIÓN PARA CALCULAR LA RUTA");
   return;
  }
  calculateRoute(origin);
 }

 private void calculateRoute(Location origin){
  if(destination==null)return;
  pendingRoute=false;
  final double fromLat=origin.getLatitude(),fromLon=origin.getLongitude();
  final double toLat=destination.getLatitude(),toLon=destination.getLongitude();
  updateStatus("CALCULANDO RUTA OFFLINE…");
  new Thread(()->{
   try{
    String raw=routeEngine.route(fromLat,fromLon,toLat,toLon);
    JSONObject trip=new JSONObject(raw).getJSONObject("trip");
    JSONObject leg=trip.getJSONArray("legs").getJSONObject(0);
    List<LatLng> points=decodePolyline6(leg.getString("shape"));
    JSONObject summary=trip.getJSONObject("summary");
    final String distance=String.format(Locale.US,"%.1f km",summary.optDouble("length",0));
    final String duration=String.format(Locale.US,"%.0f min",summary.optDouble("time",0)/60.0);
    String maneuver="Ruta calculada";
    if(leg.has("maneuvers")&&leg.getJSONArray("maneuvers").length()>0){
     maneuver=leg.getJSONArray("maneuvers").getJSONObject(0).optString("instruction",maneuver);
    }
    final String instruction=maneuver;
    runOnUiThread(()->{
     if(map==null||points.isEmpty()){
      updateStatus("RUTA SIN GEOMETRÍA VÁLIDA");
      return;
     }
     clearRouteOnly();
     routeCasing=map.addPolyline(new PolylineOptions().addAll(points).color(0xFF17212C).width(13));
     routeLine=map.addPolyline(new PolylineOptions().addAll(points).color(RED).width(7));
     routeDistance=distance;
     routeDuration=duration;
     nextInstruction=instruction;
     routeSummary="READY";
     fitRoute(points);
     updateStatus("RUTA CALCULADA  ·  VISTA PREVIA");
     rebuildBottomSheet();
    });
   }catch(Exception e){
    runOnUiThread(()->updateStatus("NO SE PUDO CALCULAR LA RUTA · "+safeMessage(e)));
   }
  },"VAG-Valhalla-Route").start();
 }

 private String safeMessage(Exception e){
  String m=e.getMessage();
  return m==null?"error desconocido":m;
 }

 private void fitRoute(List<LatLng> points){
  if(map==null||points==null||points.size()<2)return;
  try{
   LatLngBounds.Builder builder=new LatLngBounds.Builder();
   for(LatLng p:points)builder.include(p);
   LatLngBounds bounds=builder.build();
   map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds,dp(72),dp(94),dp(72),dp(230)),650);
  }catch(Exception e){
   LatLng end=points.get(points.size()-1);
   map.animateCamera(CameraUpdateFactory.newLatLngZoom(end,12.3),500);
  }
 }

 private List<LatLng> decodePolyline6(String encoded){
  List<LatLng> out=new ArrayList<>();
  int index=0,lat=0,lon=0;
  while(index<encoded.length()){
   int result=0,shift=0,b;
   do{b=encoded.charAt(index++)-63;result|=(b&0x1f)<<shift;shift+=5;}while(b>=0x20&&index<encoded.length());
   lat+=((result&1)!=0?~(result>>1):(result>>1));
   result=0;shift=0;
   do{b=encoded.charAt(index++)-63;result|=(b&0x1f)<<shift;shift+=5;}while(b>=0x20&&index<encoded.length());
   lon+=((result&1)!=0?~(result>>1):(result>>1));
   out.add(new LatLng(lat/1e6,lon/1e6));
  }
  return out;
 }

 private void locate(){
  Location loc=latestLocation();
  if(loc==null){
   requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);
   updateStatus("PERMISO GPS PENDIENTE");
   return;
  }
  currentLocation=new LatLng(loc.getLatitude(),loc.getLongitude());
  if(map!=null){
   if(locationMarker!=null)map.removeMarker(locationMarker);
   locationMarker=map.addMarker(new MarkerOptions().position(currentLocation).title("Mi ubicación"));
   map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15.2));
  }
  updateStatus("GPS  ·  UBICACIÓN DEL EQUIPO");
 }

 private void reportAlert(){
  Location loc=latestLocation();
  if(loc==null){
   requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},REQ_LOCATION);
   updateStatus("CONCEDE UBICACIÓN Y VUELVE A REPORTAR");
   return;
  }
  final Location here=loc;
  String[] types={"Peligro en carretera","Bache","Obra / cierre","Inundación","Accidente","Vehículo detenido","Retén / control","Otro"};
  new AlertDialog.Builder(this).setTitle("Reportar incidencia").setItems(types,(d,w)->{
   EditText note=new EditText(this);
   note.setSingleLine(true);
   note.setHint("Nota opcional");
   new AlertDialog.Builder(this).setTitle(types[w]).setView(note)
    .setNegativeButton("Cancelar",null)
    .setPositiveButton("Guardar",(dd,ww)->{
     String n=note.getText().toString().trim();
     LocalAlertStore.add(this,types[w],n,here.getLatitude(),here.getLongitude());
     if(map!=null)map.addMarker(new MarkerOptions().position(new LatLng(here.getLatitude(),here.getLongitude())).title("ALERTA · "+types[w]).snippet(n));
     updateStatus("ALERTA GUARDADA EN ESTE EQUIPO · NO COMPARTIDA");
    }).show();
  }).setNegativeButton("Cancelar",null).show();
 }

 private void showAlerts(){
  List<LocalAlertStore.Alert> items=LocalAlertStore.all(this);
  if(items.isEmpty()){
   new AlertDialog.Builder(this).setTitle("Alertas guardadas")
    .setMessage("Todavía no hay alertas locales. Puedes reportar un peligro desde una ubicación GPS.")
    .setPositiveButton("OK",null).show();
   return;
  }
  String[] lines=new String[items.size()];
  for(int i=0;i<items.size();i++){
   LocalAlertStore.Alert a=items.get(i);
   lines[i]=a.type+" · "+String.format(Locale.US,"%.5f, %.5f",a.lat,a.lon)+(a.note.isEmpty()?"":"\n"+a.note);
  }
  new AlertDialog.Builder(this).setTitle("Alertas locales").setItems(lines,(d,w)->{
   LocalAlertStore.Alert a=items.get(w);
   if(map!=null)map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(a.lat,a.lon),16));
  }).setPositiveButton("Cerrar",null).show();
 }

 private void toggle3d(){
  if(map==null)return;
  CameraPosition c=map.getCameraPosition();
  double tilt=c.tilt>20?0:55;
  map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder(c).tilt(tilt).build()),350);
  updateStatus(tilt>0?"PERSPECTIVA 3D":"VISTA SUPERIOR");
 }

 private void showLayers(){
  new AlertDialog.Builder(this).setTitle("Capas del mapa")
   .setItems(new String[]{"Vista estándar","Perspectiva 3D","Vista superior","Mapas offline"},(d,w)->{
    if(w==1)toggle3d();
    else if(w==2&&map!=null){
     CameraPosition c=map.getCameraPosition();
     map.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder(c).tilt(0).build()),350);
    }else if(w==3)startActivity(new Intent(this,OfflineMapsActivity.class));
    else updateStatus("ESTILO LIBERTY · OPENFREEMAP");
   }).show();
 }

 private void loadLocalAlerts(){
  if(map==null)return;
  for(LocalAlertStore.Alert a:LocalAlertStore.all(this)){
   map.addMarker(new MarkerOptions().position(new LatLng(a.lat,a.lon)).title("ALERTA · "+a.type).snippet(a.note));
  }
 }

 @Override public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grants){
  super.onRequestPermissionsResult(requestCode,permissions,grants);
  boolean granted=false;
  for(int value:grants)if(value==PackageManager.PERMISSION_GRANTED)granted=true;
  if(requestCode==REQ_LOCATION&&granted){
   if(pendingRoute){
    Location loc=latestLocation();
    if(loc!=null)calculateRoute(loc);else updateStatus("GPS AÚN NO DISPONIBLE · INTENTA DE NUEVO");
   }else locate();
  }else{
   pendingRoute=false;
   updateStatus("PERMISO GPS NO CONCEDIDO");
  }
 }

 @Override protected void onStart(){super.onStart();if(mapView!=null)mapView.onStart();}
 @Override protected void onResume(){super.onResume();if(mapView!=null)mapView.onResume();}
 @Override protected void onPause(){if(mapView!=null)mapView.onPause();super.onPause();}
 @Override protected void onStop(){if(mapView!=null)mapView.onStop();super.onStop();}
 @Override public void onLowMemory(){super.onLowMemory();if(mapView!=null)mapView.onLowMemory();}
 @Override protected void onSaveInstanceState(Bundle out){super.onSaveInstanceState(out);if(mapView!=null)mapView.onSaveInstanceState(out);}
 @Override protected void onDestroy(){if(mapView!=null)mapView.onDestroy();if(routeEngine!=null)routeEngine.close();super.onDestroy();}
}
