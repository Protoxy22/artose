package ocean.compute;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.GL45;

import compute.ComputeShaderProgram;
import textures.Texture;

public class CombineComputeGL extends ComputeShaderProgram {
    private static final String SOURCE_FILE = "shaders/combineCompute.glsl";

    private int location_textureResolution;
    private int location_texelSizes;
    private int location_lodIndex;

    private int textureWidth;
    private int textureHeight;
    private int textureId;

    public CombineComputeGL(Texture texture) {
        super(SOURCE_FILE);
        this.textureWidth = texture.getWidth();
        this.textureHeight = texture.getHeight();
        this.textureId = texture.getId();

        start();
        bindImage(0, textureId, GL15.GL_READ_WRITE);
        GL45.glBindTextureUnit(1, textureId);
        stop();
    }

    @Override
    protected void getUniformLocations() {
        location_textureResolution = getUniformLocation("u_textureResolution");
        location_texelSizes = getUniformLocation("u_texelSizes");
        location_lodIndex = getUniformLocation("u_lodIndex");
    }

    public void loadTexelSizes(float[] texelSizes) {
        loadFloatArray(location_texelSizes, texelSizes);
    }

    public void loadLODIndex(int lodIndex) {
        loadInt(location_lodIndex, lodIndex);
    }

    public void dispatch() {
        start();
        bindImage(0, textureId, GL15.GL_READ_WRITE);
        GL45.glBindTextureUnit(1, textureId);
        dispatch(textureWidth, textureHeight, 1);
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
        stop();
    }

    public void loadTextureResolution(int res) {
        loadInt(location_textureResolution, res);
    }
}
