package lottie.network

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.net.HttpURLConnection

public class DefaultLottieFetchResult(private val connection: HttpURLConnection) : LottieFetchResult {
    override fun isSuccessful(): Boolean {
        return try {
            connection.responseCode / 100 == 2
        } catch (e: IOException) {
            false
        }
    }

    @Throws(IOException::class)
    override fun bodyByteStream(): InputStream {
        return connection.inputStream
    }

    override fun contentType(): String? {
        return connection.contentType
    }

    override fun error(): String? {
        return try {
            if (isSuccessful()) null else """
     Unable to fetch ${connection.url}. Failed with ${connection.responseCode}
     
     """.trimIndent() + getErrorFromConnection(
                connection
            )
        } catch (e: IOException) {
            //TODO Logger.warning("get error failed ", e)
            e.message
        }
    }

    override fun close() {
        connection.disconnect()
    }

    @Throws(IOException::class)
    private fun getErrorFromConnection(connection: HttpURLConnection): String {
        val r = BufferedReader(InputStreamReader(connection.errorStream))
        val error = StringBuilder()
        var line: String?
        try {
            while (r.readLine().also { line = it } != null) {
                error.append(line).append('\n')
            }
        } catch (e: Exception) {
            throw e
        } finally {
            try {
                r.close()
            } catch (e: Exception) {
                // Do nothing.
            }
        }
        return error.toString()
    }
}