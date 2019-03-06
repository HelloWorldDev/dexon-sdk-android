package dekusan

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
enum class Blockchain : Parcelable {
    DEXON, ETHEREUM
}