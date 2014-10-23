package cc.kenai.smscenter;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.kenai.function.message.XToast;

import java.net.URL;

import date.RecordDatebaceService;

/**
 * Created by kenai on 13-12-14.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StateFragment extends Fragment {
    ListView listView;
    myCursorAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_state_message, container, false);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.state_message_listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = adapter.getCursor();
                try {
                    cursor.moveToPosition(position);
                    AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
                    builder.setMessage(cursor.getString(cursor.getColumnIndex("mySms")));
                    builder.create().show();
                } catch (Exception e) {
                    XToast.xToast(getActivity(), "" + cursor.getCount());
                }
            }
        });
        new DownloadFilesTask().execute();
    }

    private class DownloadFilesTask extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {
            return RecordDatebaceService.getInstance(getActivity()).getRecordNot20x();
        }

        protected void onPostExecute(Cursor result) {
            listView.setAdapter(adapter = new myCursorAdapter(getActivity(), result));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


}

class myCursorAdapter extends CursorAdapter {
    private LayoutInflater mInflater;
    private Context mContext;
    TextView add_con;


    public myCursorAdapter(Context context, Cursor c) {
        super(context, c, true);
        mContext = context;
        mInflater = LayoutInflater.from(context);
//        LoaderManager loaderManager=
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String state = cursor.getString(cursor.getColumnIndex("httpState"));
        String date = cursor.getString(cursor.getColumnIndex("date"));
        String body = cursor.getString(cursor.getColumnIndex("mySms"));
        TextView item1 = (TextView) view.findViewById(R.id.state_message_item1);
        TextView item2 = (TextView) view.findViewById(R.id.state_message_item2);
        TextView item3 = (TextView) view.findViewById(R.id.state_message_item3);
        item1.setText(state);
        item2.setText(date);
        item3.setText(body);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.layout_state_message_item, parent, false);  //一般都这样写，返回列表行元素，注意这里返回的就是bindView中的view
    }
}