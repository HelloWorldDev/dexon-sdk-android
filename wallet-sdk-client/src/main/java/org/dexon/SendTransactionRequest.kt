package org.dexon

import android.app.Activity
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import androidx.fragment.app.Fragment
import org.dexon.wallet.core.functions.getTokenTransferTo
import org.dexon.wallet.core.functions.isTokenTransfer
import org.dexon.wallet.core.model.Address
import org.dexon.wallet.core.model.ChainId
import org.dexon.wallet.core.model.Transaction
import org.dexon.wallet.core.model.createTransactionWithDefaults
import org.walleth.khex.hexToByteArray
import org.walleth.khex.toHexString
import pm.gnosis.utils.isValidEthereumAddress
import java.math.BigInteger

class SendTransactionRequest : Request, Parcelable {

    override fun getChain(): Blockchain = Blockchain.DEXON

    private val transaction: Transaction?
    private val callbackUri: Uri?
    private val uri: Uri?

    private constructor(
        id: Int,
        name: String,
        blockchain: Blockchain?,
        transaction: Transaction,
        callbackUri: Uri?
    ) {
        this.transaction = transaction
        this.callbackUri = callbackUri
        this.uri = toUri(
            id,
            name,
            blockchain ?: Blockchain.DEXON,
            transaction,
            callbackUri
        )
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

    override fun getAddress(): Address? = transaction?.from

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(transaction, flags)
        dest.writeParcelable(callbackUri, flags)
        dest.writeParcelable(uri, flags)
    }

    class Builder {
        private var id: Int = (0..10000000).random()
        private var name: String = DekuSan.appName
        private var blockchain: Blockchain? = null
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

        fun blockchain(blockchain: Blockchain): Builder {
            this.blockchain = blockchain
            return this
        }

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

        fun leafPosition(leafPosition: Long): Builder {
            this.leafPosition = leafPosition
            return this
        }

        fun transaction(transaction: Transaction): Builder {
            from(transaction.from)
                .recipient(if (transaction.isTokenTransfer()) transaction.getTokenTransferTo() else transaction.to)
                .contractAddress(if (transaction.isTokenTransfer()) transaction.getTokenTransferTo() else null)
                .value(transaction.value)
                .gasLimit(transaction.gasLimit.toLong())
                .gasPrice(transaction.gasPrice)
                .payload(transaction.input)
                .nonce(transaction.nonce?.toLong() ?: BigInteger.ZERO.toLong())
                .leafPosition(transaction.leafPosition ?: 0L)
            return this
        }

        fun uri(uri: Uri): Builder {
            id = uri.getQueryParameter(DekuSan.ExtraKey.ID)?.toIntOrNull() ?: return this
            name = uri.getQueryParameter(DekuSan.ExtraKey.NAME).orEmpty()
            val blockchain = uri.getQueryParameter(DekuSan.ExtraKey.BLOCKCHAIN)
            val from = uri.getQueryParameter(DekuSan.ExtraKey.FROM)
            val recipient = uri.getQueryParameter(DekuSan.ExtraKey.RECIPIENT)
            val value = uri.getQueryParameter(DekuSan.ExtraKey.VALUE)
            val contract = uri.getQueryParameter(DekuSan.ExtraKey.CONTRACT)
            val gasPrice = uri.getQueryParameter(DekuSan.ExtraKey.GAS_PRICE)
            val gasLimit = uri.getQueryParameter(DekuSan.ExtraKey.GAS_LIMIT)
            val nonce = uri.getQueryParameter(DekuSan.ExtraKey.NONCE)

            blockchain?.let {
                Blockchain.values().firstOrNull { it.toString().equals(blockchain, true) }
                    ?.apply { blockchain(this) }
            }
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
            leafPosition =
                uri.getQueryParameter(DekuSan.ExtraKey.LEAF_POSITION)?.toLongOrNull() ?: 0L
            return this
        }

        fun get(): SendTransactionRequest {
            val transaction = createTransactionWithDefaults(
                chain = when (blockchain) {
                    Blockchain.ETHEREUM -> ChainId(4L)
                    else -> ChainId(238L)
                },
                from = from,
                gasLimit = gasLimit.toBigInteger(),
                gasPrice = gasPrice,
                input = payload?.hexToByteArray()?.toList() ?: emptyList(),
                nonce = BigInteger.ZERO,
                to = recipient,
                value = value,
                leafPosition = leafPosition
            )
            var callbackUri: Uri? = null
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri)
                } catch (ex: Exception) { /* Quietly */
                }

            }
            return SendTransactionRequest(id, name, blockchain, transaction, callbackUri)
        }

        fun call(activity: Activity): Call<SendTransactionRequest>? {
            return DekuSan.execute(activity, get())
        }

        fun call(fragment: Fragment): Call<SendTransactionRequest>? {
            return DekuSan.execute(fragment, get())
        }

    }

    companion object {

        private fun toUri(
            id: Int,
            name: String,
            chain: Blockchain,
            transaction: Transaction,
            callbackUri: Uri?
        ): Uri {
            val uriBuilder = Uri.Builder()
                .scheme("dekusan")
                .authority(DekuSan.ACTION_SEND_TRANSACTION)
                .appendQueryParameter(DekuSan.ExtraKey.ID, id.toString())
                .appendQueryParameter(DekuSan.ExtraKey.NAME, name)
                .appendQueryParameter(DekuSan.ExtraKey.BLOCKCHAIN, chain.toString())
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

fun <T : Parcelable> Parcel.readParcelable(creator: Parcelable.Creator<T>): T? {
    return if (readString() != null) creator.createFromParcel(this) else null
}