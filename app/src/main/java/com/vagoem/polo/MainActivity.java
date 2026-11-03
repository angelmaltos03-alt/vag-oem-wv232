package com.vagoem.polo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends Activity {
    private static final String PREFS = "vag_oem_targets";
    private SharedPreferences prefs;
    private VagUiView ui;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        hideSystemBars();
        ui = new VagUiView(this, this::handleAction);
        setContentView(ui);
    }

    @Override protected void onResume() {
        super.onResume();
        hideSystemBars();
        if (ui != null) ui.invalidate();
    }

    private void hideSystemBars() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
    }

    private void handleAction(String action) {
        switch (action) {
            case "HOME": ui.setScreen("HOME"); break;
            case "RADIO": ui.setScreen("RADIO"); break;
            case "MEDIA": ui.setScreen("MEDIA"); break;
            case "PHONE": ui.setScreen("PHONE"); break;
            case "NAVIGATION": ui.setScreen("NAVIGATION"); break;
            case "VEHICLE": ui.setScreen("VEHICLE"); break;
            case "TRIP": ui.setScreen("TRIP"); break;
            case "SETTINGS": ui.setScreen("SETTINGS"); break;
            case "APP_CONNECT": launchCPLink(); break;
            case "OPEN_RADIO": launchTarget("RADIO", "Radio"); break;
            case "OPEN_MEDIA": launchTarget("MUSIC", "Media"); break;
            case "OPEN_PHONE": launchTarget("PHONE", "Teléfono"); break;
            case "OPEN_CARPLAY": launchCPLink(); break;
            case "OPEN_NAV": launchNavigation(); break;
            case "OPEN_CANBUS": launchTarget("CANBUS", "CANBUS"); break;
            case "SOUND": openSettings(Settings.ACTION_SOUND_SETTINGS); break;
            case "ANDROID_SETTINGS": openSettings(Settings.ACTION_SETTINGS); break;
            case "PICK_RADIO": pick("RADIO", "Selecciona la aplicación de radio"); break;
            case "PICK_MUSIC": pick("MUSIC", "Selecciona el reproductor de música"); break;
            case "PICK_PHONE": pick("PHONE", "Selecciona Teléfono / Bluetooth"); break;
            case "PICK_CARPLAY": pick("CARPLAY", "Selecciona CPLink / App-Connect"); break;
            case "PICK_NAV": pick("NAV", "Selecciona tu navegador offline"); break;
            case "PICK_CANBUS": pick("CANBUS", "Selecciona la aplicación CANBUS"); break;
            case "PICK_SETTINGS": pick("OEM_SETTINGS", "Selecciona ajustes del fabricante"); break;
            case "RESET_LINKS":
                new AlertDialog.Builder(this).setTitle("Restablecer vínculos")
                    .setMessage("Se borrarán las aplicaciones vinculadas, pero no se desinstalarán.")
                    .setNegativeButton("Cancelar", null)
                    .setPositiveButton("Restablecer", (d,w) -> { prefs.edit().clear().apply(); ui.invalidate(); })
                    .show();
                break;
        }
    }

    private void openSettings(String action) {
        try { startActivity(new Intent(action)); }
        catch (Exception e) { toast("No se pudieron abrir los ajustes."); }
    }

    private void launchCPLink() {
        String saved = prefs.getString("CARPLAY", null);
        if (saved != null && launch(saved, "CPLink")) return;
        List<ResolveInfo> found = launchableApps();
        List<ResolveInfo> matches = new ArrayList<>();
        for (ResolveInfo r : found) {
            String label = String.valueOf(r.loadLabel(getPackageManager())).toLowerCase();
            String pkg = r.activityInfo.packageName.toLowerCase();
            if (label.contains("cplink") || label.contains("cp link") || pkg.contains("cplink")) matches.add(r);
        }
        if (matches.size() == 1) {
            String pkg = matches.get(0).activityInfo.packageName;
            prefs.edit().putString("CARPLAY", pkg).apply();
            launch(pkg, "CPLink");
        } else pick("CARPLAY", "Selecciona la aplicación CPLink");
    }

    private void launchNavigation() {
        String pkg = prefs.getString("NAV", "app.organicmaps");
        if (!launch(pkg, "Navegación")) {
            toast("Instala Organic Maps o vincula otra app en Ajustes.");
            ui.setScreen("SETTINGS");
        }
    }

    private void launchTarget(String key, String label) {
        String pkg = prefs.getString(key, null);
        if (pkg == null) { toast("Vincula " + label + " desde Ajustes."); ui.setScreen("SETTINGS"); }
        else launch(pkg, label);
    }

    private boolean launch(String pkg, String label) {
        try {
            Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
            if (i == null) return false;
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            return true;
        } catch (Exception e) { toast("No se pudo abrir " + label); return false; }
    }

    private void pick(String key, String title) {
        List<ResolveInfo> apps = launchableApps();
        if (apps.isEmpty()) { toast("No hay aplicaciones disponibles."); return; }
        String[] labels = new String[apps.size()];
        for (int n=0; n<apps.size(); n++) {
            ResolveInfo r = apps.get(n);
            labels[n] = String.valueOf(r.loadLabel(getPackageManager())) + "\n" + r.activityInfo.packageName;
        }
        new AlertDialog.Builder(this).setTitle(title).setItems(labels, (d, which) -> {
            String pkg = apps.get(which).activityInfo.packageName;
            prefs.edit().putString(key, pkg).apply();
            if ("CARPLAY".equals(key)) launch(pkg, "CPLink");
            toast("Aplicación vinculada.");
        }).setNegativeButton("Cancelar", null).show();
    }

    private List<ResolveInfo> launchableApps() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(i, 0);
        Collections.sort(apps, Comparator.comparing(a -> String.valueOf(a.loadLabel(getPackageManager())), String.CASE_INSENSITIVE_ORDER));
        return apps;
    }

    private void toast(String s) { Toast.makeText(this, s, Toast.LENGTH_SHORT).show(); }

    @Override public void onBackPressed() {
        if (ui != null && !"HOME".equals(ui.getScreen())) ui.setScreen("HOME");
        else super.onBackPressed();
    }
}
