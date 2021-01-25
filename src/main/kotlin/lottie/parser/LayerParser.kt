package lottie.parser

import android.Rect
import lottie.LottieComposition
import lottie.model.content.ContentModel
import lottie.model.content.Mask
import lottie.model.layer.Layer
import lottie.parser.moshi.JsonReader
import lottie.value.Keyframe
import java.io.IOException
import java.util.ArrayList

public object LayerParser {
    private val NAMES: JsonReader.Options = JsonReader.Options.of(
        "nm",  // 0
        "ind",  // 1
        "refId",  // 2
        "ty",  // 3
        "parent",  // 4
        "sw",  // 5
        "sh",  // 6
        "sc",  // 7
        "ks",  // 8
        "tt",  // 9
        "masksProperties",  // 10
        "shapes",  // 11
        "t",  // 12
        "ef",  // 13
        "sr",  // 14
        "st",  // 15
        "w",  // 16
        "h",  // 17
        "ip",  // 18
        "op",  // 19
        "tm",  // 20
        "cl",  // 21
        "hd" // 22
    )

    public fun parse(composition: LottieComposition): Layer {
        val bounds: Rect = composition.getBounds()
        return Layer(emptyList<ContentModel>(), composition, "__container", -1,
            Layer.LayerType.PRE_COMP, -1, null, emptyList<Mask>(),
            AnimatableTransform(), 0, 0, 0, 0F, 0F,
            bounds.width, bounds.height, null, null, emptyList<Keyframe<Float>>(),
            Layer.MatteType.NONE, null, false)
    }

    private val TEXT_NAMES: JsonReader.Options = JsonReader.Options.of(
        "d",
        "a"
    )
    private val EFFECTS_NAMES: JsonReader.Options = JsonReader.Options.of("nm")
    @Throws(IOException::class)
    public fun parse(reader: JsonReader, composition: LottieComposition): Layer {
        // This should always be set by After Effects. However, if somebody wants to minify
        // and optimize their json, the name isn't critical for most cases so it can be removed.
        var layerName = "UNSET"
        var layerType: Layer.LayerType? = null
        var refId: String? = null
        var layerId: Long = 0
        var solidWidth = 0
        var solidHeight = 0
        var solidColor = 0
        var preCompWidth = 0
        var preCompHeight = 0
        var parentId: Long = -1
        var timeStretch = 1f
        var startFrame = 0f
        var inFrame = 0f
        var outFrame = 0f
        var cl: String? = null
        var hidden = false
        var matteType: Layer.MatteType = Layer.MatteType.NONE
        var transform: AnimatableTransform? = null
        var text: AnimatableTextFrame? = null
        var textProperties: AnimatableTextProperties? = null
        var timeRemapping: AnimatableFloatValue? = null
        val masks: MutableList<Mask> = ArrayList<Mask>()
        val shapes: MutableList<ContentModel> = ArrayList<ContentModel>()
        reader.beginObject()
        while (reader.hasNext()) {
            when (reader.selectName(NAMES)) {
                0 -> layerName = reader.nextString()
                1 -> layerId = reader.nextInt()
                2 -> refId = reader.nextString()
                3 -> {
                    val layerTypeInt: Int = reader.nextInt()
                    layerType = if (layerTypeInt < Layer.LayerType.UNKNOWN.ordinal()) {
                        Layer.LayerType.values().get(layerTypeInt)
                    } else {
                        Layer.LayerType.UNKNOWN
                    }
                }
                4 -> parentId = reader.nextInt()
                5 -> solidWidth = (reader.nextInt() * Utils.dpScale())
                6 -> solidHeight = (reader.nextInt() * Utils.dpScale())
                7 -> solidColor = Color.parseColor(reader.nextString())
                8 -> transform = AnimatableTransformParser.parse(reader, composition)
                9 -> {
                    val matteTypeIndex: Int = reader.nextInt()
                    if (matteTypeIndex >= Layer.MatteType.values().length) {
                        composition.addWarning("Unsupported matte type: $matteTypeIndex")
                        break
                    }
                    matteType = Layer.MatteType.values().get(matteTypeIndex)
                    when (matteType) {
                        LUMA -> composition.addWarning("Unsupported matte type: Luma")
                        LUMA_INVERTED -> composition.addWarning("Unsupported matte type: Luma Inverted")
                    }
                    composition.incrementMatteOrMaskCount(1)
                }
                10 -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        masks.add(MaskParser.parse(reader, composition))
                    }
                    composition.incrementMatteOrMaskCount(masks.size)
                    reader.endArray()
                }
                11 -> {
                    reader.beginArray()
                    while (reader.hasNext()) {
                        val shape: ContentModel = ContentModelParser.parse(reader, composition)
                        if (shape != null) {
                            shapes.add(shape)
                        }
                    }
                    reader.endArray()
                }
                12 -> {
                    reader.beginObject()
                    while (reader.hasNext()) {
                        when (reader.selectName(TEXT_NAMES)) {
                            0 -> text = AnimatableValueParser.parseDocumentData(reader, composition)
                            1 -> {
                                reader.beginArray()
                                if (reader.hasNext()) {
                                    textProperties = AnimatableTextPropertiesParser.parse(reader, composition)
                                }
                                while (reader.hasNext()) {
                                    reader.skipValue()
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
                13 -> {
                    reader.beginArray()
                    val effectNames: MutableList<String> = ArrayList()
                    while (reader.hasNext()) {
                        reader.beginObject()
                        while (reader.hasNext()) {
                            when (reader.selectName(EFFECTS_NAMES)) {
                                0 -> effectNames.add(reader.nextString())
                                else -> {
                                    reader.skipName()
                                    reader.skipValue()
                                }
                            }
                        }
                        reader.endObject()
                    }
                    reader.endArray()
                    composition.addWarning("Lottie doesn't support layer effects. If you are using them for " +
                            " fills, strokes, trim paths etc. then try adding them directly as contents " +
                            " in your shape. Found: " + effectNames)
                }
                14 -> timeStretch = reader.nextDouble()
                15 -> startFrame = reader.nextDouble()
                16 -> preCompWidth = (reader.nextInt() * Utils.dpScale())
                17 -> preCompHeight = (reader.nextInt() * Utils.dpScale())
                18 -> inFrame = reader.nextDouble()
                19 -> outFrame = reader.nextDouble()
                20 -> timeRemapping = AnimatableValueParser.parseFloat(reader, composition, false)
                21 -> cl = reader.nextString()
                22 -> hidden = reader.nextBoolean()
                else -> {
                    reader.skipName()
                    reader.skipValue()
                }
            }
        }
        reader.endObject()

        // Bodymovin pre-scales the in frame and out frame by the time stretch. However, that will
        // cause the stretch to be double counted since the in out animation gets treated the same
        // as all other animations and will have stretch applied to it again.
        inFrame /= timeStretch
        outFrame /= timeStretch
        val inOutKeyframes: MutableList<Keyframe<Float>> = ArrayList<Keyframe<Float>>()
        // Before the in frame
        if (inFrame > 0) {
            val preKeyframe: Keyframe<Float> = Keyframe(composition, 0f, 0f, null, 0f, inFrame)
            inOutKeyframes.add(preKeyframe)
        }

        // The + 1 is because the animation should be visible on the out frame itself.
        outFrame = if (outFrame > 0) outFrame else composition.getEndFrame()
        val visibleKeyframe: Keyframe<Float> = Keyframe(composition, 1f, 1f, null, inFrame, outFrame)
        inOutKeyframes.add(visibleKeyframe)
        val outKeyframe: Keyframe<Float> = Keyframe(
            composition, 0f, 0f, null, outFrame, Float.MAX_VALUE)
        inOutKeyframes.add(outKeyframe)
        if (layerName.endsWith(".ai") || "ai" == cl) {
            composition.addWarning("Convert your Illustrator layers to shape layers.")
        }
        return Layer(shapes, composition, layerName, layerId, layerType, parentId, refId,
            masks, transform, solidWidth, solidHeight, solidColor, timeStretch, startFrame,
            preCompWidth, preCompHeight, text, textProperties, inOutKeyframes, matteType,
            timeRemapping, hidden)
    }
}

