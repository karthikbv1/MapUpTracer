package com.karthik.mapuptracer

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SessionDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_session_detail)

        val sessionId = intent.getLongExtra("sessionId", 0L)

        val tvSessionId = findViewById<TextView>(R.id.tvSessionId)
        tvSessionId.text = "Session Details\n\nSession ID: $sessionId"
    }
}