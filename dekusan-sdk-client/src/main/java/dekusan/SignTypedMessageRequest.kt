package dekusan

import android.app.Activity
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import org.dexon.dekusan.core.model.Address
import org.dexon.dekusan.core.model.Message
import pm.gnosis.eip712.adapters.moshi.MoshiAdapter
import pm.gnosis.utils.isValidEthereumAddress

class SignTypedMessageRequest : BaseSignMessageRequest<MoshiAdapter.TypedData>, Request,
    Parcelable {

    override val data: ByteArray
        get() {
            val body = body<Message<MoshiAdapter.TypedData>>()
            return Gson().toJson(body!!.value).toByteArray()
        }

    override val authority: String
        get() = DekuSan.ACTION_SIGN_TYPED_MESSAGE

    private constructor(
        id: Int,
        name: String,
        blockchain: Blockchain?,
        from: Address?,
        message: Message<MoshiAdapter.TypedData>,
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
        private var message: MoshiAdapter.TypedData? = null
        private var callbackUri: String? = null
        private var url: String? = null

        fun blockchain(blockchain: Blockchain): Builder {
            this.blockchain = blockchain
            return this
        }

        fun from(from: Address): Builder {
            this.from = from
            return this
        }

        fun message(message: MoshiAdapter.TypedData): Builder {
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
            if (DekuSan.ACTION_SIGN_TYPED_MESSAGE != uri.authority) {
                throw IllegalArgumentException("Illegal message")
            }

            id = uri.getQueryParameter(DekuSan.ExtraKey.ID)?.toIntOrNull() ?: return this
            name = uri.getQueryParameter(DekuSan.ExtraKey.NAME).orEmpty()
            val blockchain = uri.getQueryParameter(DekuSan.ExtraKey.BLOCKCHAIN)
            val from = uri.getQueryParameter(DekuSan.ExtraKey.FROM)
            val value = uri.getQueryParameter(DekuSan.ExtraKey.MESSAGE)
            val json = String(Base64.decode(value, Base64.DEFAULT))
            Log.e("JSON", json)

            blockchain?.let {
                Blockchain.values().firstOrNull { it.toString().equals(blockchain, true) }
                    ?.apply { blockchain(this) }
            }
            from?.takeIf { it.isValidEthereumAddress() }?.apply { from(Address(this)) }
            message = Gson().fromJson<MoshiAdapter.TypedData>(json, MoshiAdapter.TypedData::class.java)
            callbackUri = uri.getQueryParameter(DekuSan.ExtraKey.CALLBACK_URI)
            return this
        }

        fun message(message: Message<MoshiAdapter.TypedData>): Builder {
            message(message.value).url(message.url)
            return this
        }

        fun get(): SignTypedMessageRequest {
            var callbackUri: Uri? = null
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri)
                } catch (ex: Exception) { /* Quietly */
                }

            }
            val message = Message<MoshiAdapter.TypedData>(this.message ?: throw Exception(), url)
            return SignTypedMessageRequest(id, name, blockchain, from, message, callbackUri)
        }

        fun call(activity: Activity): Call<SignTypedMessageRequest>? {
            return DekuSan.execute(activity, get())
        }
    }

    companion object {

        fun builder(): SignTypedMessageRequest.Builder {
            return SignTypedMessageRequest.Builder()
        }
    }
}
