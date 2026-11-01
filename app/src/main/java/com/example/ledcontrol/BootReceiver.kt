package com.example.ledcontrol

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        val p = c.getSharedPreferences("led", Context.MODE_PRIVATE)
        if (p.getBoolean("enabled", false)) {
            Led.show(
                c,
                p.getInt("color", Color.GREEN),
                p.getBoolean("steady", true),
                p.getInt("on", 500),
                p.getInt("off", 1500)
            )
        }
    }
}
