package com.example.cardreaderexample

import android.Manifest
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_authorizing.*
import com.squareup.sdk.reader.ReaderSdk
import com.squareup.sdk.reader.core.CallbackReference
import com.squareup.sdk.reader.authorization.AuthorizeErrorCode
import com.squareup.sdk.reader.authorization.Location
import com.squareup.sdk.reader.core.Result
import com.squareup.sdk.reader.core.ResultError
import android.util.Log
import android.view.View
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions


class AuthorizingActivity : AppCompatActivity() {

    private var authorizeCallbackRef: CallbackReference? = null
    private var authorizationCode = "sq0acp-AHJwPAicj_qc0M-vf4Ef25ptcd2ACYFxLZqqkAkkQ1Q"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authorizing)
        val authManager = ReaderSdk.authorizationManager()
        authorizeCallbackRef = authManager.addAuthorizeCallback(this::onAuthorizeResult)

        methodRequiresTwoPermission()

        if (ReaderSdk.authorizationManager().authorizationState.isAuthorized) {
            goToCheckoutActivity()
        }

        btnQRCode.setOnClickListener {
            val intent = Intent(this@AuthorizingActivity, QRCodeActivity::class.java)
            startActivity(intent)
        }

        btnAuthor.setOnClickListener {
            retrieveAuthorizationCode()
        }

        intent.getStringExtra("QRCODE")?.let {
            authorizationCode = it
            retrieveAuthorizationCode()
        }
    }

    private fun methodRequiresTwoPermission() {
        val perms = arrayOf(Manifest.permission.CAMERA)
        if (EasyPermissions.hasPermissions(this, *perms)) {
        } else {
            // Do not have permissions, request them now
            EasyPermissions.requestPermissions(
                this, "Request Permission",
                123, Manifest.permission.CAMERA
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    private fun retrieveAuthorizationCode() {
        // QR code
        // TODO: Asynchronous code to retrieve a mobile authorization code.
        // Calls onAuthorizationCodeRetrieved(String) with the resulting code.
        onAuthorizationCodeRetrieved(authorizationCode)
    }

    private fun onAuthorizationCodeRetrieved(authorizationCode: String) {
        showAuthorizationInProgress(true)
        ReaderSdk.authorizationManager().authorize(authorizationCode)
    }

    private fun showAuthorizationInProgress(inProgress: Boolean) {
        if (inProgress) {
            loading.visibility = View.VISIBLE
        } else {
            loading.visibility = View.INVISIBLE
        }
    }

    private fun onAuthorizeResult(result: Result<Location, ResultError<AuthorizeErrorCode>>) {
        showAuthorizationInProgress(false)
        if (result.isSuccess) {
            goToCheckoutActivity()
        } else {
            val error = result.error
            when (error.code) {
                AuthorizeErrorCode.NO_NETWORK -> {
                    Log.i("AuthorizeErrorCode", error.message)
                }
                AuthorizeErrorCode.USAGE_ERROR -> {
                    var dialogMessage = error.message
                    dialogMessage += "\n\nDebug Message: " + error.debugMessage
                    Log.i("Auth", error.debugCode + ", " + error.debugMessage)
                    Log.d("Auth Error", dialogMessage)
                }
            }
        }
    }

    private fun goToCheckoutActivity() {
        startActivity(Intent(this, CheckoutActivity::class.java))
    }

    override fun onDestroy() {
        authorizeCallbackRef?.clear()
        super.onDestroy()
    }
}
