package yc.ycqin.nb.srpcore;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public class ProtectClassTransformer implements IClassTransformer {
public ProtectClassTransformer(){}
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals("net.minecraft.entity.EntityLivingBase")) return basicClass;

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String mname, String desc, String signature, String[] exceptions) {
                // 直接替换 getHealth 或 func_110143_aJ 方法体
                if ((mname.equals("getHealth") || mname.equals("func_110143_aJ")) && desc.equals("()F")) {
                    return new MethodVisitor(ASM5, super.visitMethod(access, mname, desc, signature, exceptions)) {
                        @Override
                        public void visitCode() {
                            // 方法体: return ProtectHelper.getHealth(this);
                            mv.visitVarInsn(ALOAD, 0);
                            mv.visitMethodInsn(INVOKESTATIC, "yc/ycqin/nb/srpcore/ProtectHelper", "getHealth", "(Lnet/minecraft/entity/EntityLivingBase;)F", false);
                            mv.visitInsn(FRETURN);
                            mv.visitMaxs(1, 1);
                            mv.visitEnd();
                        }
                    };
                }
                return super.visitMethod(access, mname, desc, signature, exceptions);
            }
        };
        cr.accept(cv, 0);
        return cw.toByteArray();
    }
}