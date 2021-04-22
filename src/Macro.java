import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Macro {

    public List<Instruction> instructions = new ArrayList<>();

    public Macro(String line) {
        FileLoader loader = FileLoader.getInstance();
        List<String> tokens;
        Command command;
        if (line.matches("[10 ]+")) {
            instructions.add(Instruction.make(line));
        } else {
            tokens = Arrays.stream(line.replaceAll("[,()]", " ").trim().split("\\s+")).toList();
            command = loader.getCommand(tokens.get(0));
            if (command != null) {
                instructions.add(Instruction.make(line));
            } else {
                Pseudoinstruction p = loader.getPseudoinstruction(tokens.get(0));
                if (p == null) throw new IllegalArgumentException("command keyword is not found in any file.");
                List<String> strings = p.apply(tokens.subList(1,tokens.size()));

                for (String string : strings) {
                    instructions.add(Instruction.make(string));
                }

            }
        }

    }

}
