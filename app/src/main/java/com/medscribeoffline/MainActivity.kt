package com.medscribeoffline

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(32, 48, 32, 32)

        val title = TextView(this)
        title.text = "MedScribeOffline"
        title.textSize = 26f
        layout.addView(title)

        val description = TextView(this)
        description.text = "Offline Medical Transcription"
        description.textSize = 18f
        layout.addView(description)

        val button = Button(this)
        button.text = "Start Transcription"
        button.setOnClickListener {
            Toast.makeText(
                this,
                "Transcription feature will be added next.",
                Toast.LENGTH_LONG
            ).show()
        }
        layout.addView(button)

        setContentView(layout)
    }
}
