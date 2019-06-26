package com.example.cardreaderexample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.squareup.sdk.reader.ReaderSdk
import kotlinx.android.synthetic.main.activity_checkout.*
import com.squareup.sdk.reader.core.CallbackReference
import com.squareup.sdk.reader.checkout.AdditionalPaymentType
import com.squareup.sdk.reader.checkout.CheckoutParameters
import com.squareup.sdk.reader.checkout.CurrencyCode
import com.squareup.sdk.reader.checkout.Money
import com.squareup.sdk.reader.checkout.CheckoutErrorCode
import com.squareup.sdk.reader.core.ResultError
import com.squareup.sdk.reader.checkout.CheckoutResult
import com.squareup.sdk.reader.core.Result
import com.squareup.sdk.reader.hardware.ReaderSettingsActivityCallback
import com.squareup.sdk.reader.hardware.ReaderSettingsErrorCode
import com.squareup.sdk.reader.authorization.AuthorizationManager
import com.squareup.sdk.reader.authorization.DeauthorizeCallback
import com.squareup.sdk.reader.authorization.DeauthorizeErrorCode


class CheckoutActivity : AppCompatActivity() {

    private var checkoutCallbackRef: CallbackReference? = null
    private var readerSettingsCallbackRef: CallbackReference? = null
    private var deauthorizeCallbackRef: CallbackReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ReaderSdk.authorizationManager().authorizationState.isAuthorized) {
            goToAuthorizeActivity()
        }
        setContentView(R.layout.activity_checkout)

        // checkout || charge
        val checkoutManager = ReaderSdk.checkoutManager()
        checkoutCallbackRef =
            checkoutManager.addCheckoutActivityCallback { onCheckoutResult(it) }

        // reader setting
        val readerManager = ReaderSdk.readerManager()
        readerSettingsCallbackRef = readerManager.addReaderSettingsActivityCallback { onReaderSettingsResult(it) }

        // deauthorize
        val authorizationManager = ReaderSdk.authorizationManager()
        deauthorizeCallbackRef =
            authorizationManager.addDeauthorizeCallback { onDeauthorizeResult(it) }

        btStartCheckout.setOnClickListener {
            startCheckout()
        }

        btSetting.setOnClickListener {
            startReaderSettings()
        }

        btDeauthorize.setOnClickListener {
            deauthorize()
        }
    }

    override fun onDestroy() {
        checkoutCallbackRef?.clear()
        readerSettingsCallbackRef?.clear()
        deauthorizeCallbackRef?.clear()
        super.onDestroy()
    }

    private fun deauthorize() {
        val authorizationManager = ReaderSdk.authorizationManager()
        if (authorizationManager.authorizationState.canDeauthorize()) {
            authorizationManager.deauthorize()
        } else {
            Log.i(
                "Unable to deauthorize",
                "There are Square payments on this device that need to be \" + \"uploaded. Please ensure you are connected to the Internet."
            )
        }
    }

    private fun goToAuthorizeActivity() {
        // TODO: Reader SDK is not authorized, move to Authorize Activity
        this.finish()
    }

    private fun startCheckout() {
        val checkoutManager = ReaderSdk.checkoutManager()
        val amountMoney = Money(100, CurrencyCode.current())
        val parametersBuilder = CheckoutParameters.newBuilder(amountMoney)
        // TODO: check here
        if (BuildConfig.DEBUG) {
            parametersBuilder.additionalPaymentTypes(AdditionalPaymentType.CASH)
//            parametersBuilder.additionalPaymentTypes(AdditionalPaymentType.MANUAL_CARD_ENTRY)
//            parametersBuilder.additionalPaymentTypes(AdditionalPaymentType.OTHER)
        }
        checkoutManager.startCheckoutActivity(this, parametersBuilder.build())
    }

    private fun onCheckoutResult(result: Result<CheckoutResult, ResultError<CheckoutErrorCode>>) {
        if (result.isSuccess) {
            val checkoutResult = result.successValue
            Log.i("checkoutResult", checkoutResult.toString())
        } else {
            val error = result.error

            when (error.code) {
                CheckoutErrorCode.SDK_NOT_AUTHORIZED -> goToAuthorizeActivity()
                CheckoutErrorCode.CANCELED -> Log.i("Checkout canceled", "Checkout canceled")
                CheckoutErrorCode.USAGE_ERROR -> showErrorDialog(error)
            }
        }
    }

    private fun showErrorDialog(error: ResultError<*>) {
        var dialogMessage = error.message
        dialogMessage += "\n\nDebug Message: " + error.debugMessage
        Log.d("Checkout", error.debugCode + ", " + error.debugMessage)
        Log.d("Checkout Error", dialogMessage)
    }

    private fun onReaderSettingsResult(result: Result<Void, ResultError<ReaderSettingsErrorCode>>) {
        if (result.isError) {
            val error = result.error
            when (error.code) {
                ReaderSettingsErrorCode.SDK_NOT_AUTHORIZED -> goToAuthorizeActivity()
                ReaderSettingsErrorCode.USAGE_ERROR -> showErrorDialog(error)
            }
        }
    }

    private fun startReaderSettings() {
        val readerManager = ReaderSdk.readerManager()
        readerManager.startReaderSettingsActivity(this)
    }

    private fun onDeauthorizeResult(
        result: Result<Void, ResultError<DeauthorizeErrorCode>>
    ) {
        if (result.isSuccess) {
            goToAuthorizeActivity()
        } else {
            val error = result.error
            // TODO: check here
            if (error.code.isUsageError) {
                showErrorDialog(error)
            }
        }
    }

}
