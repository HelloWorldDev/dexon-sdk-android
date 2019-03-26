package org.dexon

import android.app.Activity
import android.content.Intent
import android.os.Parcelable
import android.text.TextUtils
import android.util.Base64
import kotlinx.android.parcel.Parcelize
import pm.gnosis.utils.toHexString

@Parcelize
class Call<T : Request>(private val request: T) : Parcelable {

    fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        onCompleteListener: OnCompleteListener<T>
    ) {
        if (requestCode != DekuSan.REQUEST_CODE_SIGN || request.key() != data?.data ?: return) {
            return
        }
        var signHex: String? = null
        var error: Int
        if (resultCode == Activity.RESULT_CANCELED) {
            error = DekuSan.ErrorCode.CANCELED
        } else if (resultCode == DekuSan.RESULT_ERROR) {
            error = data.getIntExtra(
                DekuSan.ExtraKey.ERROR,
                DekuSan.ErrorCode.UNKNOWN
            )
        } else {
            val base64 = data.getStringExtra(DekuSan.ExtraKey.SIGN)
            signHex = Base64.decode(base64, Base64.DEFAULT).toHexString()
            error = data.getIntExtra(
                DekuSan.ExtraKey.ERROR,
                DekuSan.ErrorCode.NONE
            )
            if (error == DekuSan.ErrorCode.NONE && TextUtils.isEmpty(signHex)) {
                error = DekuSan.ErrorCode.INVALID_REQUEST
            }
        }
        val response = Response(request, signHex, error)
        onCompleteListener.onComplete(response)
    }

    override fun describeContents(): Int {
        return 0
    }
}
