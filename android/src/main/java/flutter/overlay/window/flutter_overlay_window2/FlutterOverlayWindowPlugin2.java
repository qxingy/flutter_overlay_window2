package flutter.overlay.window.flutter_overlay_window2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationManagerCompat;

import java.util.Map;

import io.flutter.FlutterInjector;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.FlutterEngineGroup;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.JSONMessageCodec;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;

public class FlutterOverlayWindowPlugin2 implements
        FlutterPlugin, ActivityAware, BasicMessageChannel.MessageHandler, MethodCallHandler,
        PluginRegistry.ActivityResultListener {

    private MethodChannel channel;
    private Context context;
    private Activity mActivity;
    private BasicMessageChannel<Object> messenger;
    private Result pendingResult;
    final int REQUEST_CODE_FOR_OVERLAY_PERMISSION = 1248;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        this.context = flutterPluginBinding.getApplicationContext();
        channel = new MethodChannel(flutterPluginBinding.getBinaryMessenger(), OverlayConstants2.CHANNEL_TAG);
        channel.setMethodCallHandler(this);

        messenger = new BasicMessageChannel(flutterPluginBinding.getBinaryMessenger(), OverlayConstants2.MESSENGER_TAG,
                JSONMessageCodec.INSTANCE);
        messenger.setMessageHandler(this);

        WindowSetup2.messenger = messenger;
        WindowSetup2.messenger.setMessageHandler(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
        pendingResult = result;
        if (call.method.equals("checkPermission")) {
            result.success(checkOverlayPermission());
        } else if (call.method.equals("requestPermission")) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
                mActivity.startActivityForResult(intent, REQUEST_CODE_FOR_OVERLAY_PERMISSION);
            } else {
                result.success(true);
            }
        } else if (call.method.equals("showOverlay")) {
            if (!checkOverlayPermission()) {
                result.error("PERMISSION", "overlay permission is not enabled", null);
                return;
            }
            Integer height = call.argument("height");
            Integer width = call.argument("width");
            String alignment = call.argument("alignment");
            String flag = call.argument("flag");
            String overlayTitle = call.argument("overlayTitle");
            String overlayContent = call.argument("overlayContent");
            String notificationVisibility = call.argument("notificationVisibility");
            boolean enableDrag = call.argument("enableDrag");
            String positionGravity = call.argument("positionGravity");
            Map<String, Integer> startPosition = call.argument("startPosition");
            int startX = startPosition != null ? startPosition.getOrDefault("x", OverlayConstants2.DEFAULT_XY) : OverlayConstants2.DEFAULT_XY;
            int startY = startPosition != null ? startPosition.getOrDefault("y", OverlayConstants2.DEFAULT_XY) : OverlayConstants2.DEFAULT_XY;


            WindowSetup2.width = width != null ? width : -1;
            WindowSetup2.height = height != null ? height : -1;
            WindowSetup2.enableDrag = enableDrag;
            WindowSetup2.setGravityFromAlignment(alignment != null ? alignment : "center");
            WindowSetup2.setFlag(flag != null ? flag : "flagNotFocusable");
            WindowSetup2.overlayTitle = overlayTitle;
            WindowSetup2.overlayContent = overlayContent == null ? "" : overlayContent;
            WindowSetup2.positionGravity = positionGravity;
            WindowSetup2.setNotificationVisibility(notificationVisibility);

            final Intent intent = new Intent(context, OverlayService2.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("startX", startX);
            intent.putExtra("startY", startY);
            context.startService(intent);
            result.success(null);
        } else if (call.method.equals("isOverlayActive")) {
            result.success(OverlayService2.isRunning);
            return;
        } else if (call.method.equals("isOverlayActive")) {
            result.success(OverlayService2.isRunning);
            return;
        } else if (call.method.equals("moveOverlay")) {
            int x = call.argument("x");
            int y = call.argument("y");
            result.success(OverlayService2.moveOverlay(x, y));
        } else if (call.method.equals("getOverlayPosition")) {
            result.success(OverlayService2.getCurrentPosition());
        } else if (call.method.equals("closeOverlay")) {
            if (OverlayService2.isRunning) {
                final Intent i = new Intent(context, OverlayService2.class);
                context.stopService(i);
                result.success(true);
            }
            return;
        } else {
            result.notImplemented();
        }

    }

    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        channel.setMethodCallHandler(null);
        WindowSetup2.messenger.setMessageHandler(null);
    }

    @Override
    public void onAttachedToActivity(@NonNull ActivityPluginBinding binding) {
        mActivity = binding.getActivity();
        if (FlutterEngineCache.getInstance().get(OverlayConstants2.CACHED_TAG) == null) {
            FlutterEngineGroup enn = new FlutterEngineGroup(context);
            DartExecutor.DartEntrypoint dEntry = new DartExecutor.DartEntrypoint(
                    FlutterInjector.instance().flutterLoader().findAppBundlePath(),
                    "overlayMain2");
            FlutterEngine engine = enn.createAndRunEngine(context, dEntry);
            FlutterEngineCache.getInstance().put(OverlayConstants2.CACHED_TAG, engine);
        }
    }

    @Override
    public void onDetachedFromActivityForConfigChanges() {
    }

    @Override
    public void onReattachedToActivityForConfigChanges(@NonNull ActivityPluginBinding binding) {
        this.mActivity = binding.getActivity();
    }

    @Override
    public void onDetachedFromActivity() {
    }

    @Override
    public void onMessage(@Nullable Object message, @NonNull BasicMessageChannel.Reply reply) {
        BasicMessageChannel overlayMessageChannel = new BasicMessageChannel(
                FlutterEngineCache.getInstance().get(OverlayConstants2.CACHED_TAG)
                        .getDartExecutor(),
                OverlayConstants2.MESSENGER_TAG, JSONMessageCodec.INSTANCE);
        overlayMessageChannel.send(message, reply);
    }

    private boolean checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_FOR_OVERLAY_PERMISSION) {
            pendingResult.success(checkOverlayPermission());
            return true;
        }
        return false;
    }

}
