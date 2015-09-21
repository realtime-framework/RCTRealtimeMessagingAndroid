package com.rctrealtimemessagingandroid; /**
 * Created by jcaixinha on 15/09/15.
 */

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import ibt.ortc.api.OnDisablePresence;
import ibt.ortc.api.OnEnablePresence;
import ibt.ortc.api.OnPresence;
import ibt.ortc.api.Ortc;
import ibt.ortc.api.Presence;
import ibt.ortc.extensibility.OnConnected;
import ibt.ortc.extensibility.OnDisconnected;
import ibt.ortc.extensibility.OnException;
import ibt.ortc.extensibility.OnMessage;
import ibt.ortc.extensibility.OnReconnected;
import ibt.ortc.extensibility.OnReconnecting;
import ibt.ortc.extensibility.OnSubscribed;
import ibt.ortc.extensibility.OnUnsubscribed;
import ibt.ortc.extensibility.OrtcClient;
import ibt.ortc.extensibility.OrtcFactory;
import ibt.ortc.extensibility.exception.OrtcNotConnectedException;


public class RealtimeMessagingAndroid extends ReactContextBaseJavaModule
{

    private static final String TAG = "" ;
    private static  Bundle gCachedExtras;
    private OrtcClient client;
    private HashMap<Integer, OrtcClient> queue;
    private ReadableMap config;
    private static boolean isOnForeground;
    private static RealtimeMessagingAndroid instance;

    public static RealtimeMessagingAndroid instance(){
        return instance;
    }

    public static void setIsOnForeground(boolean isOnForeground){
        RealtimeMessagingAndroid.isOnForeground = isOnForeground;
    }

    public static boolean isOnForeground(){
        return RealtimeMessagingAndroid.isOnForeground;
    }



    public RealtimeMessagingAndroid(ReactApplicationContext reactContext){
        super(reactContext);
        RealtimeMessagingAndroid.instance = this;
        queue = new HashMap<Integer, OrtcClient>();
    }

