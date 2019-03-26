package org.dexon

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.dexon.dekusan.core.model.Address
import org.dexon.dekusan.core.model.Message

@Parcelize
open class BaseSignMessageRequest<V>(
    private val id: Int,
    private val name: String,
    private val chain: Blockchain,
    private val from: Address?,
    private val message: Message<V>,
    private val callbackUri: Uri?
) : Request, Parcelable {

    @IgnoredOnParcel
    val uri: Uri

    protected constructor(`in`: Parcel) : this(
        id = `in`.readInt(),
        name = `in`.readString()!!,
        chain = `in`.readParcelable<Blockchain>(Blockchain::class.java.classLoader)!!,
        from = `in`.readParcelable<Address>(Address::class.java.classLoader)!!,
        message = `in`.readParcelable<Message<V>>(Message::class.java.classLoader)!!,
        callbackUri = `in`.readParcelable<Uri>(Uri::class.java.classLoader)!!
    )

    init {
        uri = toUri(chain, message, callbackUri)
    }

    protected open val data: ByteArray
        @get:JvmName("getData")
        get() = ByteArray(0)

    protected open val authority: String
        @get:JvmName("getAuthority")
        get() = "message"


    private fun toUri(chain: Blockchain, message: Message<*>, callbackUri: Uri?): Uri {
        var value = data
        value = Base64.encode(value, Base64.DEFAULT)
        val uriBuilder = Uri.Builder()
            .scheme("dekusan")
            .authority(authority)
            .appendQueryParameter(DekuSan.ExtraKey.ID, id.toString())
            .appendQueryParameter(DekuSan.ExtraKey.NAME, name)
            .appendQueryParameter(DekuSan.ExtraKey.BLOCKCHAIN, chain.toString())
            .appendQueryParameter(DekuSan.ExtraKey.FROM, from?.toString().orEmpty())
            .appendQueryParameter(DekuSan.ExtraKey.MESSAGE, String(value))
            .appendQueryParameter(DekuSan.ExtraKey.URL, message.url)
        if (callbackUri != null) {
            uriBuilder.appendQueryParameter("callback", callbackUri.toString())
        }
        return uriBuilder.build()
    }

    override fun <T> body(): T? {
        return message as T?
    }

    override fun key(): Uri? {
        return uri
    }

    override fun getCallbackUri(): Uri? {
        return callbackUri
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun getChain(): Blockchain = chain

    override fun getAddress(): Address? = from

}
