# DekuSanSDK-Android
[![](https://jitpack.io/v/DekuSanWallet/DekuSanSdk-android.svg)](https://jitpack.io/#DekuSanWallet/DekuSanSdk-android)

## Getting started

The DekuSanSDK lets you sign Ethereum transactions and messages so that you can bulid a native DApp without having to worry about keys or wallets. Follow these instructions to integrate DekuSanSDK in your native DApp.

## Demo

## Add dependency

1. Add jitpack to your root gradle file at the end of repositories:
```
allprojects {
    repositories {
	...
        maven { url 'https://jitpack.io'}
    }
}
```

2. Add dependency to your module:
```
dependencies {
    implementation 'com.github.DekuSanWallet:DekuSanSDK-Android:$version'
}
```

## Handle DekuSan callbacks

In your signing activity `DekuSan`.

```
import DekuSan
```

Override `onActivityResult` to obtain the signing result. Handle the response data and pass onSuccessListener and onFailureListener.

```
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        signMessageCall?.let {
            it.onActivityResult(requestCode, resultCode, data) {response ->
                Log.d("SIGN_TAG", "Data: " + response.result)
            }
        }
        signTransactionCall?.let {
            it.onActivityResult(requestCode, resultCode, data) {response ->
                Log.d("SIGN_TAG", "Data: " + response.result)
            }
        }
    }
```

### Sign a transaction

To sign a transaction use this code:

```
DekuSan.signTransaction()
    .recipient(Address("0x3637a62430C67Fe822f7136D2d9D74bDDd7A26C1"))
    .gasPrice(BigInteger.valueOf(16).multiply(BigInteger.TEN.pow(9)))
    .gasLimit(21000)
    .value(BigDecimal.valueOf(0.3).multiply(BigDecimal.TEN.pow(18)).toBigInteger())
    .nonce(0)
    .payload("0x")
    .call(this)
```

### Sign a message

To sign a message use this code:

```
DekuSan.signMessage()
    .message("message to be signed")
    .call(this)
```

### Sign a personal message

To sign a personal message use this code:

```
DekuSan.signPersonalMessage()
    .message("message to be signed")
    .call(this)
```

## Example

DekuSan SDK includes an example project with the above code. To run the example project clone the repo and build the project with Android Studio. Run the app on your emulator or device. Make sure that you have DekuSan Wallet installed on the device or simulator to test the full callback flow.

## Authors

 * Maxim Rasputin
 * Marat Subkhankulov

License

DekuSanSDK is available under the MIT license. See the LICENSE file for more info.
