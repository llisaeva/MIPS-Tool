# MIPS-Tool
### Example input:
From binary to instruction:
```bash
    >> 0010 0001 001010000000 0000 0000 0010
    symbolic: addi $t0, $t1, 2
    actual: addi $8, $9, 0x2
    binary: 001000 01001 01000 0000 0000 0000 0010
```
From instruction to binary:
```bash
    >> sw $v0, -24($t7)
    symbolic: sw $v0, -24($t7)
    actual: sw $2, 0xffe8($15)
    binary: 101011 01111 00010 1111 1111 1110 1000
```
Pseudo-instruction:
```bash
    >> mul $t0, $t1, $t2
    symbolic: mult $t1, $t2
    actual: mult $9, $10
    binary: 000000 01001 01010 00000 00000 011000

    symbolic: mflo $t0
    actual: mflo $8
    binary: 000000 00000 00000 01000 00000 010010
```
