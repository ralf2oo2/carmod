package ralf2oo2.carmod.util;

import org.lwjgl.util.vector.Matrix4f;

public class Math {
    public static Matrix4f convertMatrix(float[] rotationMatrix) {
        Matrix4f matrix4x4 = new Matrix4f();

        matrix4x4.m00 = rotationMatrix[0];
        matrix4x4.m01 = rotationMatrix[3];
        matrix4x4.m02 = rotationMatrix[6];

        matrix4x4.m10 = rotationMatrix[1];
        matrix4x4.m11 = rotationMatrix[4];
        matrix4x4.m12 = rotationMatrix[7];

        matrix4x4.m20 = rotationMatrix[2];
        matrix4x4.m21 = rotationMatrix[5];
        matrix4x4.m22 = rotationMatrix[8];

        matrix4x4.m30 = 0.0f;
        matrix4x4.m31 = 0.0f;
        matrix4x4.m32 = 0.0f;
        matrix4x4.m33 = 1.0f;

        return matrix4x4;
    }

    public static Matrix4f getRelativeRotation(Matrix4f M1, Matrix4f M2) {
        Matrix4f m1Inverse = new Matrix4f(M1);
        m1Inverse.invert();

        Matrix4f result = new Matrix4f();
        Matrix4f.mul(m1Inverse, M2, result);

        return result;
    }
}
