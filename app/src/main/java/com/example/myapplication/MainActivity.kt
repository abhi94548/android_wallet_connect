package com.example.myapplication

import android.app.Activity
import kotlinx.android.synthetic.main.screen_main.*
import android.os.Bundle
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.walletconnect.Session
import org.walletconnect.nullOnThrow


class MainActivity : Activity(), Session.Callback {

    private var txRequest: Long? = null
    private val uiScope = CoroutineScope(Dispatchers.Main)

    override fun onStatus(status: Session.Status) {
        when(status) {
            Session.Status.Approved -> sessionApproved()
            Session.Status.Closed -> sessionClosed()
            Session.Status.Connected -> {
                requestConnectionToWallet()
            }
            Session.Status.Disconnected,
            is Session.Status.Error -> {
                // Do Stuff
            }
        }
    }

    private fun requestConnectionToWallet() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(ExampleApplication.config.toWCUri())
        startActivity(i)
    }

    private fun navigateToWallet() {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse("wc:")
        startActivity(i)
    }


    override fun onMethodCall(call: Session.MethodCall) {
    }
    private fun sessionApproved() {
//        uiScope.launch {
//            screen_main_status.text = "Connected: ${ExampleApplication.session.approvedAccounts()}"
//            screen_main_connect_button.visibility = View.GONE
//            screen_main_disconnect_button.visibility = View.VISIBLE
//            screen_main_tx_button.visibility = View.VISIBLE
//        }
        Log.i("Connected", ExampleApplication.session.approvedAccounts().toString());
    }

    private fun sessionClosed() {
//        uiScope.launch {
//            screen_main_status.text = "Disconnected"
//            screen_main_connect_button.visibility = View.VISIBLE
//            screen_main_disconnect_button.visibility = View.GONE
//            screen_main_tx_button.visibility = View.GONE
//        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_main)
    }

    override fun onStart() {
        super.onStart()
        initialSetup()
        screen_main_connect_button.setOnClickListener {
            ExampleApplication.resetSession()
            ExampleApplication.session.addCallback(this)

        }
        screen_main_disconnect_button.setOnClickListener {
            ExampleApplication.session.kill()
        }
        screen_main_tx_button.setOnClickListener {
            val from = ExampleApplication.session.approvedAccounts()?.first()
                ?: return@setOnClickListener
            val txRequest = System.currentTimeMillis()
            ExampleApplication.session.performMethodCall(
                Session.MethodCall.SignMessage(
                    txRequest,
                    from,
                    "0x9b2055d370f73ec7d8a03e965129118dc8f5bf83"
                ),::handleSign
            )

            this.txRequest = txRequest
            navigateToWallet()
        }
    }

    private fun initialSetup() {
        val session = nullOnThrow { ExampleApplication.session } ?: return
        session.addCallback(this)
        sessionApproved()
    }

    private fun handleResponse(resp: Session.MethodCall.Response) {
        if (resp.id == txRequest) {
            txRequest = null
            Log.i("Response", ((resp.result as? String) ?: "Unknown response"));
//            uiScope.launch {
//                screen_main_response.visibility = View.VISIBLE
//                screen_main_response.text = "Last response: " + ((resp.result as? String) ?: "Unknown response")
//            }
        }
    }

    private fun handleSign(resp: Session.MethodCall.Response) {
        Log.i("Sign message", resp.id.toString());
        if (resp.id == txRequest) {
            txRequest = null
            Log.i("SIgn message", ((resp.result as? String) ?: "Unknown response"));
//            uiScope.launch {
//                screen_main_response.visibility = View.VISIBLE
//                screen_main_response.text = "Last response: " + ((resp.result as? String) ?: "Unknown response")
//            }
        }
    }

    override fun onDestroy() {
        ExampleApplication.session.removeCallback(this)
        super.onDestroy()
    }
}
