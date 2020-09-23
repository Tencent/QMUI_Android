package com.qmuiteam.qmui.widget.webview;

import android.util.Pair;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class QMUIWebViewBridgeHandler {
    private static final String MESSAGE_JS_FETCH_SCRIPT = "QMUIBridge._fetchQueueFromNative()";
    private static final String MESSAGE_JS_RESPONSE_SCRIPT = "QMUIBridge._handleResponseFromNative($data$)";
    private static final String MESSAGE_PARAM_HOLDER = "$data$";
    private static final String MESSAGE_CALLBACK_ID = "callbackId";
    private static final String MESSAGE_DATA = "data";
    private static final String MESSAGE_INNER_CMD_NAME = "__cmd__";
    private static final String MESSAGE_CMD_GET_SUPPORTED_CMD_LIST = "getSupportedCmdList";

    private List<Pair<String, ValueCallback<String>>> mStartupMessageList = new ArrayList<>();
    private WebView mWebView;

    public QMUIWebViewBridgeHandler(@NonNull WebView webView) {
        mWebView = webView;
    }

    public final void evaluateBridgeScript(String script, ValueCallback<String> resultCallback) {
        if (mStartupMessageList != null) {
            mStartupMessageList.add(new Pair<>(script, resultCallback));
        } else {
            mWebView.evaluateJavascript(script, resultCallback);
        }
    }

    void onBridgeLoaded() {
        if (mStartupMessageList != null) {
            for (Pair<String, ValueCallback<String>> message : mStartupMessageList) {
                mWebView.evaluateJavascript(message.first, message.second);
            }
            mStartupMessageList = null;
        }
    }


    void fetchAndMessageFromJs() {
        mWebView.evaluateJavascript(MESSAGE_JS_FETCH_SCRIPT, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                String unescaped = unescape(value);
                if (unescaped != null) {
                    try {
                        JSONArray array = new JSONArray(unescaped);
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject message = array.getJSONObject(i);
                            String callbackId = message.getString(MESSAGE_CALLBACK_ID);
                            String msgDataOrigin = message.getString(MESSAGE_DATA);
                            MessageFinishCallback callback = new MessageFinishCallback(callbackId) {
                                @Override
                                public void finish(Object data) {
                                    try{
                                        JSONObject response = new JSONObject();
                                        response.put(MESSAGE_CALLBACK_ID, getCallbackId());
                                        response.put(MESSAGE_DATA, data);
                                        String script = MESSAGE_JS_RESPONSE_SCRIPT.replace(MESSAGE_PARAM_HOLDER, response.toString());
                                        mWebView.evaluateJavascript(script, null);
                                    }catch (Throwable ignore){

                                    }
                                }
                            };
                            try{
                                JSONObject msgData = new JSONObject(msgDataOrigin);
                                String cmdName = msgData.getString(MESSAGE_INNER_CMD_NAME);
                                handleInnerMessage(cmdName, msgData, callback);
                            }catch (Throwable e){
                                handleMessage(msgDataOrigin, callback);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    private void handleInnerMessage(String cmdName, JSONObject jsonObject, MessageFinishCallback callback){
        if(MESSAGE_CMD_GET_SUPPORTED_CMD_LIST.equals(cmdName)){
            callback.finish(new JSONArray(getSupportedCmdList()));
        }else{
            throw new RuntimeException("not a inner api message. fallback to custom message");
        }
    }

    protected abstract List<String> getSupportedCmdList();

    protected abstract void handleMessage(String message, MessageFinishCallback callback);

    @Nullable
    public static String unescape(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        String ret = value.substring(1, value.length() - 1)
                .replace("\\\\", "\\")
                .replace("\\\"", "\"");
        if ("null".equals(ret)) {
            return null;
        }
        return ret;
    }

    @NonNull
    public static String escape(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return "\"null\"";
        }
        String ret = value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
        return "\"" + ret + "\"";
    }


    public abstract class MessageFinishCallback{

        private final String mCallbackId;

        public MessageFinishCallback(String callbackId){
            mCallbackId = callbackId;
        }

        public String getCallbackId() {
            return mCallbackId;
        }

        public abstract void finish(Object data);
    }

}