    @Override
    public String getName() {
        return "RealtimeMessagingAndroid";
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable Object params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static Integer getKeyByValue(Map<Integer, OrtcClient> map, OrtcClient value) {
        for (Map.Entry<Integer, OrtcClient> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }


    @ReactMethod
    public void connect(ReadableMap config, Integer id){
        this.config = config;
        if (this.queue == null)
            queue = new HashMap<Integer, OrtcClient>();

        if (queue.containsKey(id))
        {
            client = (OrtcClient)queue.get(id);
        }else {
            Ortc api = new Ortc();
            OrtcFactory factory = null;

            try {
                factory = api.loadOrtcFactory("IbtRealtimeSJ");
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            client = factory.createClient();
            queue.put(id, client);
        }

        client.setApplicationContext(this.getReactApplicationContext());

        if (this.config.hasKey("projectId"))
            client.setGoogleProjectId(this.config.getString("projectId"));

        if (this.config.hasKey("connectionMetadata"))
            client.setConnectionMetadata(this.config.getString("connectionMetadata"));

        if(this.config.hasKey("clusterUrl")){
            client.setClusterUrl(this.config.getString("clusterUrl"));
        } else if(this.config.hasKey("url")){
            client.setUrl(this.config.getString("url"));
        }

        client.onConnected = new OnConnected() {
            @Override
            public void run(OrtcClient ortcClient) {
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onConnected", null);
            }
        };

        client.onDisconnected = new OnDisconnected() {
            @Override
            public void run(OrtcClient ortcClient) {
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onDisconnected", null);
            }
        };

        client.onException = new OnException() {
            @Override
            public void run(OrtcClient ortcClient, Exception e) {
                WritableMap params = new WritableNativeMap();
                params.putString("error", e.toString());
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onException", params);
            }
        };

        client.onSubscribed = new OnSubscribed() {
            @Override
            public void run(OrtcClient ortcClient, String s) {
                checkForNotifications();
                WritableMap params = new WritableNativeMap();
                params.putString("channel", s);
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onSubscribed", params);
            }
        };

        client.onUnsubscribed = new OnUnsubscribed() {
            @Override
            public void run(OrtcClient ortcClient, String s) {
                WritableMap params = new WritableNativeMap();
                params.putString("channel", s);
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onUnsubscribed", params);
            }
        };

        client.onReconnected = new OnReconnected() {
            @Override
            public void run(OrtcClient ortcClient) {
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onReconnected", null);
            }
        };

        client.onReconnecting = new OnReconnecting() {
            @Override
            public void run(OrtcClient ortcClient) {
                String thisId = ""+RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                sendEvent(getReactApplicationContext(), thisId + "-onReconnecting", null);
            }
        };

        client.connect(this.config.getString("appKey"), this.config.getString("token"));

    }

    @ReactMethod
    public void disconnect(Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.disconnect();
        }
    }

    @ReactMethod
    public void subscribe(String channel, Boolean subscribeOnReconnect, Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.subscribe(channel, subscribeOnReconnect, new OnMessage() {
                @Override
                public void run(OrtcClient ortcClient, String s, String s1) {
                    WritableMap params = new WritableNativeMap();
                    params.putString("channel", s);
                    params.putString("message", s1);
                    String thisId = "" + RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                    sendEvent(getReactApplicationContext(), thisId + "-onMessage", params);
                }
            });
        }
    }

    @ReactMethod
    public void subscribeWithNotifications(String channel, Boolean subscribeOnReconnect, Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.subscribeWithNotifications(channel, subscribeOnReconnect, new OnMessage() {
                @Override
                public void run(OrtcClient ortcClient, String s, String s1) {
                    WritableMap params = new WritableNativeMap();
                    params.putString("channel", s);
                    params.putString("message", s1);
                    String thisId = "" + RealtimeMessagingAndroid.getKeyByValue(queue, ortcClient);
                    sendEvent(getReactApplicationContext(), thisId + "-onMessage", params);
                }
            });
        }
    }

