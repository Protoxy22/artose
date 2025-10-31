package renderEngine;

import org.lwjgl.opengl.GL43;
import utils.FileUtils;

/**
 * Manages OpenGL compute shader programs
 */
public class ComputeShaderProgram {
    private int programID;
    private int computeShaderID;
    
    public ComputeShaderProgram(String computeShaderFile) {
        computeShaderID = loadComputeShader(computeShaderFile);
        
        programID = GL43.glCreateProgram();
        GL43.glAttachShader(programID, computeShaderID);
        GL43.glLinkProgram(programID);
        
        // Check for linking errors
        if (GL43.glGetProgrami(programID, GL43.GL_LINK_STATUS) == GL43.GL_FALSE) {
            System.err.println(GL43.glGetProgramInfoLog(programID, 1024));
            System.err.println("Could not link compute shader program!");
            throw new RuntimeException("Failed to link compute shader program");
        }
        
        GL43.glValidateProgram(programID);
        GL43.glDetachShader(programID, computeShaderID);
        GL43.glDeleteShader(computeShaderID);
    }
    
    /**
     * Get the uniform location for a given uniform name
     */
    public int getUniformLocation(String uniformName) {
        return GL43.glGetUniformLocation(programID, uniformName);
    }
    
    /**
     * Bind the compute shader program
     */
    public void bind() {
        GL43.glUseProgram(programID);
    }
    
    /**
     * Unbind the compute shader program
     */
    public void unbind() {
        GL43.glUseProgram(0);
    }
    
    /**
     * Dispatch the compute shader with the given work group sizes
     */
    public void dispatch(int numGroupsX, int numGroupsY, int numGroupsZ) {
        GL43.glDispatchCompute(numGroupsX, numGroupsY, numGroupsZ);
    }
    
    /**
     * Wait for compute shader to finish
     */
    public void waitForCompletion() {
        GL43.glMemoryBarrier(GL43.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
    }
    
    /**
     * Load an integer uniform
     */
    public void loadInt(int location, int value) {
        GL43.glUniform1i(location, value);
    }
    
    /**
     * Load a float uniform
     */
    public void loadFloat(int location, float value) {
        GL43.glUniform1f(location, value);
    }
    
    /**
     * Load a float array uniform
     */
    public void loadFloatArray(int location, float[] values) {
        GL43.glUniform1fv(location, values);
    }
    
    /**
     * Clean up resources
     */
    public void cleanUp() {
        unbind();
        GL43.glDeleteProgram(programID);
    }
    
    /**
     * Load and compile a compute shader from a file
     */
    private static int loadComputeShader(String file) {
        String shaderSource = FileUtils.readFile(file);
        
        int shaderID = GL43.glCreateShader(GL43.GL_COMPUTE_SHADER);
        GL43.glShaderSource(shaderID, shaderSource);
        GL43.glCompileShader(shaderID);
        
        if (GL43.glGetShaderi(shaderID, GL43.GL_COMPILE_STATUS) == GL43.GL_FALSE) {
            System.err.println(GL43.glGetShaderInfoLog(shaderID, 1024));
            System.err.println("Could not compile compute shader!");
            throw new RuntimeException("Failed to compile compute shader: " + file);
        }
        
        return shaderID;
    }
}
