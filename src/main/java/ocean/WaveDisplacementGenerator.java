package ocean;

import org.lwjgl.opengl.GL30;

import ocean.GerstnerBatchGenerator.GerstnerBatch;
import ocean.compute.CombineComputeGL;
import renderEngine.FrameBufferObject;
import textures.Texture;

public class WaveDisplacementGenerator {
	private class WavelengthFilter {
		private float minWavelength;
		private float maxWavelength;
		
		private WavelengthFilter(float minWavelength, float maxWavelength) {
			this.minWavelength = minWavelength;
			this.maxWavelength = maxWavelength;
		}
		
		public float filter(float wavelength) {
			return (minWavelength <= wavelength && wavelength < maxWavelength) ? 1f : 0f;
		}
	}
	
	private Ocean ocean;
	private GerstnerBatchGenerator batchGenerator;
	
        private FrameBufferObject waveBuffers;

        private CombineComputeGL combineProgram;
	
	public WaveDisplacementGenerator(Ocean ocean) {
		this.ocean = ocean;
		batchGenerator = new GerstnerBatchGenerator(ocean);
		
                waveBuffers = new FrameBufferObject(Ocean.TEXTURE_RESOLUTION, Ocean.TEXTURE_RESOLUTION, Ocean.LOD_COUNT);

                combineProgram = new CombineComputeGL(waveBuffers.getTexture());
                combineProgram.loadTextureResolution(Ocean.TEXTURE_RESOLUTION);
	}
	
	public Texture getWaveDisplacements() {
		return waveBuffers.getTexture();
	}
	
	private WavelengthFilter getWavelengthFilter(int lodIndex) {
		float minWavelength = ocean.getMinWavelength(lodIndex);
		float maxWavelength = 2f * minWavelength;
		
		return new WavelengthFilter(minWavelength, maxWavelength);
	}
	
	public void update() {
		batchGenerator.update();
		
		for (int i = 0; i < Ocean.LOD_COUNT; i++) {
			WavelengthFilter filter = getWavelengthFilter(i);
			renderWaveBuffer(i, filter);
		}
		
		updateLODs();
		
		//  Makes sure OpenGL draw calls are finished, before combining textures
		GL30.glFinish();
		
                for (int i = Ocean.LOD_COUNT - 2 ; i >= 0; i--) {
                        combineProgram.start();
                        combineProgram.loadLODIndex(i);
                        combineProgram.dispatch();
                        combineProgram.stop();
                }
        }

        private void updateLODs() {
                combineProgram.start();
                combineProgram.loadTexelSizes(ocean.getTexelSizes());
                combineProgram.stop();
        }
	
	private void renderWaveBuffer(int lodIndex, WavelengthFilter filter) {
		waveBuffers.bind(lodIndex);
		
		GL30.glClearColor(0f, 0f, 0f, 0f);
		GL30.glClear(GL30.GL_COLOR_BUFFER_BIT);
		
		for (GerstnerBatch batch : batchGenerator.getBatches()) {
			if (!batch.isEnabled()) continue; 
			
			float weight = filter.filter(batch.getWavelength());
			if (weight > 0f) {
				batch.render(weight);
			}
		}
		
		waveBuffers.unbind();
	}
	
	public void cleanUp() {
		batchGenerator.cleanUp();
		waveBuffers.delete();
                combineProgram.cleanUp();
        }
}
