package com.aos.io2017;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Splash extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

    }

    @Override
    public void onResume() {
        super.onResume();
        // Validamos google play services
        if (checkPlayServices()) {
            if (hasPermission()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                            //validamos que el usuario haya iniciado sesion
                            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                            Intent intent = new Intent(Splash.this, Register.class);
                            if (user != null) {
                                intent = new Intent(Splash.this, Main.class);
                            }
                            startActivity(intent);
                            finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(Application.tag, e.getMessage());
                        }
                    }

                }).start();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // evadimos el backkey para que no salga de la aplicacion
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            return true;
        } else {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
            } else {
                return true;
            }
        }
        return false;
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(this);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(this, status, 2404).show();
            }
            return false;
        }
        return true;
    }


}
