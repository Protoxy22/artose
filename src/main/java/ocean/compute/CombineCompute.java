package ocean.compute;

import org.lwjgl.opengl.GL43;
import renderEngine.ComputeShaderProgram;
import textures.Texture;

public class CombineCompute {
	private static final String SOURCE_FILE = "compute/combineCompute.comp";
	
	private ComputeShaderProgram shader;
	
	private int loc_textureResolution;
	private int loc_lodIndex;
	private int loc_texelSizes;
	
	private int textureResolution;
	
	public CombineCompute(int textureResolution) {
		this.textureResolution = textureResolution;
		shader = new ComputeShaderProgram(SOURCE_FILE);
		
		// Get uniform locations
		shader.bind();
		loc_textureResolution = shader.getUniformLocation("textureResolution");
		loc_lodIndex = shader.getUniformLocation("lodIndex");
		loc_texelSizes = shader.getUniformLocation("texelSizes");
		shader.unbind();
	}

	public void loadWaveBuffers(Texture texture) {
		// Bind the texture as an image for compute shader access
		shader.bind();
		GL43.glBindImageTexture(0, texture.getId(), 0, true, 0, 
			GL43.GL_READ_WRITE, GL43.GL_RGBA32F);
		shader.loadInt(loc_textureResolution, texture.getWidth());
		shader.unbind();
	}
	
	public void loadTexelSizes(float[] texelSizes) {
		shader.bind();
		shader.loadFloatArray(loc_texelSizes, texelSizes);
		shader.unbind();
	}
	
	public void loadLODIndex(int lodIndex) {
		shader.bind();
		shader.loadInt(loc_lodIndex, lodIndex);
		shader.unbind();
	}
	
	public void execute() {
		shader.bind();
		
		// Calculate work group counts (16x16 local size in shader)
		int numGroupsX = (textureResolution + 15) / 16;
		int numGroupsY = (textureResolution + 15) / 16;
		
		shader.dispatch(numGroupsX, numGroupsY, 1);
		shader.waitForCompletion();
		
		shader.unbind();
	}
	
	public void cleanUp() {
		shader.cleanUp();
	}
}
