package com.example.android.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.android.R;

public final class NotificationHelper {

    public static final String CHANNEL_ROOM = "room_events";
    public static final int ID_CHARACTER_UPDATED = 1001;

    private NotificationHelper() {}

    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager nm = (NotificationManager)
                ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (nm.getNotificationChannel(CHANNEL_ROOM) != null) return;
        NotificationChannel ch = new NotificationChannel(
                CHANNEL_ROOM,
                "События комнаты",
                NotificationManager.IMPORTANCE_DEFAULT);
        ch.setDescription("Уведомления о действиях мастера в игровой комнате");
        nm.createNotificationChannel(ch);
    }

    public static void notifyCharacterUpdated(Context ctx, String message) {
        ensureChannel(ctx);
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ROOM)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Лист персонажа обновлён")
                .setContentText(message == null ? "Мастер изменил ваш лист" : message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);
        try {
            NotificationManagerCompat.from(ctx).notify(ID_CHARACTER_UPDATED, b.build());
        } catch (SecurityException ignored) {
        }
    }
}
