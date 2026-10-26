
package com.medscribeoffline

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.content.pm.PackageManager
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.content.Intent
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var transcript: TextView
    private lateinit var status: TextView
    private var listening = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 32, 24, 24)
        }

        val title = TextView(this).apply {
            text = "MedScribe Offline"
            textSize = 26f
            gravity = Gravity.CENTER
        }

        status = TextView(this).apply {
            text = "Ready to transcribe"
            textSize = 16f
            setPadding(0, 20, 0, 20)
        }

        transcript = TextView(this).apply {
            text = "Your transcript will appear here."
            textSize = 18f
            setPadding(12, 12, 12, 12)
        }

        val scroll = ScrollView(this).apply {
            addView(transcript)
        }

        val startButton = Button(this).apply {
            text = "Start Transcription"
            setOnClickListener { startTranscription() }
        }

        val stopButton = Button(this).apply {
            text = "Stop Transcription"
            setOnClickListener { stopTranscription() }
        }

        val clearButton = Button(this).apply {
            text = "Clear Transcript"
            setOnClickListener {
                transcript.text = ""
            }
        }

        layout.addView(title)
        layout.addView(status)
        layout.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )
        layout.addView(startButton)
        layout.addView(stopButton)
        layout.addView(clearButton)

        setContentView(layout)
    }

    private fun startTranscription() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text =
                "Speech recognition is unavailable on this device."
            return
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO), 100
            )
            return
        }

        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        status.text = "Listening..."
                    }

                    override fun onBeginningOfSpeech() {
                        status.text = "Speech detected"
                    }

                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        status.text = "Processing speech..."
                    }

                    override fun onError(error: Int) {
                        listening = false
                        status.text = "Recognition stopped (code $error)"
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION
                        )
                        if (!matches.isNullOrEmpty()) {
                            val previous = transcript.text.toString()
                            val prefix = if (
                                previous == "Your transcript will appear here."
                            ) "" else "$previous\n"
                            transcript.text = prefix + matches[0]
                        }
                        if (listening) listenAgain()
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                }
            )
        }

        listening = true
        listenAgain()
    }

    private fun listenAgain() {
        if (!listening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }

        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            listening = false
            status.text = "Could not start speech recognition."
        }
    }

    private fun stopTranscription() {
        listening = false
        speechRecognizer?.stopListening()
        status.text = "Transcription stopped"
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode, permissions, grantResults
        )
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startTranscription()
        } else {
            status.text = "Microphone permission is required."
        }
    }

    override fun onDestroy() {
        listening = false
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onDestroy()
    }
}
