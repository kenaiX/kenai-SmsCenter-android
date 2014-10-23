package sms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.kenai.function.message.XLog;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cc.kenai.smscenter.R;
import date.RecordDatebaceService;

public class ObserverService {
    String Tag = "ObserverService";
    Context context;
    HandlerThread handlerThread_Local = new HandlerThread("local");
    private Handler workHandler_local;
    private HttpWorkHandlerGroup httpWorkHandlerGroup;

    public ObserverService(Service context) {
        this.context = context;
    }


    void delete(final MySms sms) {
        int n = context.getContentResolver().delete(Uri.parse("content://sms"), "_id = " + sms.getId() + " and date = '" + sms.getDate() + "'", null);
        XLog.xLog(Tag, "Sms delete : " + n);
    }

    public void xCreate() {
        handlerThread_Local.start();
        workHandler_local = new Handler(handlerThread_Local.getLooper());

        httpWorkHandlerGroup = new HttpWorkHandlerGroup(context, 5);

        httpWorkHandlerGroup.init();

        workHandler_local.post(new Runnable() {
            @Override
            public void run() {
                updateTask();
            }
        });
        workHandler_local.post(new Runnable() {
            @Override
            public void run() {
                updateTask_1();
            }
        });
        // 创建两个对象
        smsContentObserver = new SmsObserver(context, mHandler) {

            @Override
            public void onNewSms(final MySms sms) {
                delete(sms);
                workHandler_local.post(new Runnable() {
                    @Override
                    public void run() {
                        XLog.xLog(Tag, "receive sms: " + sms.toString());
                        RecordDatebaceService datebaceService = RecordDatebaceService.getInstance(context);
                        final String md5 = sms.toMd5();
                        if (!datebaceService.hasMd5(md5)) {
                            if (!datebaceService.addRecord(new RecordDatebaceService.Model(sms.toString(), md5, 0))) {
                                XLog.xLog(Tag, "add error");
                            }
                        }
                        updateTask();
                    }
                });
            }

            @Override
            public void findSms(final List<MySms> list) {
                XLog.xLog(Tag, list.toString());
                workHandler_local.post(new Runnable() {
                    @Override
                    public void run() {
                        RecordDatebaceService datebaceService = RecordDatebaceService.getInstance(context);
                        for (int i = 0; i < list.size(); i++) {
                            final MySms sms = list.get(i);
                            delete(sms);
                            final String md5 = sms.toMd5();
                            if (!datebaceService.hasMd5(md5)) {
                                if (!datebaceService.addRecord(new RecordDatebaceService.Model(sms.toString(), md5, 0))) {
                                    XLog.xLog(Tag, "add error");
                                    continue;
                                }
                            }

                        }
                        updateTask();
                    }
                });
            }

            @Override
            public boolean conditions(MySms sms) {
                if (sms.getBody().contains("action\":\"newuser")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        // 注册内容观察者
        registerContentObservers();
        timer.schedule(timerTask, 30 * 60 * 1000, 30 * 60 * 1000);
    }

    /**
     * 提取所以数据库中状态为0的信息
     */
    void updateTask() {
        Cursor cursor = RecordDatebaceService.getInstance(context).getRecordByState(0);
        if (cursor != null)
            while (cursor.moveToNext()) {
                httpWorkHandlerGroup.addTask(cursor.getString(cursor.getColumnIndex("mySms")), cursor.getString(cursor.getColumnIndex("md5")));

            }
    }

    /**
     * 提取所以数据库中状态为1的信息
     */
    void updateTask_1() {
        Cursor cursor = RecordDatebaceService.getInstance(context).getRecordByState(1);
        if (cursor != null)
            while (cursor.moveToNext()) {
                httpWorkHandlerGroup.addTask(cursor.getString(cursor.getColumnIndex("mySms")), cursor.getString(cursor.getColumnIndex("md5")));

            }
    }

    private final Timer timer = new Timer();
    private final TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            workHandler_local.post(new Runnable() {
                @Override
                public void run() {
                    MyNotification.cancelNotification(context);
                    updateTask();
                    smsContentObserver.searchInAllMessage();
                }
            });
        }
    };

    public void xDestroy() {
        unregisterContentObservers();
        httpWorkHandlerGroup.quit();
        handlerThread_Local.quit();
    }

    private SmsObserver smsContentObserver;

    private void registerContentObservers() {
        Uri smsUri = Uri.parse("content://sms");
        context.getContentResolver().registerContentObserver(smsUri, true,
                smsContentObserver);

    }

    private final void unregisterContentObservers() {
        context.getContentResolver().unregisterContentObserver(
                smsContentObserver);
    }

    private final Handler mHandler = new Handler();


}

class HttpWorkHandlerGroup {
    HandlerThread[] handlerThread;
    HttpWorkHandler[] httpWorkHandler;
    final Context context;
    final int number;

    public HttpWorkHandlerGroup(Context context, int number) {
        this.context = context;
        this.number = number;
    }

    void init() {
        handlerThread = new HandlerThread[number];
        httpWorkHandler = new HttpWorkHandler[number];
        for (int i = 0; i < number; i++) {
            handlerThread[i] = new HandlerThread("internetHandler-" + i);
            handlerThread[i].start();
            httpWorkHandler[i] = new HttpWorkHandler(handlerThread[i].getLooper(), context, "HttpWorkHandler-" + i);
        }
    }

