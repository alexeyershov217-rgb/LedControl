package com.example.ledcontrol

import android.app.Activity
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*

class MainActivity : Activity() {

    private lateinit var prefs: SharedPreferences
    private lateinit var preview: View
    private lateinit var steady: Switch
    private lateinit var onBar: SeekBar
    private lateinit var offBar: SeekBar
    private lateinit var onLbl: TextView
    private lateinit var offLbl: TextView
    private val rgb = ArrayList<SeekBar>()

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun color() = Color.rgb(rgb[0].progress, rgb[1].progress, rgb[2].progress)

    private fun refresh() {
        preview.setBackgroundColor(color())
        val s = steady.isChecked
        onBar.isEnabled = !s; offBar.isEnabled = !s
        onLbl.text = "Горит: ${onBar.progress + 100} мс"
        offLbl.text = "Пауза: ${offBar.progress + 100} мс"
    }

    private fun bar(max: Int, value: Int, parent: LinearLayout): SeekBar {
        val b = SeekBar(this)
        b.max = max
        b.progress = value
        b.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) = refresh()
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        parent.addView(b)
        return b
    }

    private fun label(text: String, parent: LinearLayout): TextView {
        val t = TextView(this)
        t.text = text
        t.setPadding(0, dp(12), 0, 0)
        parent.addView(t)
        return t
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("led", MODE_PRIVATE)
        val saved = prefs.getInt("color", Color.GREEN)

        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(dp(16), dp(16), dp(16), dp(16))
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)

        preview = View(this)
        root.addView(preview, LinearLayout.LayoutParams(MATCH_PARENT, dp(64)))

        // Ползунки R, G, B
        val names = listOf("Красный", "Зелёный", "Синий")
        val init = listOf(Color.red(saved), Color.green(saved), Color.blue(saved))
        for (i in 0..2) {
            label(names[i], root)
            rgb.add(bar(255, init[i], root))
        }

        // Готовые цвета
        label("Готовые цвета", root)
        val presets = listOf(
            "Красный" to 0xFF0000, "Зелёный" to 0x00FF00, "Синий" to 0x0000FF,
            "Жёлтый" to 0xFFFF00, "Голубой" to 0x00FFFF, "Розовый" to 0xFF00FF,
            "Белый" to 0xFFFFFF, "Оранжевый" to 0xFF6600
        )
        val grid = LinearLayout(this)
        grid.orientation = LinearLayout.VERTICAL
        root.addView(grid)
        presets.chunked(4).forEach { chunk ->
            val row = LinearLayout(this)
            chunk.forEach { (name, c) ->
                val b = Button(this)
                b.text = name
                b.textSize = 10f
                b.setBackgroundColor(Color.rgb(Color.red(c), Color.green(c), Color.blue(c)))
                b.setTextColor(if (c == 0xFFFF00 || c == 0xFFFFFF || c == 0x00FFFF || c == 0x00FF00) Color.BLACK else Color.WHITE)
                b.setOnClickListener {
                    rgb[0].progress = Color.red(c); rgb[1].progress = Color.green(c); rgb[2].progress = Color.blue(c)
                }
                row.addView(b, LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f))
            }
            grid.addView(row)
        }

        // Режим
        steady = Switch(this)
        steady.text = "Постоянное свечение (без мигания)"
        steady.isChecked = prefs.getBoolean("steady", true)
        steady.setPadding(0, dp(16), 0, 0)
        steady.setOnCheckedChangeListener { _, _ -> refresh() }
        root.addView(steady)

        onLbl = label("", root)
        onBar = bar(1900, prefs.getInt("on", 500) - 100, root)
        offLbl = label("", root)
        offBar = bar(4900, prefs.getInt("off", 1500) - 100, root)

        // Кнопки
        val apply = Button(this)
        apply.text = "Включить / применить"
        apply.setOnClickListener {
            prefs.edit()
                .putInt("color", color())
                .putBoolean("steady", steady.isChecked)
                .putInt("on", onBar.progress + 100)
                .putInt("off", offBar.progress + 100)
                .putBoolean("enabled", true)
                .apply()
            Led.show(this, color(), steady.isChecked, onBar.progress + 100, offBar.progress + 100)
            Toast.makeText(this, "Готово. Выключите экран — индикатор загорится.", Toast.LENGTH_LONG).show()
        }
        root.addView(apply)

        val stop = Button(this)
        stop.text = "Выключить подсветку"
        stop.setOnClickListener {
            prefs.edit().putBoolean("enabled", false).apply()
            Led.hide(this)
        }
        root.addView(stop)

        val hint = TextView(this)
        hint.text = "Индикатор работает только при выключенном экране. " +
            "Проверьте: Настройки → Уведомления → Индикатор уведомлений (должен быть включён)."
        hint.setPadding(0, dp(16), 0, 0)
        root.addView(hint)

        refresh()
    }
}
