package com.vagoem.polo;

import android.content.Context;
import com.valhalla.config.ValhallaConfigBuilder;
import com.valhalla.valhalla.Valhalla;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** Offline Valhalla actor. One native actor is reused for all route requests. */
public final class OfflineRoutingEngine implements AutoCloseable {
 private final Context context;
 private Valhalla engine;
 private final File tileFile;
 public OfflineRoutingEngine(Context c){context=c.getApplicationContext();tileFile=new File(context.getFilesDir(),"vag_valhalla_tiles.tar");}
 public boolean hasRegionalTiles(){
  if(tileFile.isFile()&&tileFile.length()>1024*1024)return true;
  try(InputStream in=context.getAssets().open("vag_valhalla_tiles.tar")){return in!=null;}catch(Exception ignored){return false;}
 }
 private void ensureBundledTiles() throws Exception {
  if(tileFile.isFile()&&tileFile.length()>1024*1024)return;
  File temp=new File(context.getFilesDir(),"vag_valhalla_tiles.tar.tmp");
  try(InputStream in=context.getAssets().open("vag_valhalla_tiles.tar");FileOutputStream out=new FileOutputStream(temp)){
   byte[] buffer=new byte[1024*1024];int n;while((n=in.read(buffer))!=-1)out.write(buffer,0,n);out.flush();
  }
  if(tileFile.exists()&&!tileFile.delete())throw new IllegalStateException("No se pudo reemplazar el grafo offline anterior.");
  if(!temp.renameTo(tileFile))throw new IllegalStateException("No se pudo instalar el grafo offline incluido en la APK.");
 }
 public synchronized String route(double fromLat,double fromLon,double toLat,double toLon) throws Exception {
  if(!hasRegionalTiles())throw new IllegalStateException("No está instalado el paquete de red vial offline de Durango/Coahuila.");
  ensureBundledTiles();
  if(engine==null){ValhallaConfigBuilder builder=new ValhallaConfigBuilder();engine=new Valhalla(context,builder.withTileExtract(tileFile.getAbsolutePath()).build());}
  String request="{\"locations\":[{\"lat\":"+fromLat+",\"lon\":"+fromLon+"},{\"lat\":"+toLat+",\"lon\":"+toLon+"}],\"costing\":\"auto\",\"units\":\"kilometers\",\"directions_options\":{\"units\":\"kilometers\",\"language\":\"es-ES\"}}";
  return engine.routeRaw(request);
 }
 @Override public synchronized void close(){if(engine!=null){engine.close();engine=null;}}
}
