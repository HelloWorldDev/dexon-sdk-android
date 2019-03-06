package dekusan;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;

import org.dexon.dekusan.core.model.Message;
import org.jetbrains.annotations.NotNull;

public final class SignPersonalMessageRequest extends BaseSignMessageRequest<String> implements Request, Parcelable {

    public static SignPersonalMessageRequest.Builder builder() {
        return new SignPersonalMessageRequest.Builder();
    }

    private SignPersonalMessageRequest(Message<String> message, Uri callbackUri) {
        super(Blockchain.DEXON, message, callbackUri);
    }

    private SignPersonalMessageRequest(Parcel in) {
        super(in);
    }

    @NotNull
    @Override
    protected byte[] getData() {
        return ((Message<String>) body()).value.getBytes();
    }

    @NotNull
    @Override
    protected String getAuthority() {
        return DekuSan.ACTION_SIGN_PERSONAL_MESSAGE;
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
    public static final Creator<SignPersonalMessageRequest> CREATOR = new Creator<SignPersonalMessageRequest>() {
        @Override
        public SignPersonalMessageRequest createFromParcel(Parcel in) {
            return new SignPersonalMessageRequest(in);
        }

        @Override
        public SignPersonalMessageRequest[] newArray(int size) {
            return new SignPersonalMessageRequest[size];
        }
    };
*/

    public static class Builder {
        private String message;
        private String callbackUri;
        private String url;

        public Builder message(String message) {
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
            if (!DekuSan.ACTION_SIGN_PERSONAL_MESSAGE.equals(uri.getAuthority())) {
                throw new IllegalArgumentException("Illegal message");
            }

            String value = uri.getQueryParameter(DekuSan.ExtraKey.MESSAGE);
            message = new String(Base64.decode(value, Base64.DEFAULT));
            url = uri.getQueryParameter(DekuSan.ExtraKey.URL);
            callbackUri = uri.getQueryParameter(DekuSan.ExtraKey.CALLBACK_URI);
            return this;
        }

        public Builder message(Message<String> message) {
            message(message.value).url(message.url);
            return this;
        }

        public SignPersonalMessageRequest get() {
            Uri callbackUri = null;
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri);
                } catch (Exception ex) { /* Quietly */ }
            }
            Message<String> message = new Message<>(this.message, url);
            return new SignPersonalMessageRequest(message, callbackUri);
        }

        public Call<SignPersonalMessageRequest> call(Activity activity) {
            return DekuSan.execute(activity, get());
        }
    }
}
