package android

import java.io.File

public class Context {
    public fun getAssets(): AssetManager = AssetManager()
    public fun getApplicationContext(): Context {
        TODO("Not yet implemented")
    }

    public fun getCacheDir(): File? {
        TODO("Not yet implemented")
    }
}
