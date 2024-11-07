package ralf2oo2.carmod.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.modificationstation.stationapi.api.util.math.MathHelper;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Matrix4f;
import org.ode4j.math.DVector3C;

import java.nio.FloatBuffer;

public class Util {
    private static final FloatBuffer matrixBuffer = BufferUtils.createFloatBuffer(16);

    public static void applyMatrix(Matrix4f matrix){
        matrixBuffer.clear();
        matrix.store(matrixBuffer);
        matrixBuffer.flip();
        GL11.glMultMatrix(matrixBuffer);
    }

    public static Vec3d getRelativePos(PlayerEntity player, DVector3C pos, float delta){
        return Vec3d.create(
                (pos.get0() - MathHelper.lerp(delta, player.prevX, player.x)),
                (pos.get1() - MathHelper.lerp(delta, player.prevY, player.y)),
                (pos.get2()- MathHelper.lerp(delta, player.prevZ, player.z))
        );
    }
}
