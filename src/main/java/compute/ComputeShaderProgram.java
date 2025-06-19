package compute;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL42;
import org.lwjgl.opengl.GL43;

import utils.FileUtils;

public abstract class ComputeShaderProgram {
    private int programID;
    private int shaderID;


    public ComputeShaderProgram(String computeFile) {
        shaderID = GL43.glCreateShader(GL43.GL_COMPUTE_SHADER);
        String shaderSource = FileUtils.readFile(computeFile);
        GL43.glShaderSource(shaderID, shaderSource);
        GL43.glCompileShader(shaderID);
        if (GL43.glGetShaderi(shaderID, GL43.GL_COMPILE_STATUS) == GL43.GL_FALSE) {
            System.out.println(GL43.glGetShaderInfoLog(shaderID, 500));
            throw new RuntimeException("Could not compile compute shader!");
        }

        programID = GL43.glCreateProgram();
        GL43.glAttachShader(programID, shaderID);
        GL43.glLinkProgram(programID);
        GL43.glValidateProgram(programID);
        GL43.glDetachShader(programID, shaderID);
        GL43.glDeleteShader(shaderID);

        getUniformLocations();
    }

    protected abstract void getUniformLocations();

    protected int getUniformLocation(String name) {
        return GL20.glGetUniformLocation(programID, name);
    }

    public void start() {
        GL20.glUseProgram(programID);
    }

    public void stop() {
        GL20.glUseProgram(0);
    }

    public void dispatch(int x, int y, int z) {
        GL43.glDispatchCompute(x, y, z);
    }

    public void bindImage(int unit, int texture, int access) {
        GL42.glBindImageTexture(unit, texture, 0, true, 0, access, GL30.GL_RGBA32F);
    }

    public void cleanUp() {
        stop();
        GL20.glDeleteProgram(programID);
    }

    protected void loadInt(int location, int value) {
        GL20.glUniform1i(location, value);
    }

    protected void loadFloat(int location, float value) {
        GL20.glUniform1f(location, value);
    }

    protected void loadFloatArray(int location, float[] values) {
        GL20.glUniform1fv(location, values);
    }
}
