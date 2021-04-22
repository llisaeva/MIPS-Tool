public class Register {
    private String symbol;
    private String number;
    private String binary;

    public Register(String line) {
        String[] parsed = line.trim().split("\\s+");
        this.symbol = parsed[0];
        this.number = parsed[1];
        this.binary = parsed[2];
    }

    public String symbol() { return symbol; }
    public String number() { return number; }
    public String binary() { return binary; }
}
