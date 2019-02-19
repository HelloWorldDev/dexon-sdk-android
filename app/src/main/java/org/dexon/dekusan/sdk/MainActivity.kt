package org.dexon.dekusan.sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.squareup.moshi.Moshi
import dekusan.*
import org.dexon.dekusan.core.model.Address
import pm.gnosis.eip712.DomainWithMessage
import pm.gnosis.eip712.EIP712JsonAdapter
import pm.gnosis.eip712.EIP712JsonParser
import pm.gnosis.eip712.adapters.moshi.MoshiAdapter
import pm.gnosis.utils.hexStringToByteArray
import java.math.BigDecimal
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private var signMessageCall: Call<SignMessageRequest>? = null
    private var signPersonalMessageCall: Call<SignPersonalMessageRequest>? = null
    private var signTypedMessageCall: Call<SignTypedMessageRequest>? = null
    private var signTransactionCall: Call<SignTransactionRequest>? = null
    private var sendTransactionCall: Call<SendTransactionRequest>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.sign_transaction).setOnClickListener {
            sendTransactionCall = DekuSan.sendTransaction()
                .recipient(Address("0x3637a62430C67Fe822f7136D2d9D74bDDd7A26C1"))
                .gasPrice(BigInteger.valueOf(16000000000))
                .gasLimit(21000)
                .value(BigDecimal.valueOf(0.3).multiply(BigDecimal.TEN.pow(18)).toBigInteger())
                .nonce(0)
                .payload("0x")
                .call(this)
/*
            signTransactionCall = DekuSan.signTransaction()
                .recipient(Address("0x3637a62430C67Fe822f7136D2d9D74bDDd7A26C1"))
                .gasPrice(BigInteger.valueOf(16000000000))
                .gasLimit(21000)
                .value(BigDecimal.valueOf(0.3).multiply(BigDecimal.TEN.pow(18)).toBigInteger())
                .nonce(0)
                .payload("0x")
                .call(this)
*/
        }

        findViewById<Button>(R.id.sign_message).setOnClickListener {
            signMessageCall = DekuSan.signMessage()
                .message("Hello world!!!")
                .call(this)
        }
        findViewById<Button>(R.id.sign_msg_with_callback).setOnClickListener {
            signMessageCall = DekuSan.signMessage()
                .message("Hello world!!!")
                .callbackUri(Uri.parse("https://google.com/search?q=dexon").toString())
                .call(this)
        }
        findViewById<Button>(R.id.sign_personal_message).setOnClickListener {
            signPersonalMessageCall = DekuSan.signPersonalMessage()
                .message("personal message to be signed")
                .call(this)
        }
        findViewById<Button>(R.id.sign_typed_message).setOnClickListener {
            val inputSource = "{\n" +
                    "  \"types\":{\n" +
                    "    \"EIP712Domain\":[\n" +
                    "      {\n" +
                    "        \"name\":\"name\",\n" +
                    "        \"type\":\"string\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"version\",\n" +
                    "        \"type\":\"string\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"chainId\",\n" +
                    "        \"type\":\"uint256\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"verifyingContract\",\n" +
                    "        \"type\":\"address\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"Person\":[\n" +
                    "      {\n" +
                    "        \"name\":\"name\",\n" +
                    "        \"type\":\"string\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"wallet\",\n" +
                    "        \"type\":\"address\"\n" +
                    "      }\n" +
                    "    ],\n" +
                    "    \"Mail\":[\n" +
                    "      {\n" +
                    "        \"name\":\"from\",\n" +
                    "        \"type\":\"Person\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"to\",\n" +
                    "        \"type\":\"Person\"\n" +
                    "      },\n" +
                    "      {\n" +
                    "        \"name\":\"contents\",\n" +
                    "        \"type\":\"string\"\n" +
                    "      }\n" +
                    "    ]\n" +
                    "  },\n" +
                    "  \"primaryType\":\"Mail\",\n" +
                    "  \"domain\":{\n" +
                    "    \"name\":\"Ether Mail\",\n" +
                    "    \"version\":\"1\",\n" +
                    "    \"chainId\":1,\n" +
                    "    \"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"\n" +
                    "  },\n" +
                    "  \"message\":{\n" +
                    "    \"from\":{\n" +
                    "      \"name\":\"Cow\",\n" +
                    "      \"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"\n" +
                    "    },\n" +
                    "    \"to\":{\n" +
                    "      \"name\":\"Bob\",\n" +
                    "      \"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"\n" +
                    "    },\n" +
                    "    \"contents\":\"Hello,\n" +
                    "     Bob!\"\n" +
                    "  }\n" +
                    "}"
//            val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
//            val adapter = Moshi.Builder().build().adapter(EIP712JsonAdapter.Result::class.java)
//            val json = adapter.toJson(MoshiAdapter().parse(inputSource))
            val typedDataAdapter = Moshi.Builder().build().adapter(MoshiAdapter.TypedData::class.java)
            val typedData = typedDataAdapter.fromJson(inputSource)
            signTypedMessageCall = DekuSan.signTypedMessage()
                .message(typedData)
                .call(this)
        }

        if (savedInstanceState != null) {
            signMessageCall = savedInstanceState.getParcelable("message_sign_call")
            signTransactionCall = savedInstanceState.getParcelable("transaction_sign_call")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        signMessageCall?.let {
            it.onActivityResult(requestCode, resultCode, data, OnCompleteListener { response ->
                val result = response.result ?: ""
                Log.d(
                    "SIGN_TAG",
                    "Data: " + (String(result.hexStringToByteArray()) + "; Error: " + response.error)
                )
            })
        }
        signPersonalMessageCall?.let {
            it.onActivityResult(requestCode, resultCode, data, OnCompleteListener { response ->
                Log.d("SIGN_TAG", "Data: " + response.result + "; Error: " + response.error)
            })
        }
        signTransactionCall?.let {
            it.onActivityResult(requestCode, resultCode, data, OnCompleteListener { response ->
                Log.d("SIGN_TAG", "Data: " + response.result + "; Error: " + response.error)
            })
        }
        signTypedMessageCall?.let {
            it.onActivityResult(requestCode, resultCode, data, OnCompleteListener { response ->
                val result = response.result ?: ""
                Log.d(
                    "SIGN_TAG",
                    "Data: " + (String(result.hexStringToByteArray()) + "; Error: " + response.error)
                )
            })
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        signTransactionCall.let {
            outState!!.putParcelable("transaction_sign_call", signTransactionCall)
        }
        signMessageCall.let {
            outState!!.putParcelable("message_sign_call", signMessageCall)
        }
    }
}
