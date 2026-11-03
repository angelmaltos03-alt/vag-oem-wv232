package com.vagoem.polo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.HapticFeedbackConstants;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VagUiView extends View {
 public interface Actions { void onAction(String action); }
 private static final int W=1024,H=600, RED=0xFFE34A60, WHITE=0xFFF1F4F7, MUTED=0xFF9BA8B7;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final Actions actions;
 private final Drawable logo;
 private final Bitmap poloSilhouette;
 private String screen="HOME";
 private int vehicleTab=0, pressedTile=-1;
 private long pressStartedAt=0L;
 private final SimpleDateFormat clock=new SimpleDateFormat("HH:mm",Locale.getDefault());
 private final SimpleDateFormat date=new SimpleDateFormat("EEE, dd MMM",Locale.getDefault());

 public VagUiView(Context c,Actions a){
  super(c);actions=a;setFocusable(true);
  logo=getResources().getDrawable(R.drawable.volkswagen_logo);
  Bitmap loaded=BitmapFactory.decodeResource(getResources(),R.drawable.polo_silhouette_red);
  poloSilhouette=loaded;
 }
 public void setScreen(String s){screen=s==null?"HOME":s;pressedTile=-1;invalidate();}
 public String getScreen(){return screen;}

 @Override protected void onDraw(Canvas c){
  super.onDraw(c);c.save();c.scale(getWidth()/(float)W,getHeight()/(float)H);
  p.setShader(new LinearGradient(0,0,W,H,0xFF111A25,0xFF080C13,Shader.TileMode.CLAMP));c.drawRect(0,0,W,H,p);p.setShader(null);
  drawHeader(c);
  switch(screen){
   case "HOME":home(c);break;
   case "PHONE":phone(c);break;
   case "VEHICLE":vehicle(c);break;
   case "TRIP":vehicleTab=1;vehicle(c);break;
   case "APP_CONNECT":page(c,"App-Connect","Apple CarPlay mediante CPLink","ABRIR APP-CONNECT · CPLINK","OPEN_CARPLAY");break;
   case "RADIO":page(c,"Radio","FM / AM · Aplicación original","ABRIR RADIO ORIGINAL","OPEN_RADIO");break;
   case "MEDIA":page(c,"Media","Música y fuentes de audio","ABRIR REPRODUCTOR","OPEN_MEDIA");break;
   case "NAVIGATION":page(c,"Navegación","Mapa offline de La Laguna y regiones instaladas","ABRIR MAPA","OPEN_NAV");break;
   case "SETTINGS":settings(c);break;
   case "SOUND":page(c,"Sonido","Perfiles acústicos y ecualizador de 10 bandas","ABRIR SOUND STUDIO","OPEN_SOUND");break;
  }
  p.setColor(0xFF080C12);c.drawRect(0,560,W,H,p);
  p.setColor(0x553D4855);c.drawRect(0,559,W,560,p);
  txt(c,"VOLKSWAGEN  ·  POLO 2020",28,584,9,MUTED,true,Paint.Align.LEFT);
  txt(c,"APP-CONNECT  ·  CPLINK",995,584,9,MUTED,true,Paint.Align.RIGHT);
  c.restore();
 }
 private void drawHeader(Canvas c){
  p.setColor(0xFF151E29);c.drawRect(0,0,W,80,p);
  p.setColor(0xFF303C49);c.drawRect(0,78,W,80,p);
  if(logo!=null){logo.setBounds(23,12,72,64);logo.draw(c);}
  txt(c,"POLO",89,34,21,WHITE,true,Paint.Align.LEFT);
  txt(c,"2020",145,34,21,RED,true,Paint.Align.LEFT);
  txt(c,"B I E N V E N I D O",89,55,9,0xFFB5BFCA,true,Paint.Align.LEFT);
  txt(c,clock.format(new Date()),512,34,27,WHITE,false,Paint.Align.CENTER);
  txt(c,date.format(new Date()).toUpperCase(Locale.getDefault()),512,55,9,MUTED,true,Paint.Align.CENTER);
  txt(c,"GPS     BT     Wi-Fi     AUDIO",995,39,10,WHITE,true,Paint.Align.RIGHT);
  p.setColor(0xFF63303B);c.drawRoundRect(new RectF(465,77,559,80),2,2,p);
  p.setColor(RED);c.drawRoundRect(new RectF(477,77,547,80),2,2,p);
 }
 private void home(Canvas c){
  String[] names={"Radio","Media","Teléfono","App-Connect","Navegación","Vehículo","Ajustes","Sonido"};
  String[] subs={"FM / AM","Música","Bluetooth","CPLink","GPS offline","Datos del vehículo","Sistema","Ecualizador"};
  String[] glyphs={"◉","♫","☎","▣","➤","▱","⚙","▥"};
  for(int i=0;i<8;i++){
   int col=i%4,row=i/4;float x=38+col*242,y=119+row*179;
   drawTile(c,x,y,226,158,i==pressedTile);
   txt(c,glyphs[i],x+113,y+63,39,WHITE,false,Paint.Align.CENTER);
   txt(c,names[i],x+113,y+106,16,WHITE,true,Paint.Align.CENTER);
   txt(c,subs[i],x+113,y+130,10,i==3?0xFFEFA0AA:MUTED,false,Paint.Align.CENTER);
   softAccent(c,x+18,y+153,x+208,y+156,i==pressedTile);
  }
  txt(c,"MANTÉN PRESIONADO UN ACCESO PARA ASIGNAR SU APP",512,528,8,0xFFE58B96,true,Paint.Align.CENTER);
 }
 private void drawTile(Canvas c,float x,float y,float w,float h,boolean active){
  if(active){p.setColor(0x28FF405B);c.drawRoundRect(new RectF(x-4,y-4,x+w+4,y+h+4),13,13,p);}
  p.setShader(new LinearGradient(x,y,x,y+h,active?0xFF293441:0xFF202B37,0xFF111923,Shader.TileMode.CLAMP));
  c.drawRoundRect(new RectF(x,y,x+w,y+h),9,9,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(active?1.8f:1.0f);p.setColor(active?0xFFB34A5A:0xFF465362);
  c.drawRoundRect(new RectF(x,y,x+w,y+h),9,9,p);p.setStyle(Paint.Style.FILL);
 }
 private void softAccent(Canvas c,float l,float t,float r,float b,boolean active){
  p.setColor(active?0x55F0445C:0x228F2638);c.drawRoundRect(new RectF(l-2,t-2,r+2,b+2),3,3,p);
  p.setColor(active?0xFFE85A6D:0xFF9A3546);c.drawRoundRect(new RectF(l,t,r,b),2,2,p);
 }
 private void phone(Canvas c){
  title(c,"Teléfono","Bluetooth · manos libres");
  txt(c,"Seleccione una función",512,177,12,MUTED,false,Paint.Align.CENTER);
  String[] names={"Teclado","Llamadas recientes","Contactos","Favoritos"};
  String[] desc={"Marcar un número","Historial de llamadas","Agenda del teléfono","Contactos favoritos"};
  String[] glyphs={"⌗","◷","☷","★"};
  String[] actions={"OPEN_PHONE","OPEN_CALL_LOG","OPEN_CONTACTS","OPEN_FAVORITES"};
  for(int i=0;i<4;i++){
   int col=i%2,row=i/2;float x=115+col*405,y=200+row*145;
   drawTile(c,x,y,365,118,pressedTile==i);
   txt(c,glyphs[i],x+58,y+54,32,WHITE,false,Paint.Align.CENTER);
   txt(c,names[i],x+91,y+48,15,WHITE,true,Paint.Align.LEFT);
   txt(c,desc[i],x+91,y+73,10,MUTED,false,Paint.Align.LEFT);
   softAccent(c,x+18,y+112,x+347,y+115,pressedTile==i);
  }
  txt(c,"Las llamadas se realizan desde la aplicación telefónica del equipo.",512,520,9,MUTED,false,Paint.Align.CENTER);
 }
 private void vehicle(Canvas c){
  title(c,"Vehículo","Información del vehículo · CANBUS / OBD-II");
  String[] tabs={"GENERAL","VIAJE","MOTOR","ESTADOS"};
  for(int i=0;i<4;i++){
   float x=36+i*240;
   boolean active=vehicleTab==i;
   p.setColor(active?0xFF3A2029:0xFF18212B);c.drawRoundRect(new RectF(x,145,x+228,179),5,5,p);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(active?0xFFB64B5C:0xFF3E4A57);c.drawRoundRect(new RectF(x,145,x+228,179),5,5,p);p.setStyle(Paint.Style.FILL);
   txt(c,tabs[i],x+114,167,10,active?0xFFFF8794:0xFFB9C3CE,true,Paint.Align.CENTER);
   if(active){p.setColor(RED);c.drawRoundRect(new RectF(x+18,176,x+210,179),2,2,p);}
  }
  panel(c,35,191,438,329);
  if(poloSilhouette!=null){
   RectF dst=fit(poloSilhouette,52,218,404,247);
   p.setAlpha(255);c.drawBitmap(poloSilhouette,null,dst,p);p.setAlpha(255);
  }
  txt(c,"VOLKSWAGEN POLO 2020  ·  VISTAS DE REFERENCIA",254,497,8,0xFF8F9BA9,true,Paint.Align.CENTER);
  panel(c,493,191,496,329);
  String[] labels,units;
  if(vehicleTab==0){
   labels=new String[]{"NIVEL DE GASOLINA","VOLUMEN ESTIMADO","AUTONOMÍA","CONSUMO ACTUAL","CONSUMO PROMEDIO","CONSUMO PROMEDIO"};
   units=new String[]{"%","L","km","km/L","km/L","L/100 km"};
  }else if(vehicleTab==1){
   labels=new String[]{"DISTANCIA VIAJE A","DISTANCIA VIAJE B","TIEMPO DE VIAJE","VELOCIDAD PROMEDIO","VELOCIDAD MÁXIMA","COMBUSTIBLE USADO"};
   units=new String[]{"km","km","h:min","km/h","km/h","L"};
  }else if(vehicleTab==2){
   labels=new String[]{"VELOCIDAD ACTUAL","RÉGIMEN DEL MOTOR","TEMP. REFRIGERANTE","CARGA DEL MOTOR","POSICIÓN ACELERADOR","VOLTAJE DE BATERÍA"};
   units=new String[]{"km/h","rpm","°C","%","%","V"};
  }else{
   labels=new String[]{"PUERTA CONDUCTOR","PUERTA COPILOTO","PUERTAS TRASERAS","CAJUELA","FRENO DE MANO","CINTURÓN / LUCES"};
   units=new String[]{"estado","estado","estado","estado","estado","estado"};
  }
  for(int i=0;i<6;i++){
   int col=i%2,row=i/2;float x=506+col*239,y=201+row*101;
   metric(c,x,y,224,91,labels[i],units[i]);
  }
  txt(c,"CANBUS SIN CONEXIÓN  ·  LECTURAS REALES PENDIENTES DEL DECODIFICADOR",741,540,8,0xFFE58B96,true,Paint.Align.CENTER);
 }
 private RectF fit(Bitmap b,float x,float y,float w,float h){
  float scale=Math.min(w/b.getWidth(),h/b.getHeight());float dw=b.getWidth()*scale,dh=b.getHeight()*scale;
  float left=x+(w-dw)/2f,top=y+(h-dh)/2f;return new RectF(left,top,left+dw,top+dh);
 }
 private void metric(Canvas c,float x,float y,float w,float h,String label,String unit){
  panel(c,x,y,w,h);
  txt(c,label,x+11,y+19,8,0xFFAEB9C5,true,Paint.Align.LEFT);
  txt(c,"— —",x+11,y+55,23,WHITE,true,Paint.Align.LEFT);
  txt(c,unit,x+w-11,y+54,9,0xFFE26A79,true,Paint.Align.RIGHT);
  txt(c,"SIN LECTURA CANBUS",x+11,y+76,7,0xFF718091,false,Paint.Align.LEFT);
 }
 private void page(Canvas c,String title,String sub,String button,String action){
  title(c,title,sub);panel(c,82,149,860,355);
  txt(c,title.toUpperCase(Locale.ROOT),512,282,27,WHITE,true,Paint.Align.CENTER);
  txt(c,sub,512,318,12,MUTED,false,Paint.Align.CENTER);
  button(c,350,380,324,58,button);
  txt(c,"‹ INICIO",50,539,9,RED,true,Paint.Align.LEFT);
 }
 private void settings(Canvas c){
  title(c,"Ajustes","Aplicaciones originales y configuración de VAG OEM");
  String[] a={"Radio FM / AM","Reproductor de música","Teléfono / Bluetooth","App-Connect · CPLink","Navegación offline","CANBUS / Vehículo","Ajustes Android"};
  String[] actions={"PICK_RADIO","PICK_MUSIC","PICK_PHONE","PICK_CARPLAY","PICK_NAV","PICK_CANBUS","ANDROID_SETTINGS"};
  for(int i=0;i<a.length;i++){
   int col=i%2,row=i/2;float x=72+col*448,y=148+row*91;
   panel(c,x,y,420,72);
   txt(c,a[i],x+18,y+31,12,WHITE,true,Paint.Align.LEFT);
   txt(c,"CONFIGURAR  ›",x+402,y+51,8,RED,true,Paint.Align.RIGHT);
  }
  txt(c,"‹ INICIO",50,539,9,RED,true,Paint.Align.LEFT);
 }
 private void title(Canvas c,String t,String s){
  txt(c,"‹",43,119,26,RED,false,Paint.Align.LEFT);
  txt(c,t,82,110,22,WHITE,true,Paint.Align.LEFT);
  txt(c,s,83,132,9,MUTED,false,Paint.Align.LEFT);
 }
 private void panel(Canvas c,float x,float y,float w,float h){
  p.setShader(new LinearGradient(x,y,x+w,y+h,0xFF1C2631,0xFF111821,Shader.TileMode.CLAMP));
  c.drawRoundRect(new RectF(x,y,x+w,y+h),7,7,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xFF394654);
  c.drawRoundRect(new RectF(x,y,x+w,y+h),7,7,p);p.setStyle(Paint.Style.FILL);
 }
 private void button(Canvas c,float x,float y,float w,float h,String s){
  p.setColor(0xFF321A22);c.drawRoundRect(new RectF(x,y,x+w,y+h),6,6,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xFFC44B5E);
  c.drawRoundRect(new RectF(x,y,x+w,y+h),6,6,p);p.setStyle(Paint.Style.FILL);
  txt(c,s,x+w/2,y+h/2+4,10,WHITE,true,Paint.Align.CENTER);
 }
 private void txt(Canvas c,String s,float x,float y,float z,int color,boolean bold,Paint.Align align){
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setAlpha(255);p.setColor(color);p.setTextSize(z);
  p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));p.setTextAlign(align);c.drawText(s,x,y,p);
 }
 @Override public boolean onTouchEvent(MotionEvent e){
  float x=e.getX()*W/Math.max(1,getWidth()),y=e.getY()*H/Math.max(1,getHeight());
  if(e.getAction()==MotionEvent.ACTION_DOWN){
   pressedTile=hitTile(x,y);pressStartedAt=android.os.SystemClock.uptimeMillis();invalidate();return true;
  }
  if(e.getAction()==MotionEvent.ACTION_UP){
   int down=pressedTile;long held=android.os.SystemClock.uptimeMillis()-pressStartedAt;
   pressedTile=-1;invalidate();
   if("HOME".equals(screen)&&down>=0&&held>=600L&&down==hitTile(x,y)){
    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);longPressHomeTile(down);performClick();return true;
   }
   tap(x,y,down);performClick();return true;
  }
  if(e.getAction()==MotionEvent.ACTION_CANCEL){pressedTile=-1;pressStartedAt=0L;invalidate();return true;}
  return true;
 }
 private void longPressHomeTile(int tile){
  String[] actions={"PICK_RADIO","PICK_MUSIC","PICK_PHONE","PICK_CARPLAY","PICK_NAV","PICK_CANBUS","PICK_SETTINGS","PICK_DSP"};
  if(tile>=0&&tile<actions.length)act(actions[tile]);
 }
 private int hitTile(float x,float y){
  if("HOME".equals(screen)&&y>=119&&y<457){int col=(int)((x-38)/242);int row=y<278?0:1;return col>=0&&col<4?row*4+col:-1;}
  if("PHONE".equals(screen)&&y>=200&&y<463){int col=x<512?0:1;int row=y<345?0:1;return row*2+col;}
  return -1;
 }
 @Override public boolean performClick(){super.performClick();return true;}
 private void tap(float x,float y,int down){
  if("HOME".equals(screen)){
   if(y>=119&&y<278){int col=(int)((x-38)/242);if(col>=0&&col<4){String[] a={"RADIO","MEDIA","PHONE","OPEN_CARPLAY"};act(a[col]);}}
   else if(y>=298&&y<457){int col=(int)((x-38)/242);if(col>=0&&col<4){String[] a={"NAVIGATION","VEHICLE","SETTINGS","SOUND"};act(a[col]);}}
   return;
  }
  if("VEHICLE".equals(screen)||"TRIP".equals(screen)){
   if(y>=145&&y<=184){vehicleTab=Math.max(0,Math.min(3,(int)(x/256)));screen="VEHICLE";invalidate();return;}
   if(x<75&&y<145){act("HOME");return;}
   return;
  }
  if("PHONE".equals(screen)){
   if(x<75&&y<145){act("HOME");return;}
   if(y>=200&&y<463){int col=x<512?0:1,row=y<345?0:1;String[] a={"OPEN_PHONE","OPEN_CALL_LOG","OPEN_CONTACTS","OPEN_FAVORITES"};act(a[row*2+col]);}
   return;
  }
  if(x<75&&y<145){act("HOME");return;}
  if(y>=372&&y<458){
   if("APP_CONNECT".equals(screen))act("OPEN_CARPLAY");
   else if("RADIO".equals(screen))act("OPEN_RADIO");
   else if("MEDIA".equals(screen))act("OPEN_MEDIA");
   else if("NAVIGATION".equals(screen))act("OPEN_NAV");
   else if("SOUND".equals(screen))act("OPEN_SOUND");
  }
  if("SETTINGS".equals(screen)&&y>=148&&y<450){
   int col=x<520?0:1;int row=Math.min(3,Math.max(0,(int)((y-148)/91)));
   String[][] a={{"PICK_RADIO","PICK_MUSIC"},{"PICK_PHONE","PICK_CARPLAY"},{"PICK_NAV","PICK_CANBUS"},{"ANDROID_SETTINGS","ANDROID_SETTINGS"}};
   act(a[row][col]);
  }
 }
 private void act(String a){if(actions!=null)actions.onAction(a);}
}
