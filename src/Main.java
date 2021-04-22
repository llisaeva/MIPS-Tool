import java.util.Scanner;

public class Main {
    private static String prompt = "instruction: ";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (line.equals("q"))break;
            Macro macro = new Macro(line);
            for (Instruction inst : macro.instructions) {
                System.out.println(String.format("symbolic: %s\nactual: %s\nbinary: %s\n", inst.symbolic(), inst.actual(), inst.format()));
            }
        }
    }
}


/*
 Example input lines:

 0000 0101 1000 0000 0000 0000 0000 0011
 10001111 10101110 1111 1111 1111 0000
 0000 0100   0000 0001 1111 1111 1111 1100
 0010 0001 001010000000 0000 0000 0010

 li $t7, 65
 sra $v0, $t9,2
 syscall
 sw $v0, -24($t7)
 li $t5, 0
 mul $t7, $t4, $t7
 add $t5, $t4, $t5
 ble $t9, $0 loop_exit

 */