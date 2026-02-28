package com.oncytech.emilocker.registration

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.oncytech.emilocker.R
import com.oncytech.emilocker.receiver.AdminReceiver

class RegistrationActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var activateButton: Button
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        statusText = findViewById(R.id.statusText)
        activateButton = findViewById(R.id.activateButton)
        
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        checkAdminStatus()

        activateButton.setOnClickListener {
            if (!dpm.isAdminActive(adminComponent)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description))
                startActivityForResult(intent, 100)
            } else {
                startRegistration()
            }
        }
    }

    private fun checkAdminStatus() {
        if (dpm.isAdminActive(adminComponent)) {
            statusText.text = "Device Protection Active\nReady for Registration"
            activateButton.text = "Register Now"
        } else {
            statusText.text = "Device Protection Inactive\nPlease enable Device Admin"
            activateButton.text = "Enable Admin"
        }
    }

    private fun startRegistration() {
        // Here we will handle the API call to /api/agent/register
        // For now, just a toast
        Toast.makeText(this, "Connecting to server...", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            checkAdminStatus()
        }
    }
}
