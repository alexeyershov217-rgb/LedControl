package com.example.ledcontrol

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object Led {
    private const val ID = 1

    /** steady = true -> постоянное свечение (on=1, off=0 означает "горит без мигания"). */
    fun show(ctx: Context, color: Int, steady: Boolean, onMs: Int, offMs: Int) {
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(ID) // чтобы система заново применила свечение

        val argb = color or 0xFF000000.toInt()
        val open = PendingIntent.getActivity(ctx, 0, Intent(ctx, MainActivity::class.java), 0)

        @Suppress("DEPRECATION")
        val n = Notification.Builder(ctx)
            .setSmallIcon(android.R.drawable.presence_online)
            .setContentTitle("Подсветка включена")
            .setContentText("Нажмите, чтобы изменить цвет")
            .setContentIntent(open)
            .setOngoing(true)
            .setPriority(Notification.PRIORITY_DEFAULT) // для свечения нужен приоритет не ниже DEFAULT
            .setLights(argb, if (steady) 1 else onMs, if (steady) 0 else offMs)
            .build()
        nm.notify(ID, n)
    }

    fun hide(ctx: Context) {
        (ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(ID)
    }
}
