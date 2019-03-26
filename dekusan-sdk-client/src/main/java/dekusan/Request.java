package dekusan;

import android.net.Uri;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import org.dexon.dekusan.core.model.Address;

public interface Request extends Parcelable {

    <T> T body();

    Uri key();

    @Nullable
    Uri getCallbackUri();

    Blockchain getChain();

    Address getAddress();
}
