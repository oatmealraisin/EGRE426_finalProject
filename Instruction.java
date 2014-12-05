import java.util.Scanner;

class Instruction {

  // The command and then whatever else is after the command.
  String command, data;

  // Each of the registers. There are going to be at max three, but not
  // all of them have to be initialized.
  int reg1, reg2, reg3;

  // The immediate
  int immediate;

  public Instruction(String instruction) {
    Scanner scan = new Scanner(instruction);

    command = scan.next();

    data = scan.nextLine().trim();
    scan.close();

    String[] params = data.split(",");

    if (params.length > 0) {
      reg1 = parseReg(params[0]);

      if (command.equals("J")) {
        scan = new Scanner(params[0]);
        immediate = scan.nextInt();
        scan.close();
      }
    }

    if (params.length > 1) {
      reg2 = parseReg(params[1]);

      // Grabbing the immediate from LW, SW, MV, BEQZ, BNEZ
      if (command.equals("LW") || command.equals("SW") || command.equals("MV")
          || command.equals("BEQZ") || command.equals("BNEZ")) {
        scan = new Scanner(params[1]);
        immediate = scan.nextInt();
        scan.close();
      }

    }

    if (params.length > 2) {
      reg2 = parseReg(params[2]);

      // Grabbing the immediate from ADDI
      if (command.equals("ADDI")) {
        scan = new Scanner(params[2]);
        immediate = scan.nextInt();
        scan.close();
      }
    }
  }

  public boolean isBranch() {
    return (command.equals("J") || command.equals("BEQZ") || command.equals("BNEZ"));
  }

  public boolean writes() {
    return command.equals("SLT") || command.equals("ADD") || command.equals("ADDI")
        || command.equals("LW") || command.equals("MV");
  }

  private int parseReg(String firstParam) {
    if (firstParam.contains("$t0"))
      return 1;
    if (firstParam.contains("$t1"))
      return 2;
    if (firstParam.contains("$t2"))
      return 3;
    if (firstParam.contains("$t3"))
      return 4;
    if (firstParam.contains("$t4"))
      return 5;
    if (firstParam.contains("$s0"))
      return 6;
    if (firstParam.contains("$s1"))
      return 7;
    if (firstParam.contains("$s2"))
      return 8;
    if (firstParam.contains("$s3"))
      return 9;
    return 0;
  }

  @Override
  public String toString() {
    if (command.equals("EXIT"))
      return "NOP";

    return command + " " + data;
  }
}
