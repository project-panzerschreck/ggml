package com.llama.rpcapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public class RpcServerService extends Service {
    private static final String TAG = "RpcServerService";
    private static final String CHANNEL_ID = "RpcServerChannel";
    private Thread serverThread;
    private NativeRpcServer nativeServer;

    @Override
    public void onCreate() {
        super.onCreate();
        nativeServer = new NativeRpcServer();
        createNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String host = intent.getStringExtra("host");
        if (host == null) host = "0.0.0.0";
        int port = intent.getIntExtra("port", 50052);
        int threads = intent.getIntExtra("threads", 4);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Llama RPC Server")
                .setContentText("Running on " + host + ":" + port)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .build();

        startForeground(1, notification);

        String finalHost = host;
        serverThread = new Thread(() -> {
            Log.i(TAG, "Starting RPC server thread...");
            nativeServer.startServer(finalHost, port, threads);
            Log.i(TAG, "RPC server thread finished.");
            stopSelf();
        });
        serverThread.start();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed. Note: ggml-rpc server might not stop cleanly without process kill.");
        // RPC server in llama_cpp is currently blocking and doesn't have a clean stop API easily accessible via JNI
        // In a real app, we might need to kill the process to restart.
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "RPC Server Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}
