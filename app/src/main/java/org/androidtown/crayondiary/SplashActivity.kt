package org.androidtown.crayondiary

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import org.androidtown.crayondiary.main.MainActivity

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1000)
    }
}