    @ReactMethod
    public void unsubscribe(String channel, Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.unsubscribe(channel);
        }
    }

    @ReactMethod
    public void sendMessage(String message, String channel, Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.send(channel, message);
        }
    }

    @ReactMethod
    public void enablePresence(String aPrivateKey, String channel, boolean aMetadata, final Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            try {
                client.enablePresence(aPrivateKey, channel, aMetadata, new OnEnablePresence() {
                    @Override
                    public void run(Exception e, String s) {
                        if (e != null){
                            WritableMap params = new WritableNativeMap();
                            params.putString("error", e.toString());
                            sendEvent(getReactApplicationContext(), id + "-onEnablePresence", params);
                        }else{
                            WritableMap params = new WritableNativeMap();
                            params.putString("result", s);
                            sendEvent(getReactApplicationContext(), id + "-onEnablePresence", params);
                        }
                    }
                });
            } catch (OrtcNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void disablePresence(String aPrivateKey, String channel, final Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            try {
                client.disablePresence(aPrivateKey, channel, new OnDisablePresence() {
                    @Override
                    public void run(Exception e, String s) {
                        if (e != null) {
                            WritableMap params = new WritableNativeMap();
                            params.putString("error", e.toString());
                            sendEvent(getReactApplicationContext(), id + "-onDisablePresence", params);
                        } else {
                            WritableMap params = new WritableNativeMap();
                            params.putString("result", s);
                            sendEvent(getReactApplicationContext(), id + "-onDisablePresence", params);
                        }
                    }
                });
            } catch (OrtcNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }

    @ReactMethod
    public void presence(String channel, final Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            try {
                client.presence(channel, new OnPresence() {
                    @Override
                    public void run(Exception e, Presence presence) {
                        if (e != null) {
                            WritableMap params = new WritableNativeMap();
                            params.putString("error", e.toString());
                            sendEvent(getReactApplicationContext(), id + "-onPresence", params);
                        } else {
                            WritableMap params = new WritableNativeMap();
                            params.putString("result", presence.toString());
                            sendEvent(getReactApplicationContext(), id + "-onPresence", params);
                        }
                    }
                });
            } catch (OrtcNotConnectedException e) {
                e.printStackTrace();
            }
        }
    }


    @ReactMethod
    public void isSubscribed(String channel, Integer id, Callback callBack){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            callBack.invoke(client.isSubscribed(channel));
        }
    }

    @ReactMethod
    public void getHeartbeatTime(Integer id, Callback callBack){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            callBack.invoke(client.getHeartbeatTime());
        }
    }

    @ReactMethod
    public void setHeartbeatTime(Integer newTime, Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.setHeartbeatTime(newTime);
        }
    }


    @ReactMethod
    public void getHeartbeatFails(Integer id, Callback callBack){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            callBack.invoke(client.getHeartbeatFails());
        }
    }

    @ReactMethod
    public void setHeartbeatTimeFails(Integer newTime, Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.setHeartbeatFails(newTime);
        }
    }

    @ReactMethod
    public void enableHeartbeat(Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.setHeartbeatActive(true);
        }
    }

    @ReactMethod
    public void disableHeartbeat(Integer id){
        OrtcClient client = null;
        if (queue.containsKey(id)) {
            client = queue.get(id);
            client.setHeartbeatActive(false);
        }
    }

    @ReactMethod
    public void checkForNotifications(){
        if (gCachedExtras != null){
            sendExtras(gCachedExtras);
            gCachedExtras = null;
        }
    }

    public static void sendExtras(Bundle extras) {
        if (extras != null) {
            gCachedExtras = extras;
            if (RealtimeMessagingAndroid.instance != null && RealtimePushNotificationActivity.getMainParent() != null) {
                RealtimeMessagingAndroid.instance.sendJavascript(convertBundleToJson(extras));
            }
        }
    }


    private static JSONObject convertBundleToJson(Bundle extras)
    {
        JSONObject json = new JSONObject();
        try {
            if (extras.containsKey("P")){
                json = new JSONObject();
                json.put("payload", extras.getString("P"));
            }
            else{
                String message = extras.getString("M");
                String newMsg = message.substring(message.indexOf("_", message.indexOf("_") + 1) + 1);
                json.put("message", newMsg);
            }

            if (extras.containsKey("C")){
                json.put("channel", extras.getString("C"));
            }

            Log.v(TAG, "extrasToJSON: " + json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;

    }


    public void sendJavascript(JSONObject json) {
        try {
            String send = "";
            String channel = json.getString("channel");
            try {

                WritableMap params = new WritableNativeMap();

                JSONObject temp = new JSONObject(json.getString("payload"));
                Iterator<String> iter = temp.keys();
                while (iter.hasNext()) {
                    String key = iter.next();
                    try {
                        params.putString(key, temp.getString(key));
                    } catch (JSONException e) {
                        // Something went wrong!
                    }
                }
                WritableMap toSend = new WritableNativeMap();
                toSend.putMap("payload", params);
                sendEvent(getReactApplicationContext(), "onPushNotification", toSend);
                gCachedExtras = null;
            } catch (JSONException ex) {
                WritableMap params = new WritableNativeMap();
                params.putString("message", json.getString("message"));
                params.putString("channel", json.getString("channel"));

                if (queue != null){
                    for (int id : this.queue.keySet()){
                        OrtcClient cl = queue.get(id);
                        if (cl != null && (cl.getIsConnected() && cl.isSubscribed(channel)))
                        {
                            gCachedExtras = null;
                        }
                    }
                }
                if (gCachedExtras != null){
                    sendEvent(getReactApplicationContext(), "onPushNotification", params);
                    gCachedExtras = null;
                }

            }
            Log.i(TAG, "sendJavascript: " + send);

        } catch (Exception e) {
            Log.e(TAG, "sendJavascript: JSON exception");
        }
    }


}
