package com.example.cardreaderexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.squareup.sdk.pos.PosClient
import com.squareup.sdk.pos.PosSdk
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import com.squareup.sdk.pos.ChargeRequest
import com.squareup.sdk.pos.CurrencyCode
import android.app.Activity
import kotlinx.android.synthetic.main.activity_main_for_pos.*


class MainActivityForPOS : AppCompatActivity() {

    private val APPLICATION_ID = "sq0idp-_GUtV4-pNzTQEovLgl5tgw"
    private val CHARGE_REQUEST_CODE = 1
    private var posClient: PosClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_for_pos)
        // Replace APPLICATION_ID with a Square-assigned application ID
        posClient = PosSdk.createClient(this, APPLICATION_ID)

        btstartTransaction.setOnClickListener {
            startTransaction()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // Handle unexpected errors
        if (data == null || requestCode != CHARGE_REQUEST_CODE) {
            Log.e("Error 1",   "Square Point of Sale was uninstalled or stopped working")
            return
        }

        // Handle expected results
        if (resultCode == Activity.RESULT_OK) {
            // Handle success
            val success = posClient?.parseChargeSuccess(data)
            Log.e("Success  ",   "Client transaction ID: " + success?.clientTransactionId)
        } else {
            // Handle expected errors
            val error = posClient?.parseChargeError(data)
            Log.e("Error 3",  "Client transaction ID: " + error?.debugDescription)
        }
        return
    }


    fun startTransaction() {
        val request = ChargeRequest.Builder(
                100,
                CurrencyCode.USD)
                .build()
        try {
            val intent = posClient?.createChargeIntent(request)
            startActivityForResult(intent, CHARGE_REQUEST_CODE)

        } catch (e: ActivityNotFoundException) {
            Log.e("Error", "Square Point of Sale is not installed")
            posClient?.openPointOfSalePlayStoreListing()
        }

    }
}
