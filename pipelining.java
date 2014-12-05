import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Ryan Murphy, Nachiket Chauhan
 *
 */
public class pipelining {

  public static pipelining INSTANCE = new pipelining();

  // The list of instructions
  private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

  // The list of registers. Check the handout for which index is which.
  private ArrayList<Integer> registers = new ArrayList<Integer>(10);

  // Flags to show if a register is being used or not.
  private boolean[] free = new boolean[10];

  // Stores which instructions are in which stage.
  private Instruction IF, ID, EX, MEM, WB;

  // The mode is only used for deciding the algorithm, so in theory it could
  // be a boolean.
  private int mode, pc = 0, counter = 0;

  // I'm doing this so that I don't have to worry about what is static and
  // what isn't, it really doesn't do anything beneficial
  public static void main(String[] args) {
    try {
      INSTANCE.run(args);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void run(String[] args) throws FileNotFoundException {
    File firstFile = new File(args[0]);
    File secondFile = new File(args[1]);
    mode = Integer.parseInt(args[2]);

    registers.set(0, 0);

    for (int i = 0; i < 10; i++)
      free[i] = true;
    Scanner scan = new Scanner(firstFile);

    // Add each instruction to the array of instructions. This might be
    // superfluous but fuck it.
    while (scan.hasNext()) {
      instructions.add(new Instruction(scan.nextLine().trim()));
    }

    scan.close();
    scan = new Scanner(secondFile);

    while (scan.hasNext()) {
      // I don't know what to do here
    }

    scan.close();

    if (mode == 1)
      runMode1();
    else
      runMode2();

  }

  private void runMode1() {
    boolean exit = true;

    while (exit) {
      if (WB != null) {
        if (WB.writes()) {
          free[WB.reg1] = true;
        }

        switch (WB.command) {

        }

        if (EX.command.equals("EXIT"))
          exit = false;

        WB = MEM;
      }

      if (MEM != null) {
        switch (MEM.command) {
          case "LW":
            registers.set(MEM.reg1, 0 /* Figure out memory stuff here */);
            break;
          case "MV":
            registers.set(MEM.reg1, MEM.immediate);
            break;
          case "SW":
            /* Figure out memory stuff here */
            break;
        }

      }

      MEM = EX;

      if (EX != null) {
        switch (EX.command) {
          case "ADDI":
            registers.set(EX.reg1, EX.reg2 + EX.immediate);
            break;
          case "ADD":
            registers.set(EX.reg1, EX.reg2 + EX.reg3);
            break;
          case "SLT":
            registers.set(EX.reg1, EX.reg2 < EX.reg3 ? 1 : 0);
            break;
        }

      }

      // Decode stage
      // "Assume that source registers are read in the second half of the
      // decode stage
      // This stage is a little bit more complicated because of memory
      // stuff
      if (ID != null) {
        if (free[ID.reg2] && free[ID.reg3]) {
          // TODO: Maybe make a method to toggle free registers, so
          // that we don't lock registers like $zero
          if (ID.writes()) {
            free[ID.reg1] = false;
          }

          if (ID.command.equals("SW")) {
            free[ID.reg2] = false;
          }

          EX = ID;
          ID = IF;
        } else {
          EX = null;
        }

      } else {
        ID = IF;
      }

      if (IF != null) {
        // Making the instruction counter higher than
        // the limit, so no instructions are loaded.
        if (IF.isBranch()) {
          pc = instructions.size() * 4;
        }

      }

      if (EX != null) {
        IF = instructions.get(pc);
      }

      pc += 4;
      counter++;
    }
  }

  private void runMode2() {
    // TODO: Implement forwarding here
  }

  private void printpc() {
    System.out.println("PC = " + pc);
  }

  private void printreg(String reg) {
    // TODO Finish printreg method
  }

  private void printex() {
    // TODO finish printex method
  }
}
