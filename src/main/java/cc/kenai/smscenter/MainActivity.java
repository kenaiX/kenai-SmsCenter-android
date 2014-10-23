package cc.kenai.smscenter;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kenai.function.message.XLog;

import date.RecordDatebaceService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Window localWindow = getWindow();
        localWindow.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);



        Button bt1= (Button) findViewById(R.id.button1);
        Button bt2= (Button) findViewById(R.id.button2);

        bt1.setText("start");
        bt2.setText("stop");



        XLog.model=true;



        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startService(new Intent(getBaseContext().getApplicationContext(), MainService.class));
            }
        });
        bt2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(getBaseContext().getApplicationContext(), MainService.class));
            }
        });

//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                XLog.xLog("date:");
//                Cursor cursor=RecordDatebaceService.getInstance(MainActivity.this).getAllRecord();
//                while (cursor.moveToNext()){
//                    XLog.xLog(new RecordDatebaceService.Model(cursor).toString());
//                }
//            }
//        }).start();

//        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.main_frame);
        FragmentTransaction transaction=getFragmentManager().beginTransaction();
        transaction.add(R.id.main_frame,new StateFragment());
        transaction.commit();
    }
}
