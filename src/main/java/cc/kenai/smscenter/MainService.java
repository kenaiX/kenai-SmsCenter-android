package cc.kenai.smscenter;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import com.kenai.function.meizu.MeizuNotification;
import com.kenai.function.message.XLog;

import org.json.JSONException;

import sms.ObserverService;

/**
 * Created by kenai on 13-12-13.
 */
public class MainService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ObserverService myObserverService;

    @Override
    public void onCreate() {
        super.onCreate();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                throw new NullPointerException();
//            }
//        }).start();
        myObserverService = new ObserverService(this);
        myObserverService.xCreate();

        startForeground(1, createNotification(this));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        myObserverService.xDestroy();
    }


    private final static Notification createNotification(Context context) {
        XLog.xLog("weather_notification");
        String contentTitle = "SMS Center";
        String contentText = "runing";
        Notification notification;

        int largeIco;

        largeIco = R.drawable.ic_launcher;
        notification = new Notification(largeIco, "", 0);
        notification.flags = Notification.FLAG_NO_CLEAR;
        PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class), 0);
        notification.setLatestEventInfo(context, contentTitle, contentText, pi);
        notification.icon = R.drawable.statebar_logo_small;
        MeizuNotification.internalApp(notification);
        return notification;
    }
}
