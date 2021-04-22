import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class FileLoader {
    private static final String REG_FILE = "txt/registers.txt";
    private static final String INSTRUCTION_FILE = "txt/instructions.txt";
    private static final String MACRO_FILE = "txt/macros.txt";
    private static final String MAP_FILE = "txt/mapping.txt";

    private static FileLoader INSTANCE;

    private Map<String, Register> registerBySymbol;
    private Map<String, Register> registerByNumber;
    private Map<String, Register> registerByBinary;
    private Map<String, Command> commands;

    public Map<String, Pseudoinstruction> macros;
    public Map<Character, String> mapping;

    public static FileLoader getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FileLoader();
        } return INSTANCE;
    }

    public Register getRegister(String key) {
        Register register = registerBySymbol.get(key);
        if (register != null) return register;
        register = registerByNumber.get(key);
        if (register != null) return register;
        register = registerByBinary.get(key);
        return register;
    }

    public Command getCommand(String key) {
        Command command = null;
        if (key.matches("[10 ]+")) {
            String binary = key.trim().replaceAll("\\s+", "");
            Iterator<Map.Entry<String, Command>> iterator = commands.entrySet().iterator();

            while (iterator.hasNext()) {
                Command temp = iterator.next().getValue();
                String commandBinaryRegex = temp.binary().replaceAll("[istd]", ".");
                if (binary.matches(commandBinaryRegex)) {
                    command = temp;
                    break;
                }
            }
        } else {
            command = commands.get(key);
        }
        return command;
    }

    public Pseudoinstruction getPseudoinstruction(String key) {
        return macros.get(key);
    }

    private FileLoader() {
        registerBySymbol = new HashMap<>();
        registerByNumber = new HashMap<>();
        registerByBinary = new HashMap<>();
        commands = new HashMap<>();
        macros = new HashMap<>();
        mapping = new HashMap<>();
        loadFiles();
    }

    private void loadFiles() {
        File registerFile = new File(REG_FILE);
        File instructionFile = new File(INSTRUCTION_FILE);
        File mapFile = new File(MAP_FILE);
        File macroFile = new File(MACRO_FILE);

        try {
            Scanner reader = new Scanner(registerFile);

            while (reader.hasNext()) {
                Register register = new Register(reader.nextLine());
                registerBySymbol.put(register.symbol(), register);
                registerByNumber.put(register.number(), register);
                registerByBinary.put(register.binary(), register);
            }
            reader.close();

            reader = new Scanner(instructionFile);
            while (reader.hasNext()) {
                Command command = new Command(reader.nextLine());
                commands.put(command.name(), command);
            }
            reader.close();

            reader = new Scanner(mapFile);
            while (reader.hasNext()) {
                String[] line = reader.nextLine().trim().split("\\s+");
                mapping.put(line[0].charAt(0), line[1]);
            }
            reader.close();

            reader = new Scanner(macroFile);
            while (reader.hasNext()) {
                Pseudoinstruction macro = new Pseudoinstruction(reader.nextLine());
                macros.put(macro.name(), macro);
            }
            reader.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
