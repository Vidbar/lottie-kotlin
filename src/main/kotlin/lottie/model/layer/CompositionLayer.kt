package lottie.model.layer

public class CompositionLayer: BaseLayer()(
    lottieDrawable:LottieDrawable ,
     layerModel: Layer,
    layerModels: List<Layer>,
     composition: LottieComposition
) {

}
