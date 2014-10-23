package sms;


import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;


import com.kenai.function.message.XLog;

import java.util.ArrayList;
import java.util.List;

public abstract class SmsObserver extends ContentObserver {
    @SuppressWarnings("unused")
    private final String TAG = "SmsObserver";
    private Context mContext;
    private ArrayList<String> unreadSms = new ArrayList<String>();

    public SmsObserver(Context context, Handler handler) {
        super(handler);
        mContext = context;
        new Thread() {
            public void run() {
                searchInAllMessage();
            }
        }.start();
    }

    public void searchInAllMessage() {

        Cursor smsCursor = mContext.getContentResolver().query(
                Uri.parse("content://sms"),
                new String[]{"_id", "read", "body", "date", "address", "type"}, "type = 1",
                null, null);
        if (smsCursor != null) {

            final List<MySms> list = new ArrayList<MySms>();
            while (smsCursor.moveToNext()) {
                final MySms sms = new MySms(smsCursor);
                if (conditions(sms)) {
                    list.add(sms);
                }
            }
            if (list.size() > 0) {
                findSms(list);
            }
            smsCursor.close();
        }

    }

    /**
     * 当所监听的Uri发生改变时，就会回调此方法
     *
     * @param selfChange 此值意义不大 一般情况下该回调值false
     */
    @Override
    public void onChange(boolean selfChange) {
        Cursor smsCursor = mContext.getContentResolver().query(
                Uri.parse("content://sms"),
                new String[]{"_id", "read", "body", "date", "address", "type"}, "read = 0",
                null, null);

        if (smsCursor != null) {
            boolean shouldUpdate = false;
            while (smsCursor.moveToNext()) {
                final MySms sms = new MySms(smsCursor);
                if (!unreadSms.contains(sms.toMd5())) {
                    newSms(sms);
                    shouldUpdate = true;
                }
            }
            if (shouldUpdate) {
                smsCursor.moveToFirst();
                unreadSms.clear();
                while (smsCursor.moveToNext()) {
                    final MySms sms = new MySms(smsCursor);
                    unreadSms.add(sms.toMd5());
                }
            }
            smsCursor.close();
        }
    }

    public void newSms(MySms sms) {
        if (conditions(sms))
            onNewSms(sms);

    }

    public abstract void onNewSms(MySms sms);

    public abstract void findSms(List<MySms> list);

    public abstract boolean conditions(MySms sms);
}


//XLog.xLog("the sms table has changed");

////查询发件箱里的内容       
//Uri outSMSUri = Uri.parse("content://sms/inbox") ;  
// 
////Cursor c = mContext.getContentResolver().query(outSMSUri, null, null,null,"date desc");  
//Cursor c = mContext.getContentResolver().query(outSMSUri, 
//		new String[]{"read"}, 
//		"read=?", 
//		new String[]{"0"}, 
//		"date desc");
//if(c != null){  
//    
//	XLog.xLog("the number of send is"+c.getCount()) ;  
//    
//  StringBuilder sb = new StringBuilder() ;  
//  //循环遍历  
//  while(c.moveToNext()){  
////    sb.append("发件人手机号码: "+c.getInt(c.getColumnIndex("address")))  
////      .append("信息内容: "+c.getInt(c.getColumnIndex("body")))  
////      .append("是否查看: "+c.getInt(c.getColumnIndex("read")))   
////      .append("发送时间： "+c.getInt(c.getColumnIndex("date")))  
////      .append("\n");  
//      sb.append("发件人手机号码: "+c.getInt(c.getColumnIndex("address")))  
//        .append("信息内容: "+c.getString(c.getColumnIndex("body")))  
//        .append("\n");  
//  }  
//  c.close();            
//  mHandler.obtainMessage(MSG_OUTBOXCONTENT, getUnreadSmsCount(mContext)).sendToTarget(); 


//  
//  
//  Cursor cursor = managedQuery(Uri.parse("content://sms/inbox"), 
//  		new String[]{"_id", "address", "read"}, 
//  		" address=? and read=?", 
//  		new String[]{"12345678901", "0"}, 
//  		"date desc");  
//  
//  if (cursor != null){  
//      ContentValues values = new ContentValues();  
//      values.put("read", "1");        //修改短信为已读模式  
//      cursor.moveToFirst();  
//      while (cursor.isLast()){  
//          //更新当前未读短信状态为已读  
//          getContentResolver().update(Uri.parse("content://sms/inbox"), values, " _id=?", new String[]{""+cursor.getInt(0)});  
//          cursor.moveToNext();  
//      }  
//  }  
//private final int getUnreadSmsCount(Context context) {
//int unreadSmsCount = 0;
//Cursor smsCursor = mContext.getContentResolver().query(Uri.parse("content://sms"), 
//		new String[]{"read"}, 
//		"read = 0", 
//		null, 
//		null);
//if (smsCursor != null) {
//  while (smsCursor.moveToNext()) {  
//          unreadSmsCount++;
//  }
//}
//smsCursor.close();
//return unreadSmsCount;
//}
