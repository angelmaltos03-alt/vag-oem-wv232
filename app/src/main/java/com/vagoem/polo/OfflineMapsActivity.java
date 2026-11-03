package com.vagoem.polo;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StatFs;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import android.view.View;
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
 private static final long TILE_LIMIT=3000L;
 private static final long BYTE_LIMIT=50L*1024L*1024L;
 private LinearLayout root,installedList;
 private TextView status,storage;
 private ProgressBar progress;
 private OfflineManager manager;
 private boolean downloading=false;
 private static final int REQUEST_ROUTE_TILES=811;
 private static final class Pack {
  final String id,name,description;final double south,west,north,east,min,max;
  Pack(String i,String n,String d,double s,double w,double no,double e,double mi,double ma){id=i;name=n;description=d;south=s;west=w;north=no;east=e;min=mi;max=ma;}
 }
 private final Pack[] packs={
  new Pack("durango-overview","Durango · estatal","Vista general · carreteras principales",22.3,-107.2,26.8,-102.4,4,7),
  new Pack("coahuila-overview","Coahuila · estatal","Vista general · carreteras principales",24.5,-103.9,29.9,-99.8,4,7),
  new Pack("laguna-detail","La Laguna · detalle","Torreón, Gómez Palacio, Lerdo y alrededores",25.25,-103.75,26.15,-102.65,7,13),
  new Pack("durango-city","Durango capital · detalle","Zona urbana y accesos",23.75,-104.85,24.15,-104.45,7,13),
  new Pack("saltillo-city","Saltillo · detalle","Zona urbana y accesos",25.30,-101.15,25.60,-100.75,7,13)
 };
 @Override public void onCreate(Bundle b){
  super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);
  manager=OfflineManager.getInstance(this);manager.setOfflineMapboxTileCountLimit(TILE_LIMIT);
  build();refreshStorage();listRegions();
 }
 private TextView label(String s,int z,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setPadding(3,6,3,6);return t;}
 private void card(View v){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(14,10,14,10);box.setBackgroundColor(0xFF18212D);box.addView(v);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,6,0,6);root.addView(box,p);}
 private void build(){
  ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(18,14,18,22);root.setBackgroundColor(0xFF0B111A);sc.addView(root);
  root.addView(label("VAG MAP MANAGER",24,Color.WHITE,true));
  root.addView(label("Cartografía offline · Durango y Coahuila",14,0xFF9BA8B7,false));
  status=label("Elige una región. Descarga con Wi-Fi y conserva el paquete en el estéreo.",13,0xFFFF6673,true);card(status);
  progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setProgress(0);card(progress);
  storage=label("",12,0xFFD7DEE8,false);card(storage);
  root.addView(label("PAQUETES REGIONALES",16,Color.WHITE,true));
  for(Pack p:packs){
   LinearLayout line=new LinearLayout(this);line.setOrientation(LinearLayout.VERTICAL);
   line.addView(label(p.name,14,Color.WHITE,true));
   line.addView(label(p.description+"\nZoom "+(int)p.min+"–"+(int)p.max+" · máximo "+TILE_LIMIT+" teselas / 50 MB por descarga",11,0xFF9BA8B7,false));
   Button d=new Button(this);d.setText("DESCARGAR REGIÓN");d.setOnClickListener(v->download(p));line.addView(d);
   card(line);
  }
  root.addView(label("MAPAS INSTALADOS",16,Color.WHITE,true));
  installedList=new LinearLayout(this);installedList.setOrientation(1);card(installedList);
  button("IMPORTAR PAQUETE DE RUTAS VALHALLA (.TAR)",this::pickRouteTiles);\n  button("ACTUALIZAR LISTA",this::listRegions);
  button("VOLVER A NAVEGACIÓN",this::finish);
  card(label("El motor y el administrador están dentro de VAG OEM. Los datos cartográficos se descargan desde esta pantalla y se guardan en la base offline de MapLibre. La descarga inicial requiere conexión. OpenFreeMap permite cada descarga de hasta 3,000 teselas o 50 MB, con máximo 4 solicitudes simultáneas y User-Agent identificable. La app identifica sus solicitudes, limita a 4 conexiones simultáneas y pausa al alcanzar 3,000 teselas o 50 MB. El tamaño se comprueba durante la descarga; puede existir una pequeña variación por respuestas que ya estaban en tránsito.",11,0xFFFFC4C4,false));
  card(label("Atribución: OpenFreeMap © OpenMapTiles · Datos de OpenStreetMap (ODbL).",10,0xFFD7DEE8,false));
  setContentView(sc);
 }
 private void button(String title,Runnable action){Button b=new Button(this);b.setText(title);b.setOnClickListener(v->action.run());root.addView(b,new LinearLayout.LayoutParams(-1,48));}
 private void refreshStorage(){StatFs fs=new StatFs(getFilesDir().getAbsolutePath());storage.setText(String.format(Locale.US,"Espacio libre en almacenamiento de la app: %.2f GB",fs.getAvailableBytes()/1e9));}
 private void download(Pack p){
  if(downloading){status.setText("Ya hay una descarga activa. Espera a que termine o pausa la región desde el sistema.");return;}
  StatFs fs=new StatFs(getFilesDir().getAbsolutePath());
  if(fs.getAvailableBytes()<250L*1024L*1024L){status.setText("Espacio insuficiente: libera al menos 250 MB antes de descargar.");return;}
  manager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback(){
   @Override public void onList(OfflineRegion[] regions){
    if(regions!=null)for(OfflineRegion region:regions){try{JSONObject meta=new JSONObject(new String(region.getMetadata(),StandardCharsets.UTF_8));if(p.id.equals(meta.optString("id"))){status.setText("Este paquete ya existe. Revisa MAPAS INSTALADOS.");return;}}catch(Exception ignored){}}
    startDownload(p);
   }
   @Override public void onError(String error){runOnUiThread(()->status.setText("No se pudo revisar la biblioteca offline: "+error));}
  });
 }
 private void startDownload(Pack p){
  downloading=true;status.setText("Preparando "+p.name+"…");progress.setProgress(0);
  LatLngBounds bounds=new LatLngBounds.Builder().include(new LatLng(p.north,p.east)).include(new LatLng(p.south,p.west)).build();
  OfflineTilePyramidRegionDefinition def=new OfflineTilePyramidRegionDefinition(STYLE,bounds,p.min,p.max,1.0f,false);
  try{
   JSONObject meta=new JSONObject();meta.put("id",p.id);meta.put("name",p.name);meta.put("provider","OpenFreeMap");meta.put("style",STYLE);
   manager.createOfflineRegion(def,meta.toString().getBytes(StandardCharsets.UTF_8),new OfflineManager.CreateOfflineRegionCallback(){
    @Override public void onCreate(OfflineRegion region){
     region.setDeliverInactiveMessages(true);
     region.setObserver(new OfflineRegion.OfflineRegionObserver(){
      @Override public void onStatusChanged(OfflineRegionStatus s){
       long count=s.getCompletedTileCount();long bytes=s.getCompletedResourceSize();long required=s.getRequiredResourceCount();
       int pct=required>0?(int)Math.min(100,100*s.getCompletedResourceCount()/required):0;
       runOnUiThread(()->{progress.setProgress(pct);status.setText(String.format(Locale.US,"%s · %d teselas · %.1f MB · %d%%",p.name,count,bytes/1048576.0,pct));});
       if(count>TILE_LIMIT||bytes>BYTE_LIMIT||(!s.isComplete()&&(count>=TILE_LIMIT||bytes>=BYTE_LIMIT))){downloading=false;region.delete(new OfflineRegion.OfflineRegionDeleteCallback(){public void onDelete(){runOnUiThread(()->{status.setText("Se eliminó la descarga porque alcanzó el límite permitido por el proveedor.");listRegions();refreshStorage();});}public void onError(String error){runOnUiThread(()->status.setText("Límite alcanzado; no se pudo limpiar el paquete: "+error));}});}
       else if(s.isComplete()){region.setDownloadState(OfflineRegion.STATE_INACTIVE);downloading=false;runOnUiThread(()->{status.setText("Región descargada y guardada para uso offline: "+p.name);listRegions();refreshStorage();});}
      }
      @Override public void onError(OfflineRegionError e){downloading=false;runOnUiThread(()->status.setText("Error de descarga: "+e.toString()));}
      @Override public void mapboxTileCountLimitExceeded(long limit){region.setDownloadState(OfflineRegion.STATE_INACTIVE);downloading=false;runOnUiThread(()->status.setText("MapLibre alcanzó el límite de "+limit+" teselas offline."));}
     });
     region.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }
    @Override public void onError(String error){downloading=false;status.setText("No se pudo crear el paquete: "+error);}
   });
  }catch(Exception e){downloading=false;status.setText("No se pudo preparar la descarga: "+e.toString());}
 }
 private void pickRouteTiles(){
  Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,REQUEST_ROUTE_TILES);
 }
 @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
  super.onActivityResult(requestCode,resultCode,data);
  if(requestCode!=REQUEST_ROUTE_TILES||resultCode!=RESULT_OK||data==null||data.getData()==null)return;
  Uri uri=data.getData();status.setText("Importando paquete de red vial…");
  new Thread(()->{
   File target=new File(getFilesDir(),"vag_valhalla_tiles.tar");long copied=0;
   try(InputStream in=getContentResolver().openInputStream(uri);FileOutputStream out=new FileOutputStream(target)){
    if(in==null)throw new IllegalStateException("No se pudo abrir el archivo seleccionado.");
    byte[] buffer=new byte[1024*1024];int n;while((n=in.read(buffer))!=-1){copied+=n;if(copied>2L*1024L*1024L*1024L)throw new IllegalStateException("El paquete supera el límite de 2 GB.");out.write(buffer,0,n);}out.flush();
    if(copied<1024L*1024L)throw new IllegalStateException("El archivo es demasiado pequeño para un extracto Valhalla.");
    runOnUiThread(()->{status.setText(String.format(Locale.US,"Paquete de rutas importado · %.1f MB. Valhalla está listo para abrir la red vial.",copied/1048576.0));refreshStorage();});
   }catch(Exception e){temp.delete();runOnUiThread(()->status.setText("No se pudo importar el paquete: "+e.getMessage()));}
  },"VAG-RoutePack-Import").start();
 }
 private void listRegions(){
  if(installedList==null)return;installedList.removeAllViews();
  manager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback(){
   @Override public void onList(OfflineRegion[] regions){runOnUiThread(()->{
    installedList.removeAllViews();
    if(regions==null||regions.length==0){installedList.addView(label("Todavía no hay paquetes descargados.",11,0xFF9BA8B7,false));return;}
    for(OfflineRegion region:regions){
     String name="Región offline";
     try{name=new JSONObject(new String(region.getMetadata(),StandardCharsets.UTF_8)).optString("name",name);}catch(Exception ignored){}
     LinearLayout row=new LinearLayout(OfflineMapsActivity.this);row.setOrientation(LinearLayout.VERTICAL);
     row.addView(label(regionName,12,Color.WHITE,true));
     region.getStatus(new OfflineRegion.OfflineRegionStatusCallback(){
      @Override public void onStatus(OfflineRegionStatus s){runOnUiThread(()->row.addView(label((s.isComplete()?"Disponible sin conexión":"Descarga parcial")+" · "+s.getCompletedTileCount()+" teselas · "+String.format(Locale.US,"%.1f MB",s.getCompletedResourceSize()/1048576.0),10,0xFF9BA8B7,false)));}
      @Override public void onError(String error){runOnUiThread(()->row.addView(label("Estado no disponible: "+error,10,0xFFFFA0A0,false)));}
     });
     Button del=new Button(OfflineMapsActivity.this);del.setText("ELIMINAR PAQUETE");del.setOnClickListener(v->new AlertDialog.Builder(OfflineMapsActivity.this).setTitle("Eliminar "+regionName).setMessage("Se borrará este paquete offline del estéreo.").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(d,w)->region.delete(new OfflineRegion.OfflineRegionDeleteCallback(){
      @Override public void onDelete(){runOnUiThread(()->{status.setText("Paquete eliminado.");listRegions();refreshStorage();});}
      @Override public void onError(String error){runOnUiThread(()->status.setText("No se pudo eliminar: "+error));}
     })).show());row.addView(del);installedList.addView(row);
    }
   });}
   @Override public void onError(String error){runOnUiThread(()->status.setText("No se pudo consultar mapas offline: "+error));}
  });
 }
}
