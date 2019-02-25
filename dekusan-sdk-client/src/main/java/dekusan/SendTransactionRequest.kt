package dekusan

import android.app.Activity
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import org.dexon.dekusan.core.functions.getTokenTransferTo
import org.dexon.dekusan.core.functions.isTokenTransfer
import org.dexon.dekusan.core.model.Address
import org.dexon.dekusan.core.model.ChainDefinition
import org.dexon.dekusan.core.model.Transaction
import org.kethereum.DEFAULT_GAS_LIMIT
import org.kethereum.DEFAULT_GAS_PRICE
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toHexString
import pm.gnosis.utils.isValidEthereumAddress
import java.math.BigInteger

class SendTransactionRequest : Request, Parcelable {
    private val transaction: Transaction?
    private val callbackUri: Uri?
    private val uri: Uri?

    private constructor(transaction: Transaction, callbackUri: Uri?) {
        this.transaction = transaction
        this.callbackUri = callbackUri
        this.uri = toUri(transaction, callbackUri)
    }

    private constructor(`in`: Parcel) {
        transaction = `in`.readParcelable(Transaction::class.java.classLoader)
        callbackUri = `in`.readParcelable(Uri::class.java.classLoader)
        uri = `in`.readParcelable(Uri::class.java.classLoader)
    }

    override fun <T> body(): T {
        return transaction as T
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

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(transaction, flags)
        dest.writeParcelable(callbackUri, flags)
        dest.writeParcelable(uri, flags)
    }

    class Builder {
        private var from: Address? = null
        private var recipient: Address? = null
        private var value = BigInteger.ZERO
        private var gasPrice = BigInteger.ZERO
        private var gasLimit: Long = 0
        private var payload: String? = null
        private var contract: Address? = null
        private var nonce: Long = 0
        private var leafPosition: Long = 0
        private var callbackUri: String? = null

        fun from(from: Address?): Builder {
            this.from = from
            return this
        }

        fun recipient(recipient: Address?): Builder {
            this.recipient = recipient
            return this
        }

        fun value(value: BigInteger?): Builder {
            var value = value
            if (value == null) {
                value = BigInteger.ZERO
            }
            this.value = value
            return this
        }

        fun gasPrice(gasPrice: BigInteger?): Builder {
            var gasPrice = gasPrice
            if (gasPrice == null) {
                gasPrice = BigInteger.ZERO
            }
            this.gasPrice = gasPrice
            return this
        }

        fun gasLimit(gasLimit: Long): Builder {
            this.gasLimit = gasLimit
            return this
        }

        fun payload(payload: List<Byte>): Builder {
            this.payload = payload.toHexString()
            return this
        }

        fun payload(payload: ByteArray): Builder {
            this.payload = payload.toHexString()
            return this
        }

        fun payload(payload: String?): Builder {
            this.payload = payload
            return this
        }

        fun contractAddress(contract: Address?): Builder {
            this.contract = contract
            return this
        }

        fun nonce(nonce: Long): Builder {
            this.nonce = nonce
            return this
        }

        fun callbackUri(callbackUri: String?): Builder {
            this.callbackUri = callbackUri
            return this
        }

        fun transaction(transaction: Transaction): Builder {
            from(transaction.from)
                .recipient(if (transaction.isTokenTransfer()) transaction.getTokenTransferTo() else transaction.to)
                .contractAddress(transaction.getTokenTransferTo())
                .value(transaction.value)
                .gasLimit(transaction.gasLimit.toLong())
                .gasPrice(transaction.gasPrice)
                .payload(transaction.input)
                .nonce(transaction.nonce!!.toLong())
            return this
        }

