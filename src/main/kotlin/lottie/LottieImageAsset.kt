package lottie

import android.Bitmap

public class LottieImageAsset(
    public val width: Int,
    public val height: Int,
    public val id: String,
    public val fileName: String,
    public val dirName: String,
) {

    public var bitmap: Bitmap? = null

    public fun hasBitmap(): Boolean {
        return bitmap != null || fileName.startsWith("data:") && fileName.indexOf("base64,") > 0
    }
}
