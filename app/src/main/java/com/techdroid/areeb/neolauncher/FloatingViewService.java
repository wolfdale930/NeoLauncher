package com.techdroid.areeb.neolauncher;


import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class FloatingViewService extends Service {

    private PackageManager packageManager;
    private List<AppDetail> appList;
    private ArrayAdapter<AppDetail> adapter;
    private WindowManager windowManager;
    private View floatingView;
    private GridView gridView;
    private boolean isViewCollapsed = true;

    //Mandatory
    public IBinder onBind(Intent intent){
        return null;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        //Setting view and window
        floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_drawer,null);
        final View collapsedView = floatingView.findViewById(R.id.collapsed_view);
        final View expandedView = floatingView.findViewById(R.id.expanded_view);
        final int w_resolution;
        expandedView.setVisibility(View.GONE);
        collapsedView.setVisibility(View.VISIBLE);

        gridView = (GridView) floatingView.findViewById(R.id.app_grid);

        //////////////////////////////////////////////////////////////
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;

        windowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        windowManager.addView(floatingView,params);

        // Finding resolution
        DisplayMetrics dm = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(dm);
        w_resolution = dm.widthPixels;

        ///////////Loading Launchable Apps/////////////////////
        loadApps();
        ////////////////////Setting adapter///////////////////////
        adapter = new ArrayAdapter<AppDetail>(this ,R.layout.grid_item,appList){
            @Override
            public View getView(int position, View convertView, ViewGroup parent){
                if(convertView==null){
                    convertView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid_item,null);
                }
                final ImageView appIcon = (ImageView)convertView.findViewById(R.id.icon);
                appIcon.setImageDrawable(appList.get(position).icon);

                TextView label = (TextView)convertView.findViewById(R.id.label);
                label.setText(appList.get(position).label);

                return convertView;
            }
        };
        ///////////////////Setting Adapter//////////////////////
        gridView.setAdapter(adapter);
        ///////////////////////////////////////////////////////
        floatingView.findViewById(R.id.root_container).setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int)(motionEvent.getRawX() - initialTouchX);
                        params.y = initialY + (int)(motionEvent.getRawY() - initialTouchY);

                        windowManager.updateViewLayout(floatingView,params);
                        return true;
                    case MotionEvent.ACTION_UP:

                        if (params.x <= w_resolution/2){
                            params.x = 0;
                            windowManager.updateViewLayout(floatingView,params);
                        }
                        else {
                            params.x = w_resolution;
                            windowManager.updateViewLayout(floatingView,params);
                        }
                        int XDiff = (int)(motionEvent.getRawX() - initialTouchX);
                        int YDiff = (int)(motionEvent.getRawY() - initialTouchY);
                        if(XDiff<2 && YDiff<2 && XDiff>-2 && YDiff>-2 ){
                            if(isViewCollapsed){
                                expandedView.setVisibility(View.VISIBLE);
                                isViewCollapsed=false;
                            }
                            else {
                                expandedView.setVisibility(View.GONE);
                                isViewCollapsed=true;
                            }
                        }
                        return true;
                    default: break;

                }
                return false;
            }
        });
        /*
        floatingView.findViewById(R.id.drawer_icon_expanded).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                expandedView.setVisibility(View.GONE);
            }
        });*/


    }

    private void loadApps(){
        packageManager = getPackageManager();
        appList = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN,null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> available = packageManager.queryIntentActivities(intent,0);
        try {
            for (ResolveInfo info : available) {
                AppDetail temp = new AppDetail();
                temp.label = info.loadLabel(packageManager);
                temp.name = info.activityInfo.packageName;
                temp.icon = info.activityInfo.loadIcon(packageManager);
                temp.time = getApplicationContext().getPackageManager().getPackageInfo(info.activityInfo.packageName, 0).firstInstallTime;
                appList.add(temp);
            }
        }catch(Exception e){ Toast.makeText(getApplicationContext(),"Could not load apps properly",Toast.LENGTH_SHORT).show();}
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(floatingView!=null)
            windowManager.removeView(floatingView);
    }




}
