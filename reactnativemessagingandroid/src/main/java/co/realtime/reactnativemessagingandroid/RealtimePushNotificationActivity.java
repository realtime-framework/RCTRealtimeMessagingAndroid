package co.realtime.reactnativemessagingandroid;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.react.ReactActivity;

/**
 * Created by jcaixinha on 16/09/15.
 */
public class RealtimePushNotificationActivity extends ReactActivity {
    private static Activity mainParent;


    public static Activity getMainParent(){
        return mainParent;
    }

    @Override
    protected String getMainComponentName() {
        return null;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mainParent = this;
        super.onCreate(savedInstanceState);
        this.processPushBundle();
    }


    private void processPushBundle()
    {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Bundle originalExtras = extras.getBundle("pushBundle");

            if(originalExtras != null){
                originalExtras.putBoolean("foreground", false);
                RealtimeMessagingAndroid.sendExtras(originalExtras);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (RealtimeMessagingAndroid.isOnForeground() == true) {
            RealtimeMessagingAndroid.setIsOnForeground(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (RealtimeMessagingAndroid.isOnForeground() == false) {
            RealtimeMessagingAndroid.setIsOnForeground(true);
            //this.processPushBundle();
        }
    }
}
