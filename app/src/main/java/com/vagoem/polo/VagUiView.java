package com.vagoem.polo;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class VagUiView extends View {
 public interface Actions { void onAction(String action); }
 private static final int W=1024,H=600, RED=0xFFE31B36, WHITE=0xFFF3F6FA, MUTED=0xFF9BA8B7;
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final Actions actions;
 private final Drawable logo;
 private String screen="HOME";
 private final SimpleDateFormat clock=new SimpleDateFormat("HH:mm",Locale.getDefault());
 private final SimpleDateFormat date=new SimpleDateFormat("EEE, dd MMM",Locale.getDefault());
 public VagUiView(Context c,Actions a){super(c);actions=a;setFocusable(true);logo=getResources().getDrawable(R.drawable.volkswagen_logo);}
 public void setScreen(String s){screen=s==null?"HOME":s;invalidate();}
 public String getScreen(){return screen;}
 @Override protected void onDraw(Canvas c){
  super.onDraw(c);c.save();c.scale(getWidth()/(float)W,getHeight()/(float)H);
  p.setShader(new LinearGradient(0,0,W,H,0xFF111A27,0xFF080D15,Shader.TileMode.CLAMP));c.drawRect(0,0,W,H,p);p.setShader(null);
  p.setColor(0xFF101721);c.drawRect(0,0,W,80,p);p.setColor(0xFF26313D);c.drawRect(0,78,W,80,p);
  if(logo!=null){logo.setBounds(26,13,71,63);logo.draw(c);}
  txt(c,"POLO",88,34,21,WHITE,true,Paint.Align.LEFT);txt(c,"2020",145,34,21,0xFFFF6673,true,Paint.Align.LEFT);
  txt(c,"BIENVENIDO",89,55,9,MUTED,true,Paint.Align.LEFT);
  txt(c,clock.format(new Date()),512,34,27,WHITE,false,Paint.Align.CENTER);
  txt(c,date.format(new Date()).toUpperCase(Locale.getDefault()),512,55,9,MUTED,true,Paint.Align.CENTER);
  txt(c,"GPS    BT    Wi-Fi    AUDIO",995,39,10,WHITE,true,Paint.Align.RIGHT);
  p.setColor(RED);c.drawRoundRect(new RectF(477,77,547,80),2,2,p);
  switch(screen){
   case "HOME":home(c);break;
   case "APP_CONNECT":page(c,"App-Connect","Apple CarPlay mediante CPLink","ABRIR APP-CONNECT · CPLINK","OPEN_CARPLAY");break;
   case "RADIO":page(c,"Radio","FM / AM · Aplicación original","ABRIR RADIO ORIGINAL","OPEN_RADIO");break;
   case "MEDIA":page(c,"Media","Música y fuentes de audio","ABRIR REPRODUCTOR","OPEN_MEDIA");break;
   case "PHONE":page(c,"Teléfono","Manos libres Bluetooth","ABRIR TELÉFONO","OPEN_PHONE");break;
   case "NAVIGATION":page(c,"Navegación","Mapas sin conexión","ABRIR NAVEGACIÓN","OPEN_NAV");break;
   case "VEHICLE":vehicle(c);break;
   case "TRIP":trip(c);break;
   case "SETTINGS":settings(c);break;
  }
  p.setColor(0xFF080C12);c.drawRect(0,560,W,H,p);p.setColor(0xFF2C3948);c.drawRect(0,559,W,561,p);
  txt(c,"VOLKSWAGEN POLO 2020",28,584,9,MUTED,true,Paint.Align.LEFT);
  txt(c,"APP-CONNECT · CPLINK",995,584,9,MUTED,true,Paint.Align.RIGHT);
  c.restore();
 }
 private void home(Canvas c){
  String[] n={"Radio","Media","Teléfono","App-Connect","Navegación","Vehículo","Ajustes","Sonido"};
  String[] s={"FM / AM","Música","Bluetooth","CPLink","GPS offline","Estado del Polo","Sistema","Ecualizador"};
  String[] g={"◉","♫","☎","▣","➤","▱","⚙","▥"};
  for(int i=0;i<8;i++){int col=i%4,row=i/4;float x=42+col*240,y=121+row*179;
   p.setShader(new LinearGradient(x,y,x,y+157,0xFF1B222D,0xFF0D131D,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(x,y,x+220,y+157),9,9,p);p.setShader(null);
   p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(i==3?0xFF70404A:0xFF394554);c.drawRoundRect(new RectF(x,y,x+220,y+157),9,9,p);p.setStyle(Paint.Style.FILL);
   txt(c,g[i],x+110,y+61,39,WHITE,false,Paint.Align.CENTER);txt(c,n[i],x+110,y+103,15,WHITE,true,Paint.Align.CENTER);txt(c,s[i],x+110,y+127,9,i==3?0xFFFF6673:MUTED,i==3,Paint.Align.CENTER);
   p.setShader(new LinearGradient(x+18,y+154,x+202,y+154,0x00E31B36,RED,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(x+18,y+154,x+202,y+156),2,2,p);p.setShader(null);
  }
  txt(c,"BIENVENIDO A TU POLO",512,532,9,MUTED,true,Paint.Align.CENTER);
 }
 private void page(Canvas c,String title,String sub,String button,String action){
  title(c,title,sub);panel(c,82,145,860,365);txt(c,title.toUpperCase(Locale.ROOT),512,290,26,WHITE,true,Paint.Align.CENTER);
  txt(c,sub,512,329,12,MUTED,false,Paint.Align.CENTER);button(c,360,385,304,55,button);txt(c,"‹ INICIO",50,539,9,0xFFFF6673,true,Paint.Align.LEFT);
 }
 private void vehicle(Canvas c){title(c,"Vehículo","Estado del Polo · Datos reales CANBUS únicamente");panel(c,72,151,426,330);
  txt(c,"PUERTAS",95,190,12,WHITE,true,Paint.Align.LEFT);txt(c,"Esperando datos del decodificador CANBUS",95,235,11,MUTED,false,Paint.Align.LEFT);
  button(c,95,365,250,46,"ABRIR APLICACIÓN CANBUS");panel(c,524,151,426,153);
  txt(c,"CÁMARA DE REVERSA",548,190,12,WHITE,true,Paint.Align.LEFT);txt(c,"La conmutación original se conserva.",548,230,10,MUTED,false,Paint.Align.LEFT);
  panel(c,524,328,426,153);txt(c,"COMPUTADORA DE VIAJE",548,367,12,WHITE,true,Paint.Align.LEFT);txt(c,"Campos disponibles por identificar.",548,410,10,MUTED,false,Paint.Align.LEFT);
  txt(c,"‹ INICIO",50,539,9,0xFFFF6673,true,Paint.Align.LEFT);
 }
 private void trip(Canvas c){title(c,"Datos de viaje","Solo valores reales expuestos por CANBUS");
  String[] a={"CONSUMO PROMEDIO","DISTANCIA","TIEMPO DE VIAJE","VELOCIDAD PROMEDIO","AUTONOMÍA","CONSUMO INSTANTÁNEO"};
  String[] u={"L/100 km","km","h:min","km/h","km","L/100 km"};
  for(int i=0;i<6;i++){float x=78+(i%3)*294,y=158+(i/3)*166;panel(c,x,y,270,140);txt(c,a[i],x+18,y+31,9,MUTED,true,Paint.Align.LEFT);txt(c,"— —",x+18,y+86,31,WHITE,false,Paint.Align.LEFT);txt(c,u[i],x+20,y+113,10,0xFFFF6673,false,Paint.Align.LEFT);}txt(c,"‹ INICIO",50,539,9,0xFFFF6673,true,Paint.Align.LEFT);
 }
 private void settings(Canvas c){title(c,"Ajustes","Vincula las aplicaciones originales instaladas en tu WV232");
  String[] a={"Radio FM / AM","Música","Teléfono / Bluetooth","App-Connect · CPLink","Navegación offline","CANBUS / Vehículo","Ajustes del fabricante"};
  String[] b={"PICK_RADIO","PICK_MUSIC","PICK_PHONE","PICK_CARPLAY","PICK_NAV","PICK_CANBUS","PICK_SETTINGS"};
  for(int i=0;i<a.length;i++){float x=78+(i%2)*445,y=145+(i/2)*91;panel(c,x,y,420,75);txt(c,a[i],x+18,y+34,12,WHITE,true,Paint.Align.LEFT);txt(c,"VINCULAR  ›",x+400,y+43,8,0xFFFF6673,true,Paint.Align.RIGHT);}
  panel(c,523,418,420,74);txt(c,"Ajustes Android",541,448,12,WHITE,true,Paint.Align.LEFT);txt(c,"ABRIR  ›",923,461,8,0xFFFF6673,true,Paint.Align.RIGHT);txt(c,"‹ INICIO",50,539,9,0xFFFF6673,true,Paint.Align.LEFT);
 }
 private void title(Canvas c,String t,String s){txt(c,"‹",48,119,28,0xFFFF6673,false,Paint.Align.LEFT);txt(c,t,88,111,23,WHITE,true,Paint.Align.LEFT);txt(c,s,89,133,9,MUTED,false,Paint.Align.LEFT);}
 private void panel(Canvas c,float x,float y,float w,float h){p.setShader(new LinearGradient(x,y,x+w,y+h,0xFF1B222D,0xFF0D131D,Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(x,y,x+w,y+h),9,9,p);p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setColor(0xFF394554);c.drawRoundRect(new RectF(x,y,x+w,y+h),9,9,p);p.setStyle(Paint.Style.FILL);}
 private void button(Canvas c,float x,float y,float w,float h,String s){p.setColor(0xFF3A111A);c.drawRoundRect(new RectF(x,y,x+w,y+h),6,6,p);p.setStyle(Paint.Style.STROKE);p.setColor(RED);c.drawRoundRect(new RectF(x,y,x+w,y+h),6,6,p);p.setStyle(Paint.Style.FILL);txt(c,s,x+w/2,y+h/2+4,10,WHITE,true,Paint.Align.CENTER);}
 private void txt(Canvas c,String s,float x,float y,float z,int color,boolean bold,Paint.Align align){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(color);p.setTextSize(z);p.setTypeface(Typeface.create("sans-serif",bold?Typeface.BOLD:Typeface.NORMAL));p.setTextAlign(align);c.drawText(s,x,y,p);}
 @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX()*W/Math.max(1,getWidth()),y=e.getY()*H/Math.max(1,getHeight());tap(x,y);performClick();return true;}
 @Override public boolean performClick(){super.performClick();return true;}
 private void tap(float x,float y){
  if("HOME".equals(screen)){if(y>=121&&y<=278){int col=(int)((x-42)/240);if(col>=0&&col<4){String[] a={"RADIO","MEDIA","PHONE","OPEN_CARPLAY"};act(a[col]);}}
   else if(y>=300&&y<=457){int col=(int)((x-42)/240);if(col>=0&&col<4){String[] a={"NAVIGATION","VEHICLE","SETTINGS","SOUND"};act(a[col]);}}return;}
  if(x<78&&y>=90&&y<=150){act("HOME");return;}
  if("APP_CONNECT".equals(screen)&&y>375&&y<455)act("OPEN_CARPLAY");
  else if("RADIO".equals(screen)&&y>385&&y<455)act("OPEN_RADIO");
  else if("MEDIA".equals(screen)&&y>385&&y<455)act("OPEN_MEDIA");
  else if("PHONE".equals(screen)&&y>385&&y<455)act("OPEN_PHONE");
  else if("NAVIGATION".equals(screen)&&y>385&&y<455)act("OPEN_NAV");
  else if("VEHICLE".equals(screen)&&y>350&&y<420)act("OPEN_CANBUS");
  else if("SETTINGS".equals(screen)){if(y>=145&&y<220)act(x>=523?"PICK_MUSIC":"PICK_RADIO");else if(y>=236&&y<311)act(x>=523?"PICK_CARPLAY":"PICK_PHONE");else if(y>=327&&y<402)act(x>=523?"PICK_CANBUS":"PICK_NAV");else if(y>=418&&y<493)act(x>=523?"ANDROID_SETTINGS":"PICK_SETTINGS");}
 }
 private void act(String a){if(actions!=null)actions.onAction(a);}
}
