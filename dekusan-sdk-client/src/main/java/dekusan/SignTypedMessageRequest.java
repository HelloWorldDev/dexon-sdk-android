package dekusan;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.dexon.dekusan.core.model.Message;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

import pm.gnosis.eip712.adapters.moshi.MoshiAdapter;

public final class SignTypedMessageRequest extends BaseSignMessageRequest<MoshiAdapter.TypedData[]> implements Request, Parcelable {

    public static SignTypedMessageRequest.Builder builder() {
        return new SignTypedMessageRequest.Builder();
    }

    private SignTypedMessageRequest(Message<MoshiAdapter.TypedData[]> message, Uri callbackUri) {
        super(Blockchain.DEXON, message, callbackUri);
    }

    private SignTypedMessageRequest(Parcel in) {
        super(in);
    }

    @NotNull
    @Override
    protected byte[] getData() {
        Message<MoshiAdapter.TypedData[]> body = body();
        return new Gson().toJson(body.value).getBytes();
    }

    @NotNull
    @Override
    protected String getAuthority() {
        return DekuSan.ACTION_SIGN_TYPED_MESSAGE;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

/*
    public static final Creator<SignTypedMessageRequest> CREATOR = new Creator<SignTypedMessageRequest>() {
        @Override
        public SignTypedMessageRequest createFromParcel(Parcel in) {
            return new SignTypedMessageRequest(in);
        }

        @Override
        public SignTypedMessageRequest[] newArray(int size) {
            return new SignTypedMessageRequest[size];
        }
    };
*/

    public static class Builder {
        private MoshiAdapter.TypedData[] message;
        private long leafPosition;
        private String callbackUri;
        private String url;

        public Builder message(MoshiAdapter.TypedData... message) {
            this.message = message;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder callbackUri(String callbackUri) {
            this.callbackUri = callbackUri;
            return this;
        }

        public Builder uri(Uri uri) {
            if (!DekuSan.ACTION_SIGN_TYPED_MESSAGE.equals(uri.getAuthority())) {
                throw new IllegalArgumentException("Illegal message");
            }

            String value = uri.getQueryParameter(DekuSan.ExtraKey.MESSAGE);
            Type type = new TypeToken<MoshiAdapter.TypedData[]>() {}.getType();
            String json = new String(Base64.decode(value, Base64.DEFAULT));
            Log.e("JSON", json);
            message = new Gson().fromJson(json, type);
            callbackUri = uri.getQueryParameter(DekuSan.ExtraKey.CALLBACK_URI);
            return this;
        }

        public Builder message(Message<MoshiAdapter.TypedData[]> message) {
            message(message.value).url(message.url);
            return this;
        }

        public SignTypedMessageRequest get() {
            Uri callbackUri = null;
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri);
                } catch (Exception ex) { /* Quietly */ }
            }
            Message<MoshiAdapter.TypedData[]> message = new Message<>(this.message, url);
            return new SignTypedMessageRequest(message, callbackUri);
        }

        public Call<SignTypedMessageRequest> call(Activity activity) {
            return DekuSan.execute(activity, get());
        }
    }
}
