package com.vagoem.polo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StatFs;
import android.widget.*;
import org.json.JSONObject;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.geometry.LatLngBounds;
import org.maplibre.android.offline.OfflineManager;
import org.maplibre.android.offline.OfflineRegion;
import org.maplibre.android.offline.OfflineRegionError;
import org.maplibre.android.offline.OfflineRegionStatus;
import org.maplibre.android.offline.OfflineTilePyramidRegionDefinition;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class OfflineMapsActivity extends Activity {
 private static final String STYLE="https://tiles.openfreemap.org/styles/liberty";
 private static final long MAX_BYTES=50L*1024L*1024L;
 private LinearLayout root; private TextView status,storage; private ProgressBar progress;
 private OfflineManager manager;
 private static final class Pack {String name;double south,west,north,east,min,max;Pack(String n,double s,double w,double no,double e,double mi,double ma){name=n;south=s;west=w;north=no;east=e;min=mi;max=ma;}}
 private final Pack[] packs={
  new Pack("Durango · mapa estatal general",22.3,-107.2,26.8,-102.4,4,7),
  new Pack("Coahuila · mapa estatal general",24.5,-103.9,29.9,-99.8,4,7),
  new Pack("La Laguna · detalle urbano",25.25,-103.75,26.15,-102.65,7,13),
  new Pack("Durango capital · detalle urbano",23.75,-104.85,24.15,-104.45,7,13),
  new Pack("Saltillo · detalle urbano",25.30,-101.15,25.60,-100.75,7,13)
 };
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);manager=OfflineManager.getInstance(this);manager.setOfflineMapboxTileCountLimit(3000);build();refreshStorage();listRegions();}
 private TextView label(String s,int z,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setPadding(3,7,3,7);return t;}
 private void card(android.view.View v){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(14,10,14,10);box.setBackgroundColor(0xFF18212D);box.addView(v);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,7,0,7);root.addView(box,p);}
 private void build(){ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,14,18,22);root.setBackgroundColor(0xFF0B111A);sc.addView(root);
  root.addView(label("VAG MAP MANAGER",24,Color.WHITE,true));root.addView(label("Mapas offline · Durango y Coahuila",14,0xFF9BA8B7,false));
  status=label("Selecciona una región para descargarla con Wi-Fi.",13,0xFFFF6673,true);card(status);
  progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);card(progress);
  storage=label("",13,0xFFD7DEE8,false);card(storage);
  root.addView(label("PAQUETES REGIONALES",16,Color.WHITE,true));
  for(Pack p:packs){LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.VERTICAL);TextView n=label(p.name,14,Color.WHITE,true);line.addView(n);TextView detail=label(String.format(Locale.US,"Zoom %.0f–%.0f · paquete limitado a 3,000 tiles / 50 MB",p.min,p.max),11,0xFF9BA8B7,false);line.addView(detail);Button d=new Button(this);d.setText("DESCARGAR PARA USO OFFLINE");d.setOnClickListener(v->download(p));line.addView(d);card(line);}
  root.addView(label("MAPAS INSTALADOS",16,Color.WHITE,true));
  button("ACTUALIZAR LISTA",this::listRegions);
  button("VOLVER A NAVEGACIÓN",this::finish);
  card(label("Cada paquete se descarga solo cuando lo eliges. Se limita a 3,000 tiles o 50 MB por paquete de acuerdo con las condiciones del proveedor. Descarga con Wi-Fi. Los paquetes se guardan en el almacenamiento privado de VAG OEM y el mapa los utiliza sin conexión después de completarse. La cartografía requiere actualización periódica.",12,0xFFFFC4C4,false));
  card(label("Atribución: OpenFreeMap © OpenMapTiles · Datos de OpenStreetMap (ODbL).",10,0xFFD7DEE8,false));
  setContentView(sc);
 }
 private void button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setOnClickListener(v->action.run());root.addView(b,new LinearLayout.LayoutParams(-1,50));}
 private void refreshStorage(){StatFs fs=new StatFs(getFilesDir().getAbsolutePath());long free=fs.getAvailableBytes();storage.setText(String.format(Locale.US,"Espacio libre app: %.2f GB. Se recomienda al menos 500 MB libres antes de descargar un paquete.",free/1e9));}
 private void download(Pack p){
  StatFs fs=new StatFs(getFilesDir().getAbsolutePath());if(fs.getAvailableBytes()<600L*1024L*1024L){status.setText("Espacio insuficiente: libera al menos 600 MB antes de descargar.");return;}
  status.setText("Preparando "+p.name+"…");progress.setProgress(0);
  LatLngBounds bounds=new LatLngBounds.Builder().include(new LatLng(p.north,p.east)).include(new LatLng(p.south,p.west)).build();
  OfflineTilePyramidRegionDefinition def=new OfflineTilePyramidRegionDefinition(STYLE,bounds,p.min,p.max,getResources().getDisplayMetrics().density,false);
  try{JSONObject meta=new JSONObject();meta.put("name",p.name);meta.put("provider","OpenFreeMap");meta.put("maxBytes",MAX_BYTES);
   manager.createOfflineRegion(def,meta.toString().getBytes(StandardCharsets.UTF_8),new OfflineManager.CreateOfflineRegionCallback(){
    @Override public void onCreate(OfflineRegion region){status.setText("Descargando "+p.name+"…");region.setDeliverInactiveMessages(true);region.setObserver(new OfflineRegion.OfflineRegionObserver(){
     @Override public void onStatusChanged(OfflineRegionStatus s){long count=s.getCompletedTileCount();long bytes=s.getCompletedResourceSize();long required=s.getRequiredResourceCount();int pct=required>0?(int)Math.min(100,100*s.getCompletedResourceCount()/required):0;runOnUiThread(()->{progress.setProgress(pct);status.setText(String.format(Locale.US,"%s · %d tiles · %.1f MB · %d%%",p.name,count,bytes/1048576.0,pct));});
      if(count>3000||bytes>MAX_BYTES){region.setDownloadState(OfflineRegion.STATE_INACTIVE);runOnUiThread(()->status.setText("Límite del proveedor alcanzado. Paquete pausado; selecciona una región más pequeña."));}
      else if(s.isComplete()){region.setDownloadState(OfflineRegion.STATE_INACTIVE);runOnUiThread(()->{status.setText("Paquete listo para navegación sin conexión: "+p.name);listRegions();refreshStorage();});}
     }
     @Override public void onError(OfflineRegionError e){runOnUiThread(()->status.setText("Error recuperable de descarga: "+e.getMessage()));}
     @Override public void mapboxTileCountLimitExceeded(long limit){region.setDownloadState(OfflineRegion.STATE_INACTIVE);runOnUiThread(()->status.setText("Límite de tiles alcanzado ("+limit+")."));}
    });region.setDownloadState(OfflineRegion.STATE_ACTIVE);}
    @Override public void onError(String error){status.setText("No se pudo crear el paquete: "+error);}
   });
  }catch(Exception e){status.setText("Error al preparar descarga: "+e.getMessage());}
 }
 private void listRegions(){manager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback(){
  @Override public void onList(OfflineRegion[] regions){runOnUiThread(()->{root.addView(label("Paquetes guardados: "+(regions==null?0:regions.length),12,0xFF8FD6A7,true));if(regions!=null)for(OfflineRegion r:regions){String m=new String(r.getMetadata(),StandardCharsets.UTF_8);TextView t=label("• "+m,11,0xFFD7DEE8,false);root.addView(t);}});}
  @Override public void onError(String error){runOnUiThread(()->status.setText("No se pudo consultar la lista offline: "+error));}
 });}
}
