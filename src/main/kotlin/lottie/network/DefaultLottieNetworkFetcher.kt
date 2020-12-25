package lottie.network

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

public class DefaultLottieNetworkFetcher : LottieNetworkFetcher {
    @Throws(IOException::class)
    public override fun fetchSync(url: String): LottieFetchResult {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()
        return DefaultLottieFetchResult(connection)
    }
}
