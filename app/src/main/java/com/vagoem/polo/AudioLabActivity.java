package com.vagoem.polo;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.AutomaticGainControl;
import android.media.audiofx.NoiseSuppressor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.*;
import android.view.View;
import android.view.ViewGroup;

public class AudioLabActivity extends Activity {
 private static final int REQ_MIC=410, RATE=16000;
 private AudioRecord recorder; private volatile boolean recording;
 private final Handler handler=new Handler(Looper.getMainLooper());
 private ProgressBar meter; private TextView status,level,effects,storage; private Button start;
 private long started;
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);build();refreshStorage();refreshEffects();}
 private TextView text(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setPadding(3,7,3,7);return t;}
 private void card(LinearLayout root,View v){LinearLayout box=new LinearLayout(this);box.setPadding(16,12,16,12);box.setBackgroundColor(0xFF18212D);box.addView(v);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,10,0,10);root.addView(box,p);}
 private void build(){ScrollView sc=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,18,24,24);root.setBackgroundColor(0xFF0B111A);sc.addView(root);
 root.addView(text("AUDIO LAB",25,0xFFF3F6FA,true));root.addView(text("Diagnóstico de micrófono · WV232 / Android 10",13,0xFF9BA8B7,false));
 status=text("Estado: listo para probar",17,0xFFFFFFFF,true);card(root,status);
 card(root,text("Entrada: fuente MIC predeterminada de Android. La selección física depende de la MCU.",13,0xFFD7DEE8,false));
 meter=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);meter.setMax(100);root.addView(meter,new LinearLayout.LayoutParams(-1,24));
 level=text("Nivel de entrada: —",14,0xFFFFFFFF,true);root.addView(level);
 start=new Button(this);start.setText("INICIAR PRUEBA DE MICRÓFONO");start.setOnClickListener(v->{if(recording)stopTest();else requestStart();});root.addView(start,new LinearLayout.LayoutParams(-1,58));
 effects=text("",13,0xFFD7DEE8,false);card(root,effects);storage=text("",14,0xFFD7DEE8,false);card(root,storage);
 Button st=new Button(this);st.setText("ABRIR ALMACENAMIENTO ANDROID");st.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));}catch(Exception e){startActivity(new Intent(Settings.ACTION_SETTINGS));}});root.addView(st,new LinearLayout.LayoutParams(-1,52));
 Button snd=new Button(this);snd.setText("AJUSTES DE SONIDO");snd.setOnClickListener(v->{try{startActivity(new Intent(Settings.ACTION_SOUND_SETTINGS));}catch(Exception e){}});root.addView(snd,new LinearLayout.LayoutParams(-1,52));
 card(root,text("Esta prueba mide la entrada disponible para esta app. No modifica la ganancia de llamadas Bluetooth ni CPLink. Las llamadas pueden usar otra ruta de audio. No se guarda ni transmite audio.",12,0xFFFFB9B9,false));
 Button back=new Button(this);back.setText("VOLVER A VAG OEM");back.setOnClickListener(v->finish());root.addView(back,new LinearLayout.LayoutParams(-1,52));setContentView(sc);}
 private void refreshEffects(){effects.setText("EFECTOS REPORTADOS POR ANDROID\nAGC: "+avail(AutomaticGainControl.isAvailable())+"\nAEC: "+avail(AcousticEchoCanceler.isAvailable())+"\nReducción de ruido: "+avail(NoiseSuppressor.isAvailable())+"\nDisponibilidad no significa que afecten llamadas del sistema o CPLink.");}
 private String avail(boolean b){return b?"disponible para sesiones compatibles":"no disponible";}
 private void refreshStorage(){android.os.StatFs fs=new android.os.StatFs(getFilesDir().getAbsolutePath());double gb=fs.getAvailableBytes()/1073741824.0;storage.setText(String.format(java.util.Locale.US,"ALMACENAMIENTO DISPONIBLE EN EL VOLUMEN DE LA APP\n%.2f GB libres.\nPolítica: no descargar mapas con menos de 2 GB libres; reservar espacio para Android y aplicaciones originales.",gb));}
 private void requestStart(){if(Build.VERSION.SDK_INT>=23&&checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO},REQ_MIC);return;}startTest();}
 @Override public void onRequestPermissionsResult(int r,String[] p,int[] g){super.onRequestPermissionsResult(r,p,g);if(r==REQ_MIC&&g.length>0&&g[0]==PackageManager.PERMISSION_GRANTED)startTest();else status.setText("Permiso denegado; no se inició la prueba.");}
 private void startTest(){int min=AudioRecord.getMinBufferSize(RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT);if(min<=0){status.setText("Configuración de audio no admitida.");return;}try{recorder=new AudioRecord(MediaRecorder.AudioSource.MIC,RATE,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,Math.max(min,RATE/2));if(recorder.getState()!=AudioRecord.STATE_INITIALIZED)throw new IllegalStateException("AudioRecord no inicializado");recorder.startRecording();recording=true;started=System.currentTimeMillis();start.setText("DETENER PRUEBA");status.setText("Prueba activa · habla a volumen normal");Thread t=new Thread(()->readLoop(),"VAGOEM-AudioProbe");t.start();}catch(Exception e){status.setText("No se pudo iniciar: "+e.getMessage());release();}}
 private void readLoop(){short[] b=new short[2048];while(recording&&recorder!=null){int n=recorder.read(b,0,b.length);if(n>0){double sum=0;int peak=0;for(int i=0;i<n;i++){int v=b[i];sum+=(double)v*v;peak=Math.max(peak,Math.abs(v));}double rms=Math.sqrt(sum/n);int db=rms>0?(int)Math.max(0,Math.min(100,20*Math.log10(rms/32768.0)+100)):0;int pk=peak*100/32768;long sec=(System.currentTimeMillis()-started)/1000;handler.post(()->{if(recording){meter.setProgress(db);level.setText("Nivel RMS: "+db+"% · Pico: "+pk+"% · Tiempo: "+sec+" s");}});}else if(n<0)break;}}
 private void stopTest(){recording=false;if(recorder!=null)try{recorder.stop();}catch(Exception e){}release();start.setText("INICIAR PRUEBA DE MICRÓFONO");status.setText("Prueba detenida. Compara resultados con cada configuración física disponible.");meter.setProgress(0);level.setText("Nivel de entrada: —");}
 private void release(){if(recorder!=null){try{recorder.release();}catch(Exception e){}recorder=null;}}
 @Override protected void onPause(){if(recording)stopTest();super.onPause();}
}