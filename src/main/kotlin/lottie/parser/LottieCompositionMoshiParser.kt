package lottie.parser

import lottie.parser.moshi.JsonReader
import java.io.IOException
import java.util.ArrayList

public object LottieCompositionMoshiParser {
    private val NAMES: JsonReader.Options = JsonReader.Options.of(
        "w",  // 0
        "h",  // 1
        "ip",  // 2
        "op",  // 3
        "fr",  // 4
        "v",  // 5
        "layers",  // 6
        "assets",  // 7
        "fonts",  // 8
        "chars",  // 9
        "markers" // 10
    )

    @Throws(IOException::class)
    fun parse(reader: JsonReader): LottieComposition {
        val scale: Float = Utils.dpScale()
        var startFrame = 0f
        var endFrame = 0f
        var frameRate = 0f
        val layerMap: LongSparseArray<Layer> = LongSparseArray()
        val layers: MutableList<Layer> = ArrayList<Layer>()
        var width = 0
        var height = 0
        val precomps: MutableMap<String?, List<Layer>> = HashMap<String?, List<Layer>>()
        val images: MutableMap<String, LottieImageAsset> = HashMap<String, LottieImageAsset>()
        val fonts: MutableMap<String, Font> = HashMap<String, Font>()
        val markers: MutableList<Marker> = ArrayList<Marker>()
        val characters: SparseArrayCompat<FontCharacter> = SparseArrayCompat()
        val composition = LottieComposition()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(NAMES)) {
                0 -> width = reader.nextInt()
                1 -> height = reader.nextInt()
                2 -> startFrame = reader.nextDouble()
                3 -> endFrame = reader.nextDouble() as Float - 0.01f
                4 -> frameRate = reader.nextDouble()
                5 -> {
                    val version: String = reader.nextString()
                    val versions = version.split("\\.").toTypedArray()
                    val majorVersion = versions[0].toInt()
                    val minorVersion = versions[1].toInt()
                    val patchVersion = versions[2].toInt()
                    if (!Utils.isAtLeastVersion(majorVersion, minorVersion, patchVersion,
                            4, 4, 0)
                    ) {
                        composition.addWarning("Lottie only supports bodymovin >= 4.4.0")
                    }
                }
                6 -> parseLayers(reader, composition, layers, layerMap)
                7 -> parseAssets(reader, composition, precomps, images)
                8 -> parseFonts(reader, fonts)
                9 -> parseChars(reader, composition, characters)
                10 -> parseMarkers(reader, composition, markers)
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        val scaledWidth = (width * scale).toInt()
        val scaledHeight = (height * scale).toInt()
        val bounds = Rect(0, 0, scaledWidth, scaledHeight)
        composition.init(bounds, startFrame, endFrame, frameRate, layers, layerMap, precomps,
            images, characters, fonts, markers)
        return composition
    }

    @Throws(IOException::class)
    private fun parseLayers(
        reader: JsonReader, composition: LottieComposition,
        layers: MutableList<Layer>, layerMap: LongSparseArray<Layer>,
    ) {
        var imageCount = 0
        reader.beginArray()
        while (reader.hasNext()) {
            val layer: Layer = LayerParser.parse(reader, composition)
            if (layer.getLayerType() === Layer.LayerType.IMAGE) {
                imageCount++
            }
            layers.add(layer)
            layerMap.put(layer.getId(), layer)
            if (imageCount > 4) {
                Logger.warning("You have " + imageCount + " images. Lottie should primarily be " +
                        "used with shapes. If you are using Adobe Illustrator, convert the Illustrator layers" +
                        " to shape layers.")
            }
        }
        reader.endArray()
    }

    var ASSETS_NAMES: JsonReader.Options = JsonReader.Options.of(
        "id",  // 0
        "layers",  // 1
        "w",  // 2
        "h",  // 3
        "p",  // 4
        "u" // 5
    )

    @Throws(IOException::class)
    private fun parseAssets(
        reader: JsonReader, composition: LottieComposition,
        precomps: MutableMap<String?, List<Layer>>, images: MutableMap<String, LottieImageAsset>,
    ) {
        reader.beginArray()
        while (reader.hasNext()) {
            var id: String? = null
            // For precomps
            val layers: MutableList<Layer> = ArrayList<Layer>()
            val layerMap: LongSparseArray<Layer> = LongSparseArray()
            // For images
            var width = 0
            var height = 0
            var imageFileName: String? = null
            var relativeFolder: String? = null
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.selectName(ASSETS_NAMES)) {
                    0 -> id = reader.nextString()
                    1 -> {
                        reader.beginArray()
                        while (reader.hasNext()) {
                            val layer: Layer = LayerParser.parse(reader, composition)
                            layerMap.put(layer.getId(), layer)
                            layers.add(layer)
                        }
                        reader.endArray()
                    }
                    2 -> width = reader.nextInt()
                    3 -> height = reader.nextInt()
                    4 -> imageFileName = reader.nextString()
                    5 -> relativeFolder = reader.nextString()
                    else -> {
                        reader.skipName()
                        reader.skipValue()
                    }
                }
            }
            reader.endObject()
            if (imageFileName != null) {
                val image = LottieImageAsset(width, height, id, imageFileName, relativeFolder)
                images[image.getId()] = image
            } else {
                precomps[id] = layers
            }
        }
        reader.endArray()
    }

    private val FONT_NAMES: JsonReader.Options = JsonReader.Options.of("list")
    @Throws(IOException::class)
    private fun parseFonts(reader: JsonReader, fonts: MutableMap<String, Font>) {
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(FONT_NAMES)) {
                0 -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val font: Font = FontParser.parse(reader)
                        fonts[font.getName()] = font
                    }
                    reader.endArray()
                }
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()
    }

    @Throws(IOException::class)
    private fun parseChars(
        reader: JsonReader, composition: LottieComposition,
        characters: SparseArrayCompat<FontCharacter>,
    ) {
        reader.beginArray()
        while (reader.hasNext()) {
            val character: FontCharacter = FontCharacterParser.parse(reader, composition)
            characters.put(character.hashCode(), character)
        }
        reader.endArray()
    }

    private val MARKER_NAMES: JsonReader.Options = JsonReader.Options.of(
        "cm",
        "tm",
        "dr"
    )

    @Throws(IOException::class)
    private fun parseMarkers(
        reader: JsonReader, composition: LottieComposition, markers: MutableList<Marker>,
    ) {
        reader.beginArray()
        while (reader.hasNext()) {
            var comment: String? = null
            var frame = 0f
            var durationFrames = 0f
            reader.beginObject()
            while (reader.hasNext()) {
                when (reader.selectName(MARKER_NAMES)) {
                    0 -> comment = reader.nextString()
                    1 -> frame = reader.nextDouble()
                    2 -> durationFrames = reader.nextDouble()
                    else -> {
                        reader.skipName()
                        reader.skipValue()
                    }
                }
            }
            reader.endObject()
            markers.add(Marker(comment, frame, durationFrames))
        }
        reader.endArray()
    }
}