        fun uri(uri: Uri): Builder {
            val from = uri.getQueryParameter(DekuSan.ExtraKey.FROM)
            val recipient = uri.getQueryParameter(DekuSan.ExtraKey.RECIPIENT)
            val value = uri.getQueryParameter(DekuSan.ExtraKey.VALUE)
            val contract = uri.getQueryParameter(DekuSan.ExtraKey.CONTRACT)
            val gasPrice = uri.getQueryParameter(DekuSan.ExtraKey.GAS_PRICE)
            val gasLimit = uri.getQueryParameter(DekuSan.ExtraKey.GAS_LIMIT)
            val nonce = uri.getQueryParameter(DekuSan.ExtraKey.NONCE)
            from?.takeIf { it.isValidEthereumAddress() }?.apply { from(Address(this)) }
            recipient?.takeIf { it.isValidEthereumAddress() }?.apply { recipient(Address(this)) }
            value(value?.toBigIntegerOrNull() ?: BigInteger.ZERO)
            gasPrice(gasPrice?.toBigIntegerOrNull() ?: BigInteger.ZERO)
            gasLimit(gasLimit?.toLongOrNull() ?: 0L)
            payload(uri.getQueryParameter(DekuSan.ExtraKey.PAYLOAD))
            contract?.takeIf { it.isValidEthereumAddress() }
                ?.apply { contractAddress(Address(this)) }
            nonce(nonce?.toLongOrNull() ?: 0L)
            callbackUri(uri.getQueryParameter("callback"))
            return this
        }

        fun get(): SendTransactionRequest {
            val transaction = createTransactionWithDefaults(
                chain = ChainDefinition(238L, "DXN"),
                from = from,
                gasLimit = gasLimit.toBigInteger(),
                gasPrice = gasPrice,
                input = payload?.hexToByteArray()?.toList() ?: emptyList(),
                nonce = BigInteger.ZERO,
                to = recipient,
                value = value
            )
            var callbackUri: Uri? = null
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri)
                } catch (ex: Exception) { /* Quietly */
                }

            }
            return SendTransactionRequest(transaction, callbackUri)
        }

        fun call(activity: Activity): Call<SendTransactionRequest>? {
            return DekuSan.execute(activity, get())
        }
    }

    companion object {

        private fun toUri(transaction: Transaction, callbackUri: Uri?): Uri {
            val uriBuilder = Uri.Builder()
                .scheme("dekusan")
                .authority(DekuSan.ACTION_SEND_TRANSACTION)
                .appendQueryParameter(
                    DekuSan.ExtraKey.FROM,
                    transaction.from?.toString().orEmpty()
                )
                .appendQueryParameter(
                    DekuSan.ExtraKey.RECIPIENT,
                    transaction.to?.toString().orEmpty()
                )
                .appendQueryParameter(
                    DekuSan.ExtraKey.CONTRACT,
                    if (!transaction.isTokenTransfer()) "" else transaction.to.toString()
                )
                .appendQueryParameter(
                    DekuSan.ExtraKey.VALUE,
                    transaction.value.toString()
                )
                .appendQueryParameter(
                    DekuSan.ExtraKey.GAS_PRICE,
                    transaction.gasPrice.toString()
                )
                .appendQueryParameter(DekuSan.ExtraKey.GAS_LIMIT, transaction.gasLimit.toString())
                .appendQueryParameter(DekuSan.ExtraKey.NONCE, transaction.nonce.toString())
                .appendQueryParameter(DekuSan.ExtraKey.PAYLOAD, transaction.input.toHexString())
            if (callbackUri != null) {
                uriBuilder.appendQueryParameter("callback", callbackUri.toString())
            }
            return uriBuilder.build()
        }

        @JvmStatic
        fun builder(): Builder {
            return Builder()
        }

        @JvmField
        val CREATOR: Parcelable.Creator<SendTransactionRequest> =
            object : Parcelable.Creator<SendTransactionRequest> {
                override fun createFromParcel(`in`: Parcel): SendTransactionRequest {
                    return SendTransactionRequest(`in`)
                }

                override fun newArray(size: Int): Array<SendTransactionRequest?> {
                    return arrayOfNulls(size)
                }
            }
    }
}

fun createTransactionWithDefaults(
    chain: ChainDefinition? = null,
    creationEpochSecond: Long? = null,
    from: Address?,
    gasLimit: BigInteger = DEFAULT_GAS_LIMIT,
    gasPrice: BigInteger = DEFAULT_GAS_PRICE,
    input: List<Byte> = emptyList(),
    nonce: BigInteger? = null,
    to: Address?,
    txHash: String? = null,
    value: BigInteger
) = Transaction(
    chain,
    creationEpochSecond,
    from,
    gasLimit,
    gasPrice,
    input,
    nonce,
    to,
    txHash,
    value
)

fun <T : Parcelable> Parcel.readParcelable(creator: Parcelable.Creator<T>): T? {
    return if (readString() != null) creator.createFromParcel(this) else null
}