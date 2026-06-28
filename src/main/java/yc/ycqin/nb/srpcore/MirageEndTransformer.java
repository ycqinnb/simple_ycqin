package yc.ycqin.nb.srpcore;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public class MirageEndTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals("net.minecraft.block.BlockEndPortal")) return basicClass;

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                if (name.equals("func_180634_a") && desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/Entity;)V")) {
                    return new MethodVisitor(ASM5, mv) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
                            // 原版 changeDimension(1) 的 SRG 描述符
                            if (owner.equals("net/minecraft/entity/Entity") && name.equals("func_184204_a") && desc.equals("(I)Lnet/minecraft/entity/Entity;")) {
                                // shouldRedirect(world, entity)
                                mv.visitVarInsn(ALOAD, 1); // world
                                mv.visitVarInsn(ALOAD, 4); // entity
                                mv.visitMethodInsn(INVOKESTATIC, "yc/ycqin/nb/srpcore/EndPortalHelper", "shouldRedirect", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)Z", false);
                                Label redirect = new Label();
                                mv.visitJumpInsn(IFNE, redirect);
                                // 不重定向，调用原版 changeDimension(1)
                                super.visitMethodInsn(opcode, owner, name, desc, itf);
                                Label back = new Label();
                                mv.visitJumpInsn(GOTO, back);
                                // 重定向处理
                                mv.visitLabel(redirect);
                                mv.visitVarInsn(ALOAD, 1);
                                mv.visitVarInsn(ALOAD, 4);
                                mv.visitMethodInsn(INVOKESTATIC, "yc/ycqin/nb/srpcore/EndPortalHelper", "handleEndPortal", "(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;)V", false);
                                mv.visitInsn(RETURN);
                                mv.visitLabel(back);
                                return; // 已处理，跳过 super.visitMethodInsn
                            }
                            super.visitMethodInsn(opcode, owner, name, desc, itf);
                        }
                    };
                }
                return mv;
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}