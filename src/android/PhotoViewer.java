package com.sarriaroman.PhotoViewer;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * Class to Open PhotoViewer with the Required Parameters from Cordova
 *
 * - URL
 * - Title
 */
public class PhotoViewer extends CordovaPlugin {

    public static final int PERMISSION_DENIED_ERROR = 20;

    public static final String WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ = Manifest.permission.READ_EXTERNAL_STORAGE;

    public static final int REQ_CODE = 0;

    private static final String ACTION_SHOW = "show";

    private String url;
    private String title;
    private String subtitle;
    private int maxWidth;
    private int maxHeight;
    private JSONArray menu;
    protected CallbackContext callbackContext;

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals(ACTION_SHOW)) {
            this.url = args.getString(0);
            this.title = args.getString(1);
            this.subtitle = args.getString(2);
            this.maxWidth = args.getInt(3);
            this.maxHeight = args.getInt(4);
            this.menu = args.getJSONArray(5);

            this.callbackContext = callbackContext;

            if (cordova.hasPermission(READ) && cordova.hasPermission(WRITE)) {
                this.launchActivity();
            } else {
                this.getPermission();
            }
            return true;
        }
        return false;
    }

    protected void getPermission() {
        cordova.requestPermissions(this, REQ_CODE, new String[]{WRITE, READ});
    }

    protected void launchActivity() throws JSONException {
        Intent i = new Intent(this.cordova.getActivity(), com.sarriaroman.PhotoViewer.PhotoActivity.class);

        i.putExtra("url", this.url);
        i.putExtra("title", this.title);
        i.putExtra("subtitle", this.subtitle);
        i.putExtra("maxWidth", this.maxWidth);
        i.putExtra("maxHeight", this.maxHeight);
        i.putExtra("menu", this.menu.toString());

        this.cordova.getActivity().startActivity(i);
        this.callbackContext.success("");
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
        for(int r:grantResults) {
            if(r == PackageManager.PERMISSION_DENIED) {
                this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }

        switch(requestCode) {
            case REQ_CODE:
                launchActivity();
                break;
        }

    }
}