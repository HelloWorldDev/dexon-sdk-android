package dekusan;

import android.app.Activity;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Base64;

import org.dexon.dekusan.core.model.Message;


public final class SignMessageRequest extends BaseSignMessageRequest<String> implements Request, Parcelable {

    public static SignMessageRequest.Builder builder() {
        return new SignMessageRequest.Builder();
    }

    private SignMessageRequest(Message<String> message, Uri callbackUri) {
        super(message, callbackUri);
    }

    private SignMessageRequest(Parcel in) {
        super(in);
    }

    @Override
    byte[] getData() {
        return ((Message<String>) body()).value.getBytes();
    }

    @Override
    String getAuthority() {
        return DekuSan.ACTION_SIGN_MESSAGE;
    }

    @Override
    public int describeContents() {
        return super.describeContents();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public static final Creator<SignMessageRequest> CREATOR = new Creator<SignMessageRequest>() {
        @Override
        public SignMessageRequest createFromParcel(Parcel in) {
            return new SignMessageRequest(in);
        }

        @Override
        public SignMessageRequest[] newArray(int size) {
            return new SignMessageRequest[size];
        }
    };

    public static class Builder {
        private String message;
        private long leafPosition;
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
            if (!DekuSan.ACTION_SIGN_MESSAGE.equals(uri.getAuthority())) {
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

        public SignMessageRequest get() {
            Uri callbackUri = null;
            if (!TextUtils.isEmpty(this.callbackUri)) {
                try {
                    callbackUri = Uri.parse(this.callbackUri);
                } catch (Exception ex) { /* Quietly */ }
            }
            Message<String> message = new Message<>(this.message, url);
            return new SignMessageRequest(message, callbackUri);
        }

        public Call<SignMessageRequest> call(Activity activity) {
            return DekuSan.execute(activity, get());
        }
    }
}
