public class Command {
    private String name;
    private String operands;
    private String binary;

    public Command(String line) {
        String[] parsed = line.trim().split("\\s+");
        this.name = parsed[0];
        this.operands = parsed[1];
        this.binary = parsed[2];
    }

    public String name() {return name;}
    public String operands() {return operands;}
    public String binary() {return binary;}
}
