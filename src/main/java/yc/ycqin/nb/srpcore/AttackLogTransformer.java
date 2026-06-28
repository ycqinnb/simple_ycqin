package yc.ycqin.nb.srpcore;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

public class AttackLogTransformer implements IClassTransformer {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!transformedName.equals("net.minecraft.entity.monster.EntityMob"))
            return basicClass;

        ClassReader cr = new ClassReader(basicClass);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new ClassVisitor(ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String mname, String desc, String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, mname, desc, signature, exceptions);
                if (mname.equals("func_70652_k") && desc.equals("(Lnet/minecraft/entity/Entity;)Z")) {
                    return new MethodVisitor(ASM5, mv) {
                        @Override
                        public void visitCode() {
                            // 插入：System.out.println("[LOG] EntityMob.attackEntityAsMob called, attacker=" + this + ", target=" + p_70652_1_);
                            // 先保存参数（目标实体）到局部变量，然后调用 System.out.println
                            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
                            mv.visitLdcInsn("[LOG] EntityMob.attackEntityAsMob called, attacker=");
                            mv.visitVarInsn(ALOAD, 0); // this
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "toString", "()Ljava/lang/String;", false);
                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
                            mv.visitLdcInsn(", target=");
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
                            mv.visitVarInsn(ALOAD, 1); // 目标实体
                            mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;", false);
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
                            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
                            super.visitCode();
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