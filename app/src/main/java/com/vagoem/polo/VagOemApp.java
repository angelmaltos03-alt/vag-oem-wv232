package com.vagoem.polo;

import android.app.Application;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.maplibre.android.module.http.HttpRequestUtil;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Configures MapLibre networking once for the whole head unit. */
public final class VagOemApp extends Application {
 @Override public void onCreate(){
  super.onCreate();
  Dispatcher dispatcher=new Dispatcher();
  dispatcher.setMaxRequests(4);
  dispatcher.setMaxRequestsPerHost(4);
  OkHttpClient client=new OkHttpClient.Builder()
   .dispatcher(dispatcher)
   .connectTimeout(20,TimeUnit.SECONDS)
   .readTimeout(35,TimeUnit.SECONDS)
   .addInterceptor(chain->{
    Request request=chain.request().newBuilder()
     .header("User-Agent","VAG-OEM-WV232/0.11.0 (Android automotive offline maps)")
     .build();
    return chain.proceed(request);
   }).build();
  HttpRequestUtil.setOkHttpClient(client);
 }
}