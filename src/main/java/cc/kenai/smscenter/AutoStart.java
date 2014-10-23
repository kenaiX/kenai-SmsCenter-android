package cc.kenai.smscenter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String locIntent = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(locIntent)) {
            context.startService(new Intent(context,
                    MainService.class));
        }

    }
}
