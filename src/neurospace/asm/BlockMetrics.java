package neurospace.asm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;

public class BlockMetrics {
    private double field = 0;
    private double insn = 0;
    private double invokeDynamic = 0;
    private double jump = 0;
    private double ldc = 0;
    private double method = 0;
    private double multiANewArray = 0;
    private double opcode = 0;
    private double tableSwitch = 0;
    private double type = 0;

    private static double charsValue(String s) {
        char[] chars = s.toCharArray();
        int val = 0;

        for (int i = 0; i < chars.length; i++) {
            val += Character.getNumericValue(chars[i]);
        }

        return (double)val;
    }

    private static double descValue(String desc) {
        String re = "(?:(\\[*L)[^;]+;|([BCDFIJSVZ]))";
        Matcher m = Pattern.compile(re).matcher(desc);

        double val = 0.0;

        while (m.find()) {
            for (int i = 0; i < m.groupCount(); i++) {
                char[] chars = m.group(i).toCharArray();

                for (int j = 0; j < chars.length; j++) {
                    val += Character.getNumericValue(chars[j]);
                }
            }
        }

        return val;
    }

    public static double[] calculate(List<AbstractInsnNode> insnNodes) {
        BlockMetrics m = new BlockMetrics(insnNodes);

        return new double[]{m.field, m.insn, m.invokeDynamic, m.jump, m.ldc,
                            m.method, m.multiANewArray, m.opcode, m.tableSwitch,
                            m.type};
    }

    private BlockMetrics(List<AbstractInsnNode> insnNodes) {
        for (int i = 0; i < insnNodes.size(); i++) {
            measure(insnNodes.get(i));
        }
    }

    private void measure(AbstractInsnNode insn) {
        this.opcode += insn.getOpcode();
        if (insn instanceof JumpInsnNode) {
            this.jump += insn.getOpcode() * 2.25;
        }
    }

    private void measure(FieldInsnNode insn) {
        this.field += descValue(insn.desc);
        measure((AbstractInsnNode)insn);
    }

    private void measure(InsnNode insn) {
        if (insn.getOpcode() != Opcodes.ATHROW) {
            measure((AbstractInsnNode)insn);
        }
    }

    private void measure(InvokeDynamicInsnNode insn) {
        this.invokeDynamic += descValue(insn.desc);
        measure((AbstractInsnNode)insn);
    }

    private void measure(LdcInsnNode insn) {
        this.ldc += charsValue(insn.cst.getClass().getName());
        measure((AbstractInsnNode)insn);
    }

    private void measure(MethodInsnNode insn) {
        this.method += descValue(insn.desc);
        measure((AbstractInsnNode)insn);
    }

    private void measure(MultiANewArrayInsnNode insn) {
        this.multiANewArray += descValue(insn.desc) * insn.dims;
        measure((AbstractInsnNode)insn);
    }

    private void measure(TableSwitchInsnNode insn) {
        this.tableSwitch += insn.max;
        measure((AbstractInsnNode)insn);
    }

    private void measure(TypeInsnNode insn) {
        this.type += descValue(insn.desc);
        measure((AbstractInsnNode)insn);
    }
}
