package org.dexon

import android.app.Activity
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Base64
import androidx.fragment.app.Fragment

import org.dexon.dekusan.core.model.Address
import org.dexon.dekusan.core.model.Message
import pm.gnosis.utils.isValidEthereumAddress

class SignMessageRequest : BaseSignMessageRequest<String>, Request, Parcelable {

    override val data: ByteArray
        get() = (body<Any>() as Message<String>).value.toByteArray()

    override val authority: String
        get() = DekuSan.ACTION_SIGN_MESSAGE

    private constructor(
        id: Int,
        name: String,
        blockchain: Blockchain?,
        from: Address?,
        message: Message<String>,
        callbackUri: Uri?
    ) : super(
        id,
        name,
        blockchain ?: Blockchain.DEXON,
        from,
        message,
        callbackUri
    )

    private constructor(`in`: Parcel) : super(`in`)

    override fun describeContents(): Int {
        return super.describeContents()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
    }

    class Builder {
        private var id: Int = (0..10000000).random()
        private var name: String = DekuSan.appName
        private var blockchain: Blockchain? = null
        private var from: Address? = null
        private var message: String? = null
        private var callbackUri: String? = null
        private var leafPosition: Long = 0
        private var url: String? = null

        fun blockchain(blockchain: Blockchain): Builder {
            this.blockchain = blockchain
            return this
        }

        fun from(from: Address): Builder {
            this.from = from
            return this
        }

        fun message(message: String): Builder {
            this.message = message
            return this
        }

        fun url(url: String?): Builder {
            this.url = url
            return this
        }

        fun callbackUri(callbackUri: String): Builder {
            this.callbackUri = callbackUri
            return this
        }

        fun uri(uri: Uri): Builder {
            if (DekuSan.ACTION_SIGN_MESSAGE != uri.authority) {
                throw IllegalArgumentException("Illegal message")
            }
            id = uri.getQueryParameter(DekuSan.ExtraKey.ID)?.toIntOrNull() ?: return this
            name = uri.getQueryParameter(DekuSan.ExtraKey.NAME).orEmpty()
            val blockchain = uri.getQueryParameter(DekuSan.ExtraKey.BLOCKCHAIN)
            val value = uri.getQueryParameter(DekuSan.ExtraKey.MESSAGE)
            val from = uri.getQueryParameter(DekuSan.ExtraKey.FROM)

            blockchain?.let {
                Blockchain.values().firstOrNull { it.toString().equals(blockchain, true) }
                    ?.apply { blockchain(this) }
            }
            message = String(Base64.decode(value, Base64.DEFAULT))
            from?.takeIf { it.isValidEthereumAddress() }?.apply { from(Address(this)) }
            url = uri.getQueryParameter(DekuSan.ExtraKey.URL)
            callbackUri = uri.getQueryParameter(DekuSan.ExtraKey.CALLBACK_URI)
            leafPosition =
                uri.getQueryParameter(DekuSan.ExtraKey.LEAF_POSITION)?.toLongOrNull() ?: 0L
            return this
        }

        fun message(message: Message<String>): Builder {
            message(message.value).url(message.url)
            message.leafPosition?.apply { leafPosition(this) }
            return this
        }

        fun leafPosition(leafPosition: Long): Builder {
            this.leafPosition = leafPosition
            return this
        }

        fun get(): SignMessageRequest {
            var callbackUri: Uri? = null
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri)
                } catch (ex: Exception) { /* Quietly */
                }
            }
            val message = Message(this.message.orEmpty(), this.url, this.leafPosition)
            return SignMessageRequest(id, name, blockchain, from, message, callbackUri)
        }

        fun call(activity: Activity): Call<SignMessageRequest>? {
            return DekuSan.execute(activity, get())
        }

        fun call(fragment: Fragment): Call<SignMessageRequest>? {
            return DekuSan.execute(fragment, get())
        }

    }

    companion object {

        fun builder(): Builder {
            return Builder()
        }
    }
}
