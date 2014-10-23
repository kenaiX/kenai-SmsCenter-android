package date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.format.DateFormat;

import com.kenai.function.message.XLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by kenai on 13-11-16.
 */
public class RecordDatebaceService {
    public interface Callback {
        void onCallback(Cursor cursor);
    }

    private static RecordDatebaceService myRecordDatebaceService = new RecordDatebaceService();
    private static DatebaceHelper dbHelper = null;
//	private static Context context;


    public static synchronized RecordDatebaceService getInstance(final Context context) {
        if (dbHelper == null) {
            dbHelper = new DatebaceHelper(context.getApplicationContext());
        }
        return myRecordDatebaceService;
    }

    public static synchronized void xclose() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }


    public static class Model {
        public String mySms;
        String md5;
        int httpState;


        public Model(String mySms, String md5, int httpState) {
            this.mySms = mySms;
            this.md5 = md5;
            this.httpState = httpState;
        }

        public Model(Cursor cursor){
            this.mySms = cursor.getString(cursor.getColumnIndex(DatebaceHelper.Record_MySms));
            this.md5 = cursor.getString(cursor.getColumnIndex(DatebaceHelper.Record_Md5));
            this.httpState = cursor.getInt(cursor.getColumnIndex(DatebaceHelper.Record_HttpState));
        }

        @Override
        public String toString() {
            try {
                return new JSONObject().put("mySms",mySms).put("md5",md5).put("httpState",httpState).toString();
            } catch (JSONException e) {
                return super.toString();
            }
        }
    }

    public boolean addRecord(final Model model) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatebaceHelper.Record_MySms, model.mySms);
        values.put(DatebaceHelper.Record_Md5, model.md5);
        values.put(DatebaceHelper.Record_HttpState, model.httpState);
        long result = db.insert(DatebaceHelper.Record_TableName, null, values);
        if (result > 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hasMd5(final String md5) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatebaceHelper.Record_Md5 + " = '" + md5 + "'";
        Cursor cursor = db.query(DatebaceHelper.Record_TableName, new String[]{DatebaceHelper.Record_Md5}, selection, null, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            return true;
        } else {
            return false;
        }

    }

    public void updateHttpState(String md5, int state) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatebaceHelper.Record_HttpState, state);
        db.update(DatebaceHelper.Record_TableName, values, DatebaceHelper.Record_Md5 + " = '" + md5+"'", null);

    }
    public Cursor getRecordByState(int state) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatebaceHelper.Record_HttpState + " = '" + state + "'";
        return db.query(DatebaceHelper.Record_TableName, new String[]{DatebaceHelper.Record_MySms,DatebaceHelper.Record_Md5}, selection, null, null, null, null);
    }

    public Cursor getAllRecord() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(DatebaceHelper.Record_TableName, null, null, null, null, null, null);
    }

    public Cursor getRecordNot20x() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String selection = DatebaceHelper.Record_HttpState + " < '200' or "+DatebaceHelper.Record_HttpState+"  > '299'";
        return db.query(DatebaceHelper.Record_TableName, null, selection, null, null, null, null);
    }


//    // 删除数据
//    public static void delete(Context context) {
//        if (dbHelper != null) {
//            SQLiteDatabase db = dbHelper.getReadableDatabase();
//            db.execSQL("DROP TABLE IF EXISTS mytable_liuliang");
//            db.execSQL("CREATE TABLE mytable_liuliang (_id integer primary key autoincrement,"
//                    + "time_year integer,time_month integer,time_day integer,time_hour integer,"
//                    + "time_minutes integer,send integer, receive integer)");
//
//        } else {
//            getInstance(context);
//            dbHelper.close();
//        }
//
//    }

}


class DatebaceHelper extends SQLiteOpenHelper {
    public final static String DatebaceName = "SMSRECORD";
    public final static String Record_TableName = "MainRecord";
    public final static String Record_MySms = "mySms";
    public final static String Record_Md5 = "md5";
    public final static String Record_HttpState = "httpState";
    public final static String Record_Datetime = "date";


    public final static int VERSION = 1;

    public DatebaceHelper(Context context) {
        super(context, DatebaceName, null, VERSION);
        // TODO Auto-generated constructor stub
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
        db.execSQL("CREATE TABLE " + Record_TableName + " (_id INTEGER PRIMARY KEY autoincrement," +
                Record_Datetime + " TimeStamp DEFAULT (datetime('now','localtime'))," + Record_HttpState + " INTEGER," + Record_Md5 + " TEXT," + Record_MySms + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {


    }
}