    void quit() {
        for (int i = 0; i < number; i++) {
            handlerThread[i].quit();
        }
        handlerThread = null;
        httpWorkHandler = null;
    }

    int now = 0;

    void addTask(String smsString, String md5) {
        now = now % number;
        httpWorkHandler[now].add(smsString, md5);
        now++;
    }
}

class HttpWorkHandler extends Handler {
    static Set<String> stringSet = new HashSet<String>();
    private Context context;
    private String Tag;

    public HttpWorkHandler(Looper looper, Context context, String Tag) {
        super(looper);
        this.context = context;
        this.Tag = Tag;
    }

    public void add(String smsString, String md5) {


        MySms sms = MySms.formatFromJson(smsString);
        String phone = getPhoneNumber(sms.getAddress());
        String sn = null;
        XLog.xLog(Tag, "smsString : " + smsString);
        try {
            String body = sms.getBody();
            body = body.replace("(", "{");
            body = body.replace(")", "}");
            body = body.replaceAll("[^a-zA-Z\\d{}'\":=,]", "");
            if (!body.startsWith("{")) {
                body = "{" + body;
            }
            if (!body.endsWith("}")) {
                body = body + "}";
            }
            sn = new JSONObject(body).getString("sn");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (phone != null && sn != null) {
            try {
                synchronized (this) {
                    stringSet.add(new JSONObject().put("sn", sn).put("phone", phone).put("md5", md5).toString());
                }
            } catch (JSONException e) {
            }
            handleMessage(null);
        }
    }


    final static String MobileMatchStr = "^(86|(\\+86))?1+\\d{10}$";

    /**
     * 给定的参数如果是手机号码则返回后11位，否则返回null
     *
     * @return
     */
    public final static String getPhoneNumber(String num) {
        Pattern pattern = Pattern.compile(MobileMatchStr);
        Matcher matcher = pattern.matcher(num);
        if (matcher.find()) {
            pattern = Pattern.compile("1+\\d{10}");
            matcher = pattern.matcher(num);
            if (matcher.find()) {
                return matcher.group();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    private synchronized String getOne() {
        String s = stringSet.iterator().next();
        XLog.xLog(Tag, "do : " + s);
        stringSet.remove(s);
        return s;
    }


    @Override
    public void handleMessage(Message msg) {
        while (stringSet.size() > 0) {
            JSONObject jsonObject;
            String sn;
            String phone;
            String sms_md5;
            String sign;
            try {
                jsonObject = new JSONObject(getOne());
                sn = jsonObject.getString("sn");
                phone = jsonObject.getString("phone");
                sms_md5 = jsonObject.getString("md5");
            } catch (JSONException e) {
                XLog.xLog_bug(Tag, "string to json error");
                continue;
            }

            String s = "31246de53ff36c8d" + "||" + sn + "||" + phone;

            try {
                // MD5加密
                EncryptUtil eu = new EncryptUtil();
                XLog.xLog(Tag, "md5 :" + s);
                sign = eu.md5Digest(s).substring(12, 20);
            } catch (Exception e) {
                XLog.xLog_bug(Tag, "Md5 error");
                continue;
            }

            String http = "http://1.meicall.duapp.com/rest/2/get/smsreceiver?sn=" + sn + "&phone=" + phone + "&sign=" + sign;
            XLog.xLog(Tag, "http get :" + http);

            HttpGet get = new HttpGet(http);

            HttpParams httpParameters = new BasicHttpParams();
            // Set the timeout in milliseconds until a connection is established.
            HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
            // Set the default socket timeout (SO_TIMEOUT)
            // in milliseconds which is the timeout for waiting for data.
            HttpConnectionParams.setSoTimeout(httpParameters, 50000);


            HttpClient hc = new DefaultHttpClient(httpParameters);


            RecordDatebaceService.getInstance(context).updateHttpState(sms_md5, 1);


            try {
                HttpResponse response = hc.execute(get);
                int state = response.getStatusLine().getStatusCode();


                if (state > 0 && state < 400) {
                    XLog.xLog(Tag, "http response : " + state);
                    RecordDatebaceService.getInstance(context).updateHttpState(sms_md5, response.getStatusLine().getStatusCode());
                    continue;
                } else {
                    XLog.xLog_bug(Tag, "http response : " + state);
                }
            } catch (Exception e) {
                XLog.xLog_bug(Tag, " timeout ");
            } finally {
                hc.getConnectionManager().shutdown();
            }
            RecordDatebaceService.getInstance(context).updateHttpState(sms_md5, 0);
            MyNotification.showNotification(context);
            continue;
        }
    }


}

class MyNotification {
    public final static void showNotification(Context context) {
        String contentTitle = "SMS Center Log";
        String contentText = "internet response not pass";
        Notification notification;

        int largeIco;

        largeIco = R.drawable.ic_launcher;
        notification = new Notification(largeIco, "", 0);
        notification.flags = Notification.FLAG_SHOW_LIGHTS;
        notification.defaults = Notification.DEFAULT_ALL;

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(
                ""), 0);
        notification.setLatestEventInfo(context, contentTitle, contentText, pi);

        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(400, notification);
    }

    public final static void cancelNotification(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(400);
    }
}
