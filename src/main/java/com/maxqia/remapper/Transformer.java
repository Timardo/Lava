package com.maxqia.remapper;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

public class Transformer {

    public static JarMapping jarMapping;
    public static JarRemapper remapper;

    public static void init(JarMapping mapping, JarRemapper remapper) {
        if (Transformer.jarMapping == null) {
            Transformer.jarMapping = mapping;
        }
        if (Transformer.remapper == null) {
            Transformer.remapper = remapper;
        }
    }

    /**
     * Convert code from using Class.X methods to our remapped versions
     */
    public static byte[] transform(byte[] code) {
        ClassReader reader = new ClassReader(code); // Turn from bytes into visitor
        ClassNode node = new ClassNode();
        reader.accept(node, 0); // Visit using ClassNode

        for (MethodNode method : node.methods) { // Taken from SpecialSource
            ListIterator<AbstractInsnNode> insnIterator = method.instructions.iterator();
            while (insnIterator.hasNext()) {
                AbstractInsnNode insn = insnIterator.next();
                switch (insn.getOpcode()) {
                    case Opcodes.INVOKEVIRTUAL:
                        remapVirtual(insn);
                        break;
                    case Opcodes.INVOKESTATIC:
                        remapForName(insn);
                        break;
                }
            }
        }

        ClassWriter writer = new ClassWriter(0);
        node.accept(writer);
        return writer.toByteArray();
    }

    public static void remapForName(AbstractInsnNode insn) {
        MethodInsnNode method = (MethodInsnNode) insn;
        if (!"java/lang/Class".equals(method.owner) || !"forName".equals(method.name)) {
            return;
        }
        method.owner = "com/maxqia/remapper/RemappedMethods";
        ;
    }

    public static void remapVirtual(AbstractInsnNode insn) {
        MethodInsnNode method = (MethodInsnNode) insn;

        if (!("java/lang/Package".equals(method.owner) && "getName".equals(method.name)) &&
                !("java/lang/Class".equals(method.owner) && ("getField".equals(method.name) || "getDeclaredField".equals(method.name)
                        || "getMethod".equals(method.name) || "getDeclaredMethod".equals(method.name)
                        || "getName".equals(method.name) || "getSimpleName".equals(method.name))) &&
                !("java/lang/reflect/Field".equals(method.owner) && "getName".equals(method.name)) &&
                !("java/lang/reflect/Method".equals(method.owner) && "getName".equals(method.name))) {
            return;
        }

        Type returnType = Type.getReturnType(method.desc);

        ArrayList<Type> args = new ArrayList<>();
        args.add(Type.getObjectType(method.owner));
        args.addAll(Arrays.asList(Type.getArgumentTypes(method.desc)));

        method.setOpcode(Opcodes.INVOKESTATIC);
        method.owner = "com/maxqia/remapper/RemappedMethods";
        method.desc = Type.getMethodDescriptor(returnType, args.toArray(new Type[args.size()]));
    }

}
