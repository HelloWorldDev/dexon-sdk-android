package org.dexon.wallet.sdk

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.Moshi
import org.dexon.*
import org.dexon.wallet.core.model.Address
import pm.gnosis.eip712.adapters.moshi.MoshiAdapter
import pm.gnosis.utils.addHexPrefix
import pm.gnosis.utils.hexStringToByteArray
import java.math.BigDecimal
import java.math.BigInteger

class MainActivity : AppCompatActivity() {

    private var signMessageCall: Call<SignMessageRequest>? = null
    private var signPersonalMessageCall: Call<SignPersonalMessageRequest>? = null
    private var signTypedMessageCall: Call<SignTypedMessageRequest>? = null
    private var sendTransactionCall: Call<SendTransactionRequest>? = null
    private var blockchain: Blockchain = Blockchain.DEXON

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        DekuSan.setAppName("myDapp")

        findViewById<RadioGroup>(R.id.radioGroup_network).setOnCheckedChangeListener { _, checkedId ->
            blockchain = when (checkedId) {
                R.id.radioButton_ethereum -> Blockchain.ETHEREUM
                else -> Blockchain.DEXON
            }
        }

        findViewById<Button>(R.id.send_transaction).setOnClickListener {
            sendTransactionCall = DekuSan.sendTransaction()
                .blockchain(blockchain)
//                .from(Address("0x9BCA773A36ECD81e08991982B24497adb7039E17"))
//                .from(Address("0x3346d64b273a0255c10f9fee4bb06a000477af2a"))
                .recipient(Address("0x0f46E6bc4495ad0fCdD806616F1cea0EE2CF230a"))
                .gasPrice(BigInteger.valueOf(16000000000))
                .gasLimit(21000)
                .value(BigDecimal.valueOf(0.3).multiply(BigDecimal.TEN.pow(18)).toBigInteger())
                .nonce(0)
                .payload("0x")
                .call(this)
        }

        findViewById<Button>(R.id.sign_message).setOnClickListener {
            signMessageCall = DekuSan.signMessage()
                .blockchain(blockchain)
                .from(Address("0x9BCA773A36ECD81e08991982B24497adb7039E17"))
//                .from(Address("0x3346d64b273a0255c10f9fee4bb06a000477af2a"))
//                .from(Address("0x3750063d24e9b12691c4e724ecf0e243784071ee"))
//                .from(Address("0xcc3927f890a1e840cd493f0c752565da755f7091"))
                .message("Hello world!!!")
                .call(this)
        }
        findViewById<Button>(R.id.sign_msg_with_callback).setOnClickListener {
            signMessageCall = DekuSan.signMessage()
                .blockchain(blockchain)
                .message("Hello world!!!")
                .callbackUri(Uri.parse("https://google.com/search?q=dexon").toString())
                .call(this)
        }
        findViewById<Button>(R.id.sign_personal_message).setOnClickListener {
            signPersonalMessageCall = DekuSan.signPersonalMessage()
                .blockchain(blockchain)
                .message("Any Message you wanna sign")
                .call(this)
        }
        findViewById<Button>(R.id.sign_typed_message).setOnClickListener {
            val inputSource =
                "{\"types\":{\"EIP712Domain\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"version\",\"type\":\"string\"},{\"name\":\"chainId\",\"type\":\"uint256\"},{\"name\":\"verifyingContract\",\"type\":\"address\"}],\"Person\":[{\"name\":\"name\",\"type\":\"string\"},{\"name\":\"wallet\",\"type\":\"address\"}],\"Mail\":[{\"name\":\"from\",\"type\":\"Person\"},{\"name\":\"to\",\"type\":\"Person\"},{\"name\":\"contents\",\"type\":\"string\"}]},\"primaryType\":\"Mail\",\"domain\":{\"name\":\"Ether Mail\",\"version\":\"1\",\"chainId\":238,\"verifyingContract\":\"0xCcCCccccCCCCcCCCCCCcCcCccCcCCCcCcccccccC\"},\"message\":{\"from\":{\"name\":\"Cow\",\"wallet\":\"0xCD2a3d9F938E13CD947Ec05AbC7FE734Df8DD826\"},\"to\":{\"name\":\"Bob\",\"wallet\":\"0xbBbBBBBbbBBBbbbBbbBbbbbBBbBbbbbBbBbbBBbB\"},\"contents\":\"Hello, Bob!\"}}"
//            val domainWithMessage = EIP712JsonParser(MoshiAdapter()).parseMessage(inputSource)
//            val adapter = Moshi.Builder().build().adapter(EIP712JsonAdapter.Result::class.java)
//            val json = adapter.toJson(MoshiAdapter().parse(inputSource))
            val typedDataAdapter =
                Moshi.Builder().build().adapter(MoshiAdapter.TypedData::class.java)
            val typedData = typedDataAdapter.fromJson(inputSource)
            typedData?.let {
                signTypedMessageCall = DekuSan.signTypedMessage()
                    .blockchain(blockchain)
                    .message(typedData)
                    .call(this)
            }
        }

        if (savedInstanceState != null) {
            signMessageCall = savedInstanceState.getParcelable("message_sign_call")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        sendTransactionCall?.onActivityResult(requestCode, resultCode, data, onComplete())
        signMessageCall?.onActivityResult(requestCode, resultCode, data, onComplete())
        signPersonalMessageCall?.onActivityResult(requestCode, resultCode, data, onComplete())
        signTypedMessageCall?.onActivityResult(requestCode, resultCode, data, onComplete())
    }

    private fun <T : Request> onComplete(): OnCompleteListener<T> =
        OnCompleteListener { response ->
            if (response.isSuccess) {
                println(String(response.result.orEmpty().hexStringToByteArray()).addHexPrefix())
            } else {
                Toast.makeText(this@MainActivity, "Error:${response.error}", Toast.LENGTH_SHORT)
                    .show()
            }
        }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        signMessageCall.let {
            outState.putParcelable("message_sign_call", signMessageCall)
        }
    }
}
