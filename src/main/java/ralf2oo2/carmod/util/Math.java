package ralf2oo2.carmod.util;

import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.util.vector.Matrix4f;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;

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

    public static DVector3C lerpVector(float delta, DVector3C vector3C, DVector3C vector3C2){
        double x = MathHelper.lerp(delta, vector3C.get0(), vector3C2.get0());
        double y = MathHelper.lerp(delta, vector3C.get1(), vector3C2.get1());
        double z = MathHelper.lerp(delta, vector3C.get2(), vector3C2.get2());
        return new DVector3(x, y, z);
    }

    public static Matrix4f lerpMatrix(float delta, Matrix4f matrix1, Matrix4f matrix2) {
        Matrix4f result = new Matrix4f();
        result.m00 = MathHelper.lerp(delta, matrix1.m00, matrix2.m00);
        result.m01 = MathHelper.lerp(delta, matrix1.m01, matrix2.m01);
        result.m02 = MathHelper.lerp(delta, matrix1.m02, matrix2.m02);
        result.m03 = MathHelper.lerp(delta, matrix1.m03, matrix2.m03);
        result.m10 = MathHelper.lerp(delta, matrix1.m10, matrix2.m10);
        result.m11 = MathHelper.lerp(delta, matrix1.m11, matrix2.m11);
        result.m12 = MathHelper.lerp(delta, matrix1.m12, matrix2.m12);
        result.m13 = MathHelper.lerp(delta, matrix1.m13, matrix2.m13);
        result.m20 = MathHelper.lerp(delta, matrix1.m20, matrix2.m20);
        result.m21 = MathHelper.lerp(delta, matrix1.m21, matrix2.m21);
        result.m22 = MathHelper.lerp(delta, matrix1.m22, matrix2.m22);
        result.m23 = MathHelper.lerp(delta, matrix1.m23, matrix2.m23);
        result.m30 = MathHelper.lerp(delta, matrix1.m30, matrix2.m30);
        result.m31 = MathHelper.lerp(delta, matrix1.m31, matrix2.m31);
        result.m32 = MathHelper.lerp(delta, matrix1.m32, matrix2.m32);
        result.m33 = MathHelper.lerp(delta, matrix1.m33, matrix2.m33);
        return result;
    }
}

