package com.oncytech.emilocker.registration

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.oncytech.emilocker.R
import com.oncytech.emilocker.MainActivity
import com.oncytech.emilocker.api.RegisterRequest
import com.oncytech.emilocker.api.RegisterResponse
import com.oncytech.emilocker.api.RetrofitClient
import com.oncytech.emilocker.receiver.AdminReceiver

class RegistrationActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var activateButton: Button
    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminComponent: ComponentName

    private var enrollmentToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        statusText = findViewById(R.id.statusText)
        activateButton = findViewById(R.id.activateButton)
        
        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminComponent = ComponentName(this, AdminReceiver::class.java)

        // 1. Try to get token from Deep Link
        handleIntent(intent)

        // 2. Try to get token from Android Enterprise Admin Extras
        val legacyExtras = intent.getBundleExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE)
        
        enrollmentToken = enrollmentToken ?: legacyExtras?.getString("enrollmentToken")

        if (enrollmentToken != null) {
            Toast.makeText(this, "Found Token: $enrollmentToken", Toast.LENGTH_LONG).show()
        }

        checkAdminStatus()

        activateButton.setOnClickListener {
            if (!dpm.isAdminActive(adminComponent)) {
                val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, getString(R.string.device_admin_description))
                startActivityForResult(intent, 100)
            } else {
                if (enrollmentToken != null) {
                    startRegistration()
                } else {
                    Toast.makeText(this, "No Enrollment Token found. Please scan QR.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleIntent(intent: Intent) {
        val appLinkData = intent.data
        if (appLinkData != null && appLinkData.pathSegments.size >= 2) {
            // URL format: https://emilocker.oncytech.com/enroll/TOKEN
            enrollmentToken = appLinkData.lastPathSegment
        }
    }

    private fun checkAdminStatus() {
        if (dpm.isAdminActive(adminComponent)) {
            statusText.text = "Device Protection Active\nReady for Registration\nToken: ${enrollmentToken ?: "None"}"
            activateButton.text = "Register Now"
        } else {
            statusText.text = "Device Protection Inactive\nPlease enable Device Admin"
            activateButton.text = "Enable Admin"
        }
    }

    private fun startRegistration() {
        val token = enrollmentToken ?: return
        statusText.text = "Registering device..."
        Log.d("EMILocker", "Starting registration for token: $token")
        
        val request = RegisterRequest(
            enrollmentToken = token,
            imei1 = "FETCH_IMEI_LOGIC", // Need permission logic for IMEI
            model = android.os.Build.MODEL,
            manufacturer = android.os.Build.MANUFACTURER,
            osVersion = android.os.Build.VERSION.RELEASE
        )

        Log.d("EMILocker", "Sending Request: $request")

        RetrofitClient.getService().register(request).enqueue(object : retrofit2.Callback<RegisterResponse> {
            override fun onResponse(call: retrofit2.Call<RegisterResponse>, response: retrofit2.Response<RegisterResponse>) {
                val body = response.body()
                Log.d("EMILocker", "Response Received: ${response.code()} - ${body?.message}")
                
                if (response.isSuccessful && body?.success == true) {
                    saveDeviceToken(body.deviceToken)
                    Log.d("EMILocker", "Registration Success. Token Saved.")
                    statusText.text = "Registered Successfully!"
                    Toast.makeText(this@RegistrationActivity, "Security Active", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@RegistrationActivity, MainActivity::class.java))
                    finish()
                } else {
                    Log.e("EMILocker", "Registration Failed: ${body?.message}")
                    statusText.text = "Failed: ${body?.message ?: "Unknown error"}"
                }
            }

            override fun onFailure(call: retrofit2.Call<RegisterResponse>, t: Throwable) {
                Log.e("EMILocker", "Network Error: ${t.message}", t)
                statusText.text = "Connection Error: ${t.message}"
            }
        })
    }

    private fun saveDeviceToken(token: String?) {
        val sharedPref = getSharedPreferences("EMI_LOCKER", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putString("DEVICE_TOKEN", token)
            apply()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            checkAdminStatus()
        }
    }
}
