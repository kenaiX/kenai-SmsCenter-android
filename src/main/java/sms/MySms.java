package sms;

import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;

/**
 * strColumnName=_id strColumnValue=48 //短消息序号
 * strColumnName=thread_id strColumnValue=16 //对话的序号（conversation）
 * strColumnName=address strColumnValue=+8613411884805 //发件人地址，手机号
 * strColumnName=person strColumnValue=null //发件人，返回一个数字就是联系人列表里的序号，陌生人为null
 * strColumnName=date strColumnValue=1256539465022 //日期 long型，想得到具体日期自己转换吧！
 * strColumnName=protocol strColumnValue=0 //协议
 * strColumnName=read strColumnValue=1 //是否阅读
 * strColumnName=status strColumnValue=-1 //状态
 * strColumnName=type strColumnValue=1 //类型 1是接收到的，2是发出的
 * strColumnName=reply_path_present strColumnValue=0 //
 * strColumnName=subject strColumnValue=null //主题
 * strColumnName=body strColumnValue=您好 //短消息内容
 * strColumnName=service_center strColumnValue=+8613800755500 //短信服务中心号码编号，可以得知该短信是从哪里发过来的
 */
class MySms {
    private final long id;
    private final String address;
    private final long date;
    private final int read;
    /**
     * 1:receive,2:send
     */
    private final int type;
    private final String body;

    public MySms(Cursor smsCursor) {
        id = smsCursor.getLong(smsCursor.getColumnIndex("_id"));
        address = smsCursor.getString(smsCursor.getColumnIndex("address"));
        date = smsCursor.getLong(smsCursor.getColumnIndex("date"));
        read = smsCursor.getInt(smsCursor.getColumnIndex("read"));
        type = smsCursor.getInt(smsCursor.getColumnIndex("type"));
        body = smsCursor.getString(smsCursor.getColumnIndex("body"));
    }

    public MySms(long id, String address, Long date, int read, int type, String body) {
        this.id = id;
        this.address = address;
        this.date = date;
        this.read = read;
        this.type = type;
        this.body = body;
    }

    public long getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public long getDate() {
        return date;
    }

    public int getRead() {
        return read;
    }

    public int getType() {
        return type;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", id);
            jsonObject.put("address", address);
            jsonObject.put("date", date);
            jsonObject.put("read", read);
            jsonObject.put("type", type);
            jsonObject.put("body", body);
        } catch (JSONException e) {
        }


        return jsonObject.toString();
    }

    public static MySms formatFromJson(String jsonString) {
        MySms sms = null;
        try {
            JSONObject json = new JSONObject(jsonString);
            sms = new MySms(json.getLong("id"), json.getString("address"), json.getLong("date"), json.getInt("read"), json.getInt("type"), json.getString("body"));
        } catch (JSONException e) {
        }
        return sms;
    }

    public String toMd5() {
        EncryptUtil eu = new EncryptUtil();
        try {
            return eu.md5Digest(toString()).substring(12, 20);
        } catch (Exception e) {
            return null;
        }
    }


}

class EncryptUtil {

    private static final String UTF8 = "utf-8";

    /**
     * MD5数字签名
     *
     * @param src
     * @return
     * @throws Exception
     */
    public String md5Digest(String src) throws Exception {
        // 定义数字签名方法, 可用：MD5, SHA-1
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] b = md.digest(src.getBytes(UTF8));
        return this.byte2HexStr(b);
    }

    /**
     * 字节数组转化为大写16进制字符串
     *
     * @param b
     * @return
     */
    private String byte2HexStr(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < b.length; i++) {
            String s = Integer.toHexString(b[i] & 0xFF);
            if (s.length() == 1) {
                sb.append("0");
            }
            sb.append(s.toUpperCase());
        }
        return sb.toString();
    }
}