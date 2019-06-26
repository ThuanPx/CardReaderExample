package com.example.cardreaderexample

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.os.Bundle
import android.util.Log


/**
 * Created by ThuanPx on 2019-06-26.
 */
class QRCodeActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {

    private var mScannerView: ZXingScannerView? = null

    public override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    public override fun onResume() {
        super.onResume()
        mScannerView?.setResultHandler(this)
        mScannerView?.startCamera()
    }

    public override fun onPause() {
        super.onPause()
        mScannerView?.stopCamera()
    }

    override fun handleResult(rawResult: Result?) {
        // Do something with the result here
        Log.v(TAG, rawResult?.text) // Prints scan results
        val intent = Intent(this, AuthorizingActivity::class.java)
        rawResult?.text?.let {
            intent.putExtra("QRCODE", it)
        }
        startActivity(intent)

    }

    companion object {
        private const val TAG = "QRCodeActivity"
    }
}