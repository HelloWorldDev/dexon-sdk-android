package org.dexon

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.util.Base64
import org.dexon.DekuSan.ErrorCode.CANCELED
import org.dexon.DekuSan.ErrorCode.NONE
import org.dexon.DekuSan.RESULT_ERROR
import org.dexon.dekusan.core.model.Address
import org.dexon.dekusan.core.model.Message
import org.dexon.dekusan.core.model.Transaction
import pm.gnosis.eip712.adapters.moshi.MoshiAdapter

class SignRequestHelper(intent: Intent, callback: Callback) {

    private var request: Request? = null

    var appName: String? = null
        private set

    var id: Int? = null
        private set

    var chain: Blockchain = Blockchain.DEXON
        private set

    val address: Address?
        get() = request?.address

    init {
        val uri = intent.data
        if (isSignUri(uri) && uri?.authority != null) {

            id = uri.getQueryParameter(DekuSan.ExtraKey.ID)?.toIntOrNull()
            appName = uri.getQueryParameter(DekuSan.ExtraKey.NAME)
            chain = Blockchain.values().firstOrNull {
                it.name.equals(
                    uri.getQueryParameter(DekuSan.ExtraKey.BLOCKCHAIN),
                    true
                )
            } ?: Blockchain.DEXON

            val action = uri.authority
            when (action) {
                DekuSan.ACTION_SIGN_MESSAGE -> {
                    request = SignMessageRequest.builder().uri(uri).get()
                    val message = request?.body<Message<String>>()
                    callback.signMessage(request!!.address, message)
                }
                DekuSan.ACTION_SIGN_PERSONAL_MESSAGE -> {
                    request = SignPersonalMessageRequest.builder().uri(uri).get()
                    val message = request?.body<Message<String>>()
                    callback.signPersonalMessage(request!!.address, message)
                }
                DekuSan.ACTION_SIGN_TYPED_MESSAGE -> {
                    request = SignTypedMessageRequest.builder().uri(uri).get()
                    val message = request?.body<Message<MoshiAdapter.TypedData>>()
                    callback.signTypedMessage(request!!.address, message)
                }
                DekuSan.ACTION_SEND_TRANSACTION -> {
                    request = SendTransactionRequest.builder().uri(uri).get()
                    callback.sendTransaction(request?.body() as? Transaction)
                }
            }
        }
    }

/*
    protected constructor(`in`: Parcel) : this() {
        request = `in`.readParcelable(Request::class.java.classLoader)
    }
*/

    fun onSignCancel(activity: Activity) {
        fail(activity, CANCELED)
    }

    fun onSignError(activity: Activity, error: Int) {
        fail(activity, error)
    }

    fun onMessageSigned(activity: Activity, sign: ByteArray) {
        success(activity, sign)
    }

    fun onTransactionSigned(activity: Activity, sign: ByteArray) {
        success(activity, sign)
    }

    fun onTransactionSent(activity: Activity, sign: ByteArray) {
        success(activity, sign)
    }

    private fun success(activity: Activity, sign: ByteArray) {
        result(activity, NONE, sign)
    }

    private fun fail(activity: Activity, error: Int) {
        result(activity, error, null)
    }

    private fun result(activity: Activity, error: Int, sign: ByteArray?) {
        val intent = makeResultIntent(error, sign)
        if (request!!.callbackUri == null) {
            val code: Int
            if (error != NONE) {
                code = if (error == CANCELED) RESULT_CANCELED else RESULT_ERROR
            } else {
                code = RESULT_OK
            }
            activity.setResult(code, intent)
            activity.finish()
        } else if (DekuSan.canStartActivity(activity, intent)) {
            activity.startActivity(intent)
            activity.finish()
        } else {
            AlertDialog.Builder(activity)
                .setTitle("No application found")
                .setMessage("No proper application to handle result")
                .create()
                .show()
        }
    }

    private fun makeResultIntent(error: Int, sign: ByteArray?): Intent {
        var error = error
        val intent = Intent(Intent.ACTION_VIEW)
        var signBase64: String? = null
        if (sign != null && sign.size > 0) {
            signBase64 = String(Base64.encode(sign, Base64.DEFAULT))
        } else if (error == NONE) {
            error = DekuSan.ErrorCode.INVALID_REQUEST
        }
        var data = request!!.key()
        if (request!!.callbackUri != null) {
            val dataBuilder = request!!.callbackUri!!.buildUpon()
                .appendQueryParameter("src", getSrcUri(request!!.key()))
            if (error == NONE) {
                dataBuilder.appendQueryParameter("result", signBase64)
            } else {
                dataBuilder.appendQueryParameter("error", error.toString())
            }
            data = dataBuilder.build()
        }
        intent.data = data
        intent.putExtra(DekuSan.ExtraKey.SIGN, signBase64)
        intent.putExtra(DekuSan.ExtraKey.ERROR, error)
        return intent
    }

    private fun getSrcUri(key: Uri): String {
        return String(Base64.encode(key.toString().toByteArray(), Base64.DEFAULT))
    }

    interface Callback {
        fun signMessage(from: Address?, message: Message<String>?)

        fun signPersonalMessage(from: Address?, message: Message<String>?)

        fun signTypedMessage(from: Address?, message: Message<MoshiAdapter.TypedData>?)

        fun sendTransaction(transaction: Transaction?)
    }

    companion object {

        private val authorities = setOf(
            DekuSan.ACTION_SIGN_MESSAGE,
            DekuSan.ACTION_SIGN_PERSONAL_MESSAGE,
            DekuSan.ACTION_SIGN_TYPED_MESSAGE,
            DekuSan.ACTION_SEND_TRANSACTION
        )

        private fun isSignUri(uri: Uri?): Boolean {
            return (uri != null && "dekusan" == uri.scheme
                    && authorities.contains(uri.authority))
        }
    }
}
