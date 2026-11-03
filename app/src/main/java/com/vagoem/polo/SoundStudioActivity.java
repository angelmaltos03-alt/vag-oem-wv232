package com.vagoem.polo;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.audiofx.DynamicsProcessing;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.*;
import java.util.*;

public class SoundStudioActivity extends Activity {
 private static final String PREF="vag_sound_profiles";
 private LinearLayout root;
 private TextView report,profileStatus;
 private SeekBar[] bands=new SeekBar[10];
 private final String[] hz={"31","62","125","250","500","1k","2k","4k","8k","16k"};
 private final int[][] presets={{2,3,2,0,-1,0,2,3,2,1},{3,2,0,-1,1,2,3,2,1,0},{4,3,1,0,-1,0,2,3,3,2},{0,0,1,2,2,2,1,0,0,0}};
 private String current="Personalizado";
 @Override public void onCreate(Bundle b){super.onCreate(b);getWindow().getDecorView().setSystemUiVisibility(5894|1024|512);build();loadCustom();refreshReport();}
 private TextView label(String s,int size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);if(bold)t.setTypeface(null,1);t.setPadding(2,8,2,8);return t;}
 private void card(View v){LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(16,12,16,12);box.setBackgroundColor(0xFF18212D);box.addView(v);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,8,0,8);root.addView(box,p);}
 private Button button(String s,Runnable action){Button b=new Button(this);b.setText(s);b.setOnClickListener(v->action.run());root.addView(b,new LinearLayout.LayoutParams(-1,50));return b;}
 private void build(){
  ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(20,16,20,24);root.setBackgroundColor(0xFF0B111A);scroll.addView(root);
  root.addView(label("VAG SOUND STUDIO",25,Color.WHITE,true));
  root.addView(label("Perfiles acústicos · diagnóstico del WV232 · Android 10",13,0xFF9BA8B7,false));
  profileStatus=label("Perfil: Personalizado",16,0xFFFF6673,true);card(profileStatus);
  LinearLayout row=new LinearLayout(this);row.setOrientation(0);
  String[] names={"OEM","Pop","Rock","Voz"};
  for(int i=0;i<names.length;i++){final int k=i;Button b=new Button(this);b.setText(names[i]);row.addView(b,new LinearLayout.LayoutParams(0,52,1));b.setOnClickListener(v->applyPreset(k));}
  card(row);
  root.addView(label("ECUALIZADOR DE 10 BANDAS",17,Color.WHITE,true));
  for(int i=0;i<10;i++){LinearLayout line=new LinearLayout(this);line.setGravity(android.view.Gravity.CENTER_VERTICAL);TextView f=label(hz[i]+" Hz",12,0xFFD7DEE8,true);f.setWidth(58);line.addView(f);SeekBar sb=new SeekBar(this);sb.setMax(12);sb.setProgress(6);bands[i]=sb;line.addView(sb,new LinearLayout.LayoutParams(0,42,1));TextView val=label("0 dB",11,0xFFFF6673,true);val.setWidth(48);line.addView(val);final int idx=i;sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean u){val.setText((p-6>0?"+":"")+(p-6)+" dB");if(u){current="Personalizado";profileStatus.setText("Perfil: "+current+" · sin aplicar a otras apps");saveCustom();}}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}});root.addView(line);}
  button("GUARDAR PERFIL PERSONALIZADO",()->{saveCustom();Toast.makeText(this,"Perfil guardado en VAG OEM",Toast.LENGTH_SHORT).show();});
  report=label("",12,0xFFD7DEE8,false);card(report);
  button("ABRIR DIAGNÓSTICO DE MICRÓFONO",()->startActivity(new android.content.Intent(this,AudioLabActivity.class)));
  button("ABRIR AJUSTES DE SONIDO ANDROID",()->{try{startActivity(new android.content.Intent(android.provider.Settings.ACTION_SOUND_SETTINGS));}catch(Exception ignored){}});
  button("VOLVER A VAG OEM",this::finish);
  card(label("IMPORTANTE: los perfiles se guardan localmente. Android 10 no garantiza que una app controle el audio global, la radio o CPLink. No aumentamos la ganancia artificialmente ni aplicamos efectos a ciegas: primero identificamos el DSP y la ruta de audio del fabricante.",12,0xFFFFC4C4,false));
  setContentView(scroll);
 }
 private void applyPreset(int k){int[] p=presets[k];for(int i=0;i<10;i++)bands[i].setProgress((p[i]+6));current=new String[]{"OEM equilibrado","Pop","Rock","Voz"}[k];profileStatus.setText("Perfil: "+current+" · guardado en VAG OEM");saveCustom();}
 private void saveCustom(){android.content.SharedPreferences.Editor e=getSharedPreferences(PREF,0).edit().putString("name",current);for(int i=0;i<10;i++)e.putInt("b"+i,bands[i].getProgress());e.apply();}
 private void loadCustom(){android.content.SharedPreferences p=getSharedPreferences(PREF,0);current=p.getString("name","Personalizado");for(int i=0;i<10;i++)bands[i].setProgress(p.getInt("b"+i,6));profileStatus.setText("Perfil: "+current+" · local");}
 private void refreshReport(){
  StringBuilder s=new StringBuilder("DIAGNÓSTICO DEL EQUIPO\n");
  s.append("Android: ").append(Build.VERSION.RELEASE).append(" · API ").append(Build.VERSION.SDK_INT).append("\n");
  s.append("Fabricante / modelo: ").append(Build.MANUFACTURER).append(" / ").append(Build.MODEL).append("\n");
  s.append("Placa: ").append(Build.BOARD).append(" · Hardware: ").append(Build.HARDWARE).append("\n");
  s.append("Producto: ").append(Build.PRODUCT).append("\nABI: ").append(Arrays.toString(Build.SUPPORTED_ABIS)).append("\n");
  DisplayMetrics dm=getResources().getDisplayMetrics();s.append("Pantalla lógica: ").append(dm.widthPixels).append(" × ").append(dm.heightPixels).append(" · densidad ").append(dm.densityDpi).append(" dpi\n");
  ActivityManager.MemoryInfo mi=new ActivityManager.MemoryInfo();((ActivityManager)getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(mi);s.append(String.format(Locale.US,"RAM disponible ahora: %.0f MB\n",mi.availMem/1048576.0));
  StatFs fs=new StatFs(android.os.Environment.getDataDirectory().getAbsolutePath());s.append(String.format(Locale.US,"Almacenamiento /data libre: %.2f GB de %.2f GB\n",fs.getAvailableBytes()/1e9,fs.getTotalBytes()/1e9));
  s.append("Efectos Android reportados: EQ ").append(safeEq()).append(" · Loudness ").append(safeLoud()).append(" · Dynamics ").append(safeDyn()).append("\n");
  s.append("Códecs decodificadores: ").append(codecSummary()).append("\n");
  s.append("Salidas de audio detectadas: ").append(audioOutputs());
  report.setText(s.toString());
 }
 private String safeEq(){return Build.VERSION.SDK_INT>=9?"API disponible · requiere sesión de audio":"API no disponible";}
 private String safeLoud(){return Build.VERSION.SDK_INT>=19?"API disponible · requiere sesión de audio":"API no disponible";}
 private String safeDyn(){return Build.VERSION.SDK_INT>=28?"API disponible · requiere sesión de audio":"API no disponible";}
 private String codecSummary(){Set<String> found=new TreeSet<>();try{for(MediaCodecInfo c:new MediaCodecList(MediaCodecList.REGULAR_CODECS).getCodecInfos())if(!c.isEncoder())for(String t:c.getSupportedTypes()){String x=t.toLowerCase(Locale.ROOT);if(x.contains("audio/")&&(x.contains("aac")||x.contains("mpeg")||x.contains("flac")||x.contains("opus")||x.contains("vorbis")||x.contains("raw")||x.contains("ac3")||x.contains("eac3")))found.add(x);}}catch(Throwable ignored){}return found.isEmpty()?"sin datos":found.toString();}
 private String audioOutputs(){try{AudioManager am=(AudioManager)getSystemService(AUDIO_SERVICE);if(Build.VERSION.SDK_INT>=23){List<String> out=new ArrayList<>();for(AudioDeviceInfo d:am.getDevices(AudioManager.GET_DEVICES_OUTPUTS))out.add(d.getProductName()+" (tipo "+d.getType()+")");return out.isEmpty()?"no reportadas":out.toString();}return "API anterior";}catch(Throwable t){return "no consultables";}}
}
