package hellfirepvp.astralsorcery.client.util.camera;

/**
 * Port of 1.16.5 {@code ICameraTransformer}: a prioritized camera override managed by {@link ClientCameraManager}.
 */
public interface ICameraTransformer {

    int getPriority();

    void onClientTick();

    ICameraPersistencyFunction getPersistencyFunction();

    void onStartTransforming(float pTicks);

    void onStopTransforming(float pTicks);

    void transformRenderView(float pTicks);

}
