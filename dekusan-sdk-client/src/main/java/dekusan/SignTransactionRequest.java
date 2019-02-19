package dekusan;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.dexon.dekusan.core.model.Address;
import org.dexon.dekusan.core.model.Transaction;
import org.walleth.khex.HexFunKt;

import java.math.BigInteger;


public class SignTransactionRequest implements Request, Parcelable {
    private final Transaction transaction;
    private final Uri callbackUri;
    private final Uri uri;

    private SignTransactionRequest(Transaction transaction, Uri callbackUri) {
        this.transaction = transaction;
        this.callbackUri = callbackUri;
        this.uri = toUri(transaction, callbackUri);
    }

    private SignTransactionRequest(Parcel in) {
        transaction = in.readParcelable(Transaction.class.getClassLoader());
        callbackUri = in.readParcelable(Uri.class.getClassLoader());
        uri = in.readParcelable(Uri.class.getClassLoader());
    }

    private static Uri toUri(Transaction transaction, Uri callbackUri) {
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("dekusan")
                .authority(DekuSan.ACTION_SIGN_TRANSACTION)
                /*.appendQueryParameter(DekuSan.ExtraKey.RECIPIENT,
                        transaction.recipient == null ? "" : transaction.recipient.toString())
                .appendQueryParameter(DekuSan.ExtraKey.CONTRACT,
                        transaction.contract == null ? "" : transaction.contract.toString())
                .appendQueryParameter(DekuSan.ExtraKey.VALUE,
                        transaction.value == null ? "0" : transaction.value.toString())
                .appendQueryParameter(DekuSan.ExtraKey.GAS_PRICE,
                        transaction.gasPrice == null ? "0" : transaction.gasPrice.toString())
                .appendQueryParameter(DekuSan.ExtraKey.GAS_LIMIT, String.valueOf(transaction.gasLimit))
                .appendQueryParameter(DekuSan.ExtraKey.NONCE, String.valueOf(transaction.nonce))
                .appendQueryParameter(DekuSan.ExtraKey.PAYLOAD, transaction.payload)
                .appendQueryParameter(DekuSan.ExtraKey.LEAF_POSITION, String.valueOf(transaction.leafPosition))*/;
        if (callbackUri != null) {
            uriBuilder.appendQueryParameter("callback", callbackUri.toString());
        }
        return uriBuilder.build();
    }

    @Override
    public <T> T body() {
        return (T) transaction;
    }

    @Override
    public Uri key() {
        return uri;
    }


    @Nullable
    @Override
    public Uri getCallbackUri() {
        return callbackUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(transaction, flags);
        dest.writeParcelable(callbackUri, flags);
        dest.writeParcelable(uri, flags);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final Creator<SignTransactionRequest> CREATOR = new Creator<SignTransactionRequest>() {
        @Override
        public SignTransactionRequest createFromParcel(Parcel in) {
            return new SignTransactionRequest(in);
        }

        @Override
        public SignTransactionRequest[] newArray(int size) {
            return new SignTransactionRequest[size];
        }
    };

    public static class Builder {

        private Address recipient;
        private BigInteger value = BigInteger.ZERO;
        private BigInteger gasPrice = BigInteger.ZERO;
        private long gasLimit;
        private String payload;
        private Address contract;
        private long nonce;
        private long leafPosition;
        private String callbackUri;

        public Builder recipient(Address recipient) {
            this.recipient = recipient;
            return this;
        }

        public Builder value(BigInteger value) {
            if (value == null) {
                value = BigInteger.ZERO;
            }
            this.value = value;
            return this;
        }

        public Builder gasPrice(BigInteger gasPrice) {
            if (gasPrice == null) {
                gasPrice = BigInteger.ZERO;
            }
            this.gasPrice = gasPrice;
            return this;
        }

        public Builder gasLimit(long gasLimit) {
            this.gasLimit = gasLimit;
            return this;
        }

        public Builder payload(byte[] payload) {
            this.payload = HexFunKt.toHexString(payload, "");
            return this;
        }

        public Builder payload(String payload) {
            this.payload = payload;
            return this;
        }

        public Builder contractAddress(Address contract) {
            this.contract = contract;
            return this;
        }

        public Builder nonce(long nonce) {
            this.nonce = nonce;
            return this;
        }

        public Builder leafPosition(long leafPosition) {
            this.leafPosition = leafPosition;
            return this;
        }

        public Builder callbackUri(String callbackUri) {
            this.callbackUri = callbackUri;
            return this;
        }

        public Builder transaction(Transaction transaction) {
            /*recipient(transaction.recipient)
                    .contractAddress(transaction.contract)
                    .value(transaction.value)
                    .gasLimit(transaction.gasLimit)
                    .gasPrice(transaction.gasPrice)
                    .payload(transaction.payload)
                    .nonce(transaction.nonce)
                    .leafPosition(transaction.leafPosition);*/
            return this;
        }

        public Builder uri(Uri uri) {
            String recipient = uri.getQueryParameter(DekuSan.ExtraKey.RECIPIENT);
            String value = uri.getQueryParameter(DekuSan.ExtraKey.VALUE);
            String contract = uri.getQueryParameter(DekuSan.ExtraKey.CONTRACT);
            String gasPrice = uri.getQueryParameter(DekuSan.ExtraKey.GAS_PRICE);
            String gasLimit = uri.getQueryParameter(DekuSan.ExtraKey.GAS_LIMIT);
            String nonce = uri.getQueryParameter(DekuSan.ExtraKey.NONCE);
            recipient(TextUtils.isEmpty(recipient) ? null : new Address(recipient));
            try {
                value(TextUtils.isEmpty(value) ? BigInteger.ZERO : new BigInteger(value));
            } catch (Exception ex) { /* Quietly */ }
            try {
                gasPrice(TextUtils.isEmpty(gasPrice) ? BigInteger.ZERO : new BigInteger(gasPrice));
            } catch (Exception ex) { /* Quietly */ }
            try {
                gasLimit(Long.valueOf(gasLimit));
            } catch (Exception ex) { /* Quietly */ }
            payload(uri.getQueryParameter(DekuSan.ExtraKey.PAYLOAD));
            contractAddress(TextUtils.isEmpty(contract) ? null : new Address(contract));
            try {
                nonce(Long.valueOf(nonce));
            } catch (Exception ex) { /* Quietly */ }
            callbackUri(uri.getQueryParameter("callback"));
            return this;
        }

        public SignTransactionRequest get() {
            Transaction transaction = new Transaction();
            Uri callbackUri = null;
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri);
                } catch (Exception ex) { /* Quietly */ }
            }
            return new SignTransactionRequest(transaction, callbackUri);
        }

        @Nullable
        public Call<SignTransactionRequest> call(Activity activity) {
            return DekuSan.execute(activity, get());
        }
    }
}
