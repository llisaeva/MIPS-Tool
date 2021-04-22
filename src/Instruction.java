import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Instruction {

    private static final String D_REG = "$d";
    private static final String S_REG = "$s";
    private static final String T_REG = "$t";
    private static final String D_CODE = "ddddd";
    private static final String S_CODE = "sssss";
    private static final String T_CODE = "ttttt";
    private static final String I_CODE = "i";

    private String name;
    private String binary;

    public Instruction(String name, String binary) {this.name = name; this.binary = binary; }

    public String name() { return name; }
    public String binary() { return binary; }
    public String format() {
        String binary = binary();
        return String.format("%s %s %s %s %s %s",
                binary.substring(0, 6),
                binary.substring(6, 11),
                binary.substring(11, 16),
                binary.substring(16, 21),
                binary.substring(21, 26),
                binary.substring(26));
    }

    abstract String symbolic();
    abstract String actual();
    abstract void setOperand(int position, String operand);

    // ---------

    public static Instruction make(String line) {
        FileLoader loader = FileLoader.getInstance();
        List<String> tokens;
        Command command = null;
        Instruction instruction;

        if (line.matches("[10 ]+")) {
            String binary = line.trim().replaceAll("\\s+", "");
            command = loader.getCommand(line);
            tokens = new ArrayList<>();
            if (command == null) throw new IllegalArgumentException("(non-macro) binary does not match any command.");

            tokens.add(command.name());
            String operands = command.operands();

            for (int i = 0; i < operands.length(); i++) {
                char opSymbol = operands.charAt(i);

                switch (opSymbol) {
                    case 'o':
                    case 'i':
                        int start = command.binary().indexOf('i');
                        int end = command.binary().lastIndexOf('i');
                        String immediateField = binary.substring(start, end + 1);
                        int value;
                        if (immediateField.charAt(0) == '1') {
                            char[] arr = immediateField.toCharArray();
                            for (int j = arr.length-1; j >= 0; j--) {
                                arr[j] = arr[j] == '1' ? '0' : '1';
                            }
                            value = Integer.parseInt(new StringBuilder().append(arr).toString(), 2);
                            value = (value + 1)*(-1);
                        } else {
                            value = Integer.parseInt(immediateField, 2);
                        }
                        tokens.add(String.valueOf(value));
                        break;

                    case 'l':
                        tokens.add("label");
                        break;

                    case 'd':
                    case 't':
                    case 's':
                        int index = command.binary().indexOf(loader.mapping.get(opSymbol));
                        String regBinary = binary.substring(index, index + 5);
                        Register reg = loader.getRegister(regBinary);
                        tokens.add(reg.symbol());
                        break;

                    default: break;
                }
            }
        } else {
            tokens = Arrays.stream(line.replaceAll("[,()]", " ").trim().split("\\s+")).toList();
            command = loader.getCommand(tokens.get(0));
        }

        switch(command.operands()) {
            case "dst": instruction = new DST(command.name(), command.binary()); break;
            case "tsi": instruction = new TSI(command.name(), command.binary()); break;
            case "stl": instruction = new STL(command.name(), command.binary()); break;
            case "sl":  instruction = new SL(command.name(), command.binary());  break;
            case "ds":  instruction = new DS(command.name(), command.binary());  break;
            case "st":  instruction = new ST(command.name(), command.binary());  break;
            case "l":   instruction = new L(command.name(), command.binary());   break;
            case "s":   instruction = new S(command.name(), command.binary());   break;
            case "tos": instruction = new TOS(command.name(), command.binary()); break;
            case "ti":  instruction = new TI(command.name(), command.binary());  break;
            case "td":  instruction = new TD(command.name(), command.binary());  break;
            case "d":   instruction = new D(command.name(), command.binary());   break;
            case "dt":  instruction = new DT(command.name(), command.binary());  break;
            case "x":   instruction = new X(command.name(), command.binary());   break;
            case "dti": instruction = new DTI(command.name(), command.binary()); break;
            case "dts": instruction = new DTS(command.name(), command.binary()); break;
            default: throw new IllegalArgumentException("(non-macro) command keyword does not match any instruction class.");
        }

        for (int i = 1; i < tokens.size(); i++)instruction.setOperand(i, tokens.get(i));

        return instruction;
    }

    private String getImmediateField(String binary, int immediate) {
        String immediateField = binary.replaceAll("[^i]+", "");
        String number = Integer.toBinaryString(immediate);
        int length = number.length();
        int limit = immediateField.length();
        if (length >= limit) number = number.substring(length - limit, length);
        else number = "0".repeat(limit - length) + number;
        binary = binary.replaceAll(I_CODE + "{" + limit + "}", number);
        return binary;
    }

    private int getImmediate(String input) {
        int immediate;
        if (input.toLowerCase().startsWith("0x")) {
            immediate = Integer.parseInt(input, 16);
        } else {
            immediate = Integer.parseInt(input);
        }
        return immediate;
    }

    // -------------------

    private static class DST extends Instruction {
        private Register D;
        private Register S;
        private Register T;

        public DST(String name, String binary) { super(name, binary); }

        @Override public String symbolic() {
            String d = D == null ? D_REG : D.symbol();
            String s = S == null ? S_REG : S.symbol();
            String t = T == null ? T_REG : T.symbol();
            return String.format("%s %s, %s, %s", name(), d, s, t);
        }

        @Override public String actual() {
            String d = D == null ? D_REG : D.number();
            String s = S == null ? S_REG : S.number();
            String t = T == null ? T_REG : T.number();
            return String.format("%s %s, %s, %s", name(), d, s, t);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: D = loader.getRegister(operand); break;
                case 2: S = loader.getRegister(operand); break;
                case 3: T = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("3 operands total ($d, $s, $t), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class TSI extends Instruction {
        private Register T;
        private Register S;
        private int immediate;

        public TSI(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String t = T == null ? T_REG : T.symbol();
            String s = S == null ? S_REG : S.symbol();
            return String.format("%s %s, %s, %d", name(), t, s, immediate);
        }

        @Override String actual() {
            String t = T == null ? T_REG : T.number();
            String s = S == null ? S_REG : S.number();
            return String.format("%s %s, %s, 0x%s", name(), t, s, Integer.toHexString(immediate));
        }

        @Override public String binary() {
            String binary = super.binary();
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            binary = super.getImmediateField(binary, immediate);
            return binary;
        }

        @Override public String format() {
            String binary = binary();
            return String.format("%s %s %s %s %s %s %s",
                    binary.substring(0, 6),
                    binary.substring(6, 11),
                    binary.substring(11, 16),
                    binary.substring(16, 20),
                    binary.substring(20, 24),
                    binary.substring(24, 28),
                    binary.substring(28));
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: T = loader.getRegister(operand); break;
                case 2: S = loader.getRegister(operand); break;
                case 3: immediate = super.getImmediate(operand); break;
                default: throw new IllegalArgumentException("3 operands total ($t, $s, immediate), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class TI extends Instruction {
        Register T;
        int immediate;

        public TI(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String t = T == null ? T_REG : T.symbol();
            return String.format("%s %s, %d", name(), t, immediate);
        }

        @Override String actual() {
            String t = T == null ? T_REG : T.number();
            return String.format("%s %s, 0x%s", name(), t, Integer.toHexString(immediate));
        }

        @Override public String binary() {
            String binary = super.binary();
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            binary = super.getImmediateField(binary, immediate);
            return binary;
        }

        @Override public String format() {
            String binary = binary();
            return String.format("%s %s %s %s %s %s %s",
                    binary.substring(0, 6),
                    binary.substring(6, 11),
                    binary.substring(11, 16),
                    binary.substring(16, 20),
                    binary.substring(20, 24),
                    binary.substring(24, 28),
                    binary.substring(28));
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: T = loader.getRegister(operand); break;
                case 2: immediate = super.getImmediate(operand); break;
                default: throw new IllegalArgumentException("2 operands total ($t, immediate), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class STL extends Instruction {
        Register S;
        Register T;
        String label;

        public STL(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String s = S == null ? S_REG : S.symbol();
            String t = T == null ? T_REG : T.symbol();
            return String.format("%s %s, %s, %s", name(), s, t, label);
        }

        @Override String actual() {
            String s = S == null ? S_REG : S.number();
            String t = T == null ? T_REG : T.number();
            return String.format("%s %s, %s, %s", name(), s, t, label);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            return binary;
        }

        @Override public String format() {
            String binary = binary();
            return String.format("%s %s %s %s %s %s %s",
                    binary.substring(0, 6),
                    binary.substring(6, 11),
                    binary.substring(11, 16),
                    binary.substring(16, 20),
                    binary.substring(20, 24),
                    binary.substring(24, 28),
                    binary.substring(28));
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: S = loader.getRegister(operand); break;
                case 2: T = loader.getRegister(operand); break;
                case 3: label = operand; break;
                default: throw new IllegalArgumentException("3 operands total ($s, $t, label), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class SL extends Instruction {
        Register S;
        String label;

        public SL(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String s = S == null ? S_REG : S.symbol();
            return String.format("%s %s, %s", name(), s, label);
        }

        @Override String actual() {
            String s = S == null ? S_REG : S.number();
            return String.format("%s %s, %s", name(), s, label);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            return binary;
        }

        @Override public String format() {
            String binary = binary();
            return String.format("%s %s %s %s %s %s %s",
                    binary.substring(0, 6),
                    binary.substring(6, 11),
                    binary.substring(11, 16),
                    binary.substring(16, 20),
                    binary.substring(20, 24),
                    binary.substring(24, 28),
                    binary.substring(28));
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: S = loader.getRegister(operand); break;
                case 2: label = operand; break;
                default: throw new IllegalArgumentException("2 operands total ($s, label), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class DS extends Instruction {
        Register D;
        Register S;

        public DS(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String d = D == null ? D_REG : D.symbol();
            String s = S == null ? S_REG : S.symbol();
            return String.format("%s %s, %s", name(), d, s);
        }

        @Override String actual() {
            String d = D == null ? D_REG : D.number();
            String s = S == null ? S_REG : S.number();
            return String.format("%s %s, %s", name(), d, s);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: D = loader.getRegister(operand); break;
                case 2: S = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("2 operands total ($s, $d), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class ST extends Instruction {
        Register S;
        Register T;

        public ST(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String s = S == null ? S_REG : S.symbol();
            String t = T == null ? T_REG : T.symbol();
            return String.format("%s %s, %s", name(), s, t);
        }

        @Override String actual() {
            String s = S == null ? S_REG : S.number();
            String t = T == null ? T_REG : T.number();
            return String.format("%s %s, %s", name(), s, t);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: S = loader.getRegister(operand); break;
                case 2: T = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("2 operands total ($s, $t), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class L extends Instruction {
        String label;

        public L(String name, String binary) { super(name, binary); }

        @Override String symbolic() { return String.format("%s %s", name(), label); }

        @Override String actual() { return String.format("%s %s", name(), label); }

        @Override public String binary() { return super.binary(); }

        @Override public String format() {
            String binary = binary();
            return String.format("%s %s %s %s %s %s %s %s",
                    binary.substring(0, 6),
                    binary.substring(6, 8),
                    binary.substring(8, 12),
                    binary.substring(12, 16),
                    binary.substring(16, 20),
                    binary.substring(20, 24),
                    binary.substring(24, 28),
                    binary.substring(28));
        }

        @Override public void setOperand(int position, String operand) {
            switch (position) {
                case 1: label = operand; break;
                default: throw new IllegalArgumentException("1 operand total (label), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class S extends Instruction {
        Register S;

        public S(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String s = S == null ? S_REG : S.symbol();
            return String.format("%s %s", name(), s);
        }

        @Override String actual() {
            String s = S == null ? S_REG : S.number();
            return String.format("%s %s", name(), s);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: S = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("1 operands total ($s), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class D extends Instruction {
        Register D;

        public D(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String d = D == null ? D_REG : D.symbol();
            return String.format("%s %s", name(), d);
        }

        @Override String actual() {
            String d = D == null ? D_REG : D.number();
            return String.format("%s %s", name(), d);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: D = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("1 operands total ($d), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class TOS extends Instruction {
        Register T;
        int offset;
        Register S;

        public TOS(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String t = T == null ? T_REG : T.symbol();
            String s = S == null ? S_REG : S.symbol();
            return String.format("%s %s, %d(%s)", name(), t, offset, s);
        }

        @Override String actual() {
            String t = T == null ? T_REG : T.number();
            String s = S == null ? S_REG : S.number();
            return String.format("%s %s, 0x%s(%s)", name(), t, Integer.toHexString(offset), s);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            binary = super.getImmediateField(binary, offset);
            return binary;
        }

        @Override public String format() {
            String binary = binary();
            return String.format("%s %s %s %s %s %s %s",
                    binary.substring(0, 6),
                    binary.substring(6, 11),
                    binary.substring(11, 16),
                    binary.substring(16, 20),
                    binary.substring(20, 24),
                    binary.substring(24, 28),
                    binary.substring(28));
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: T = loader.getRegister(operand); break;
                case 2: offset = super.getImmediate(operand); break;
                case 3: S = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("3 operands total ($t, offset($s)), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class TD extends Instruction {
        Register T;
        Register D;

        public TD(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String t = T == null ? T_REG : T.symbol();
            String d = D == null ? D_REG : D.symbol();
            return String.format("%s %s, %s", name(), t, d);
        }

        @Override String actual() {
            String t = T == null ? T_REG : T.number();
            String d = D == null ? D_REG : D.number();
            return String.format("%s %s, %s", name(), t, d);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: T = loader.getRegister(operand); break;
                case 2: D = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("2 operands total ($t, $d), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class DT extends Instruction {
        Register D;
        Register T;

        public DT(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String d = D == null ? D_REG : D.symbol();
            String t = T == null ? T_REG : T.symbol();
            return String.format("%s %s, %s", name(), d, t);
        }

        @Override String actual() {
            String d = D == null ? D_REG : D.number();
            String t = T == null ? T_REG : T.number();
            return String.format("%s %s, %s", name(), d, t);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: D = loader.getRegister(operand); break;
                case 2: T = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("2 operands total ($d, $t), #" + position + " requested.");
            }
        }
    }


    private static class X extends Instruction {

        public X(String name, String binary) { super(name, binary); }

        @Override String symbolic() { return name(); }

        @Override String actual() { return name(); }

        @Override public String binary() { return super.binary(); }

        @Override public void setOperand(int position, String operand) {
            throw new IllegalArgumentException("0 operands total, #" + position + " requested.");
        }
    }

    // -------------------

    private static class DTI extends Instruction {
        Register D;
        Register T;
        int immediate;

        public DTI(String name, String binary) { super(name, binary); }

        @Override String symbolic() {
            String d = D == null ? D_REG : D.symbol();
            String t = T == null ? T_REG : T.symbol();
            return String.format("%s %s, %s, %d", name(), d, t, immediate);
        }

        @Override String actual() {
            String d = D == null ? D_REG : D.number();
            String t = T == null ? T_REG : T.number();
            return String.format("%s %s, %s, 0x%s", name(), d, t, Integer.toHexString(immediate));
        }

        @Override public String binary() {
            String binary = super.binary();
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            binary = super.getImmediateField(binary, immediate);
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: D = loader.getRegister(operand); break;
                case 2: T = loader.getRegister(operand); break;
                case 3: immediate = super.getImmediate(operand); break;
                default: throw new IllegalArgumentException("3 operands total ($d, $t, immediate), #" + position + " requested.");
            }
        }
    }

    // -------------------

    private static class DTS extends Instruction {
        Register D;
        Register T;
        Register S;

        public DTS(String name, String binary) { super(name, binary); }

        @Override public String symbolic() {
            String d = D == null ? D_REG : D.symbol();
            String t = T == null ? T_REG : T.symbol();
            String s = S == null ? S_REG : S.symbol();

            return String.format("%s %s, %s, %s", name(), d, t, s);
        }

        @Override public String actual() {
            String d = D == null ? D_REG : D.number();
            String t = T == null ? T_REG : T.number();
            String s = S == null ? S_REG : S.number();
            return String.format("%s %s, %s, %s", name(), d, t, s);
        }

        @Override public String binary() {
            String binary = super.binary();
            if (D != null)binary = binary.replace(D_CODE, D.binary());
            if (T != null)binary = binary.replace(T_CODE, T.binary());
            if (S != null)binary = binary.replace(S_CODE, S.binary());
            return binary;
        }

        @Override public void setOperand(int position, String operand) {
            FileLoader loader = FileLoader.getInstance();
            switch (position) {
                case 1: D = loader.getRegister(operand); break;
                case 2: T = loader.getRegister(operand); break;
                case 3: S = loader.getRegister(operand); break;
                default: throw new IllegalArgumentException("3 operands total ($d, $t, $s), #" + position + " requested.");
            }
        }
    }
}
