package com.vagoem.polo;

import android.content.Context;
import com.valhalla.config.ValhallaConfigBuilder;
import com.valhalla.valhalla.Valhalla;
import java.io.File;

/** Offline Valhalla actor. One native actor is reused for all route requests. */
public final class OfflineRoutingEngine implements AutoCloseable {
 private final Context context;
 private Valhalla engine;
 private File tileFile;
 public OfflineRoutingEngine(Context c){context=c.getApplicationContext();tileFile=new File(context.getFilesDir(),"vag_valhalla_tiles.tar");}
 public boolean hasRegionalTiles(){return tileFile.isFile()&&tileFile.length()>1024*1024;}
 public synchronized String route(double fromLat,double fromLon,double toLat,double toLon) throws Exception {
  if(!hasRegionalTiles())throw new IllegalStateException("No está instalado el paquete de red vial offline de Durango/Coahuila.");
  if(engine==null){ValhallaConfigBuilder builder=new ValhallaConfigBuilder();engine=new Valhalla(context,builder.withTileExtract(tileFile.getAbsolutePath()).build());}
  String request="{\"locations\":[{\"lat\":"+fromLat+",\"lon\":"+fromLon+"},{\"lat\":"+toLat+",\"lon\":"+toLon+"}],\"costing\":\"auto\",\"units\":\"kilometers\",\"directions_options\":{\"units\":\"kilometers\",\"language\":\"es-ES\"}}";
  return engine.routeRaw(request);
 }
 @Override public synchronized void close(){if(engine!=null){engine.close();engine=null;}}
}
