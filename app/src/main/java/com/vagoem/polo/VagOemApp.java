package com.vagoem.polo;

import android.app.Application;
import android.os.Build;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Keep Application startup deliberately small.
 *
 * MapLibre is initialized by NavigationActivity only when the user opens
 * navigation. Loading map/network classes here can prevent the launcher UI
 * from appearing on constrained automotive Android head units.
 */
public final class VagOemApp extends Application {
 private static final String TAG = "VAGOEM_STARTUP";
 private static final String CRASH_FILE = "vag-oem-last-crash.txt";

 @Override public void onCreate() {
  super.onCreate();
  installCrashLogger();
 }

 private void installCrashLogger() {
  final Thread.UncaughtExceptionHandler previous =
    Thread.getDefaultUncaughtExceptionHandler();
  Thread.setDefaultUncaughtExceptionHandler((thread, error) -> {
   try {
    StringWriter trace = new StringWriter();
    error.printStackTrace(new PrintWriter(trace));
    String report = "VAG OEM WV232 - INFORME DE ERROR\n"
      + "Fecha: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date()) + "\n"
      + "Android: " + Build.VERSION.RELEASE + " (SDK " + Build.VERSION.SDK_INT + ")\n"
      + "Fabricante: " + Build.MANUFACTURER + "\n"
      + "Modelo: " + Build.MODEL + "\n"
      + "Dispositivo: " + Build.DEVICE + "\n"
      + "ABI: " + (Build.SUPPORTED_ABIS.length > 0 ? Build.SUPPORTED_ABIS[0] : "desconocida") + "\n"
      + "Hilo: " + thread.getName() + "\n\n" + trace;
    File target = new File(getFilesDir(), CRASH_FILE);
    try (FileOutputStream out = new FileOutputStream(target, false)) {
     out.write(report.getBytes(StandardCharsets.UTF_8));
     out.getFD().sync();
    }
    Log.e(TAG, "Se guardó un informe local de error: " + target.getAbsolutePath(), error);
   } catch (Throwable loggingFailure) {
    Log.e(TAG, "No se pudo guardar el informe de error", loggingFailure);
   }
   if (previous != null) previous.uncaughtException(thread, error);
  });
 }

 /** Returns the most recent crash report, if one exists. */
 public File getLastCrashReport() {
  File report = new File(getFilesDir(), CRASH_FILE);
  return report.isFile() && report.length() > 0 ? report : null;
 }

 public void clearLastCrashReport() {
  File report = new File(getFilesDir(), CRASH_FILE);
  if (report.isFile()) report.delete();
 }
}