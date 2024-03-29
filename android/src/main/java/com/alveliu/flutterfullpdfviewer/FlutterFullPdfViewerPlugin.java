package com.alveliu.flutterfullpdfviewer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.view.Display;
import android.widget.FrameLayout;

import java.util.Map;

import androidx.annotation.NonNull;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

/**
 * FlutterFullPdfViewerPlugin
 */
public class FlutterFullPdfViewerPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
    static MethodChannel channel;
    private Activity activity;
    private FlutterFullPdfViewerManager flutterFullPdfViewerManager;

    public FlutterFullPdfViewerPlugin () {}

    // /**
    //  * Plugin registration.
    //  */
    // public static void registerWith(Registrar registrar) {
    //     channel = new MethodChannel(registrar.messenger(), "flutter_full_pdf_viewer");
    //     final FlutterFullPdfViewerPlugin instance = new FlutterFullPdfViewerPlugin(registrar.activity());
    //     registrar.addActivityResultListener(instance);
    //     channel.setMethodCallHandler(instance);
    // }

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding binding) {
        channel = new MethodChannel(binding.getFlutterEngine().getDartExecutor(), "flutter_full_pdf_viewer");
        channel.setMethodCallHandler(this);
    }

    @Override
    public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
        this.activity = activityPluginBinding.getActivity();
    }

    @Override
    public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
        this.onAttachedToActivity(activityPluginBinding);
    }

    @Override
    public void onDetachedFromActivity() {
        this.activity = null;
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
        this.onDetachedFromActivity();
    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {}

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        switch (call.method) {
            case "launch":
                openPDF(call, result);
                break;
            case "resize":
                resize(call, result);
                break;
            case "close":
                close(call, result);
                break;
            default:
                result.notImplemented();
                break;
        }
    }

    private void openPDF(MethodCall call, MethodChannel.Result result) {
        String path = call.argument("path");
        if (flutterFullPdfViewerManager == null || flutterFullPdfViewerManager.closed) {
            flutterFullPdfViewerManager = new FlutterFullPdfViewerManager(this.activity);
        }
        FrameLayout.LayoutParams params = buildLayoutParams(call);
        this.activity.addContentView(flutterFullPdfViewerManager.pdfView, params);
        flutterFullPdfViewerManager.openPDF(path);
        result.success(null);
    }

    private void resize(MethodCall call, final MethodChannel.Result result) {
        if (flutterFullPdfViewerManager != null) {
            FrameLayout.LayoutParams params = buildLayoutParams(call);
            flutterFullPdfViewerManager.resize(params);
        }
        result.success(null);
    }

    private void close(MethodCall call, MethodChannel.Result result) {
        if (flutterFullPdfViewerManager != null) {
            flutterFullPdfViewerManager.close(call, result);
            flutterFullPdfViewerManager = null;
        }
    }

    private FrameLayout.LayoutParams buildLayoutParams(MethodCall call) {
        Map<String, Number> rc = call.argument("rect");
        FrameLayout.LayoutParams params;
        if (rc != null) {
            params = new FrameLayout.LayoutParams(dp2px(activity, rc.get("width").intValue()), dp2px(activity, rc.get("height").intValue()));
            params.setMargins(dp2px(activity, rc.get("left").intValue()), dp2px(activity, rc.get("top").intValue()), 0, 0);
        } else {
            Display display = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int width = size.x;
            int height = size.y;
            params = new FrameLayout.LayoutParams(width, height);
        }
        return params;
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
