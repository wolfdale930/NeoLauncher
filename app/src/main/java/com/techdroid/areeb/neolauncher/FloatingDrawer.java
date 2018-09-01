package com.techdroid.areeb.neolauncher;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;


public class FloatingDrawer extends Activity {
    private static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2048;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_floating_drawer);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){
           // Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
           // startActivityForResult(intent,CODE_DRAW_OVER_OTHER_APP_PERMISSION);
            Intent in = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(in,CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
        else {
            initializeView();
        }

    }

    private void initializeView(){

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(FloatingDrawer.this,FloatingViewService.class));
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode,int resultCode,Intent data){

        if (!Settings.canDrawOverlays(getApplicationContext())){

            Toast.makeText(getApplicationContext(),"Permission not granted",Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(getApplicationContext(),"Permission granted",Toast.LENGTH_SHORT).show();
            initializeView();
        }
    }
}
