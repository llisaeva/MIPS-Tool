import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Pseudoinstruction {
    private String name;
    private String operands;
    private String instructions;

    public Pseudoinstruction(String line) {
        String[] parsed = line.trim().split("\\s+",3);
        this.name = parsed[0];
        this.operands = parsed[1];
        this.instructions = parsed[2];
    }

    public String name() { return name;}
    public String operands() {return operands;}

    public List<String> apply(List<String> ops) {
        if (ops == null || ops.size() == 0)return null;
        String template = instructions;
        List<String> result = new ArrayList<>();

        if (name.equals("li")) {
            String number = ops.get(1);
            int immediate;
            if (number.matches("[0123456789-]+"))immediate = Integer.parseInt(number);
            else immediate = Integer.parseInt(number, 16);
            if (immediate >= 0 && immediate <= 32767) {
                template = "ori#aa#$0#" + immediate;
            } else {
                String binary = Integer.toBinaryString(immediate);
                int lower = Integer.parseInt(binary.substring(binary.length() - 16),2);
                int upper;
                if (immediate < 0) {
                    upper = Integer.parseInt(binary.substring(binary.length()-32, binary.length()-16),2);
                } else {
                    String temp = binary.substring(0, binary.length()-16);
                    upper = temp.isEmpty() ? 0 : Integer.parseInt(temp,2);
                }
                template = "lui#$at#" + upper + " " + "ori#aa#$at#" + lower;
            }
            template = template.replace(operands, ops.get(0));

        } else if (name.equals("la")) {
            template = template.replace(operands, ops.get(0));
            String label = ops.get(1);
            if (label.toLowerCase().startsWith("0x")) {
                String binary = Integer.toBinaryString(Integer.parseInt(ops.get(1),16));
                int upper = Integer.parseInt(binary.substring(0, 16));
                int lower = Integer.parseInt(binary.substring(16));
                template = template.replace("u16", String.valueOf(upper)).replace("l16", String.valueOf(lower));
            } else {
                template = template.replace("#u16", "").replace("#l16", "");
            }
        } else {
            for (int i = 0, j = 0; j < operands.length() && i < ops.size(); i++, j+=2) {
                template = template.replace(operands.substring(j, j+2), ops.get(i));
            }
        }

        String[] temp = template.trim().split("\\s+");
        for (String string : temp) {
            result.add(string.replaceAll("#", " "));
        }

        return result;
    }
}
