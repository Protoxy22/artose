package ocean.compute;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import compute.ComputeShaderProgram;
import textures.Texture;

public class CombineComputeGL extends ComputeShaderProgram {
    private static final String SOURCE_FILE = "shaders/combineCompute.glsl";

    private int location_textureResolution;
    private int location_texelSizes;
    private int location_lodIndex;

    private int textureWidth;
    private int textureHeight;

    public CombineComputeGL(Texture texture) {
        super(SOURCE_FILE);
        this.textureWidth = texture.getWidth();
        this.textureHeight = texture.getHeight();

        start();
        bindImage(0, texture.getId(), GL15.GL_READ_WRITE);
        GL30.glBindTextureUnit(1, texture.getId());
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
        dispatch(textureWidth, textureHeight, 1);
        GL42.glMemoryBarrier(GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }

    public void loadTextureResolution(int res) {
        loadInt(location_textureResolution, res);
    }
}
