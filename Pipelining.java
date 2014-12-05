import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import enums.Registers;
import enums.Stage;

/**
 * @author Ryan Murphy, Nachiket Chauhan
 *
 */
public class Pipelining {

  public static Pipelining pipline = new Pipelining();

  private HashMap<String, Register> registers = new HashMap<>();
  private HashMap<Integer, Integer> memory = new HashMap<>();

  // The list of instructions
  private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

  // Flags to show if a register is being used or not.
  private boolean[] free = new boolean[10];

  // The mode is only used for deciding the algorithm, so in theory it could
  // be a boolean.
  private int mode, pc = 0, counter = 0;

  private Instruction IF, ID, EX, MEM, WB;

  // I'm doing this so that I don't have to worry about what is static and
  // what isn't, it really doesn't do anything beneficial
  public static void main(String[] args) {
    try {
      // We could make initizalize the constructor
      pipline.initizalize();
      pipline.run(args);
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  // Initialize registers
  public void initizalize() {
    registers.put(Registers.ZERO.returnKey(), new Register(0));

    registers.put(Registers.T0.returnKey(), new Register(0));
    registers.put(Registers.T1.returnKey(), new Register(0));
    registers.put(Registers.T2.returnKey(), new Register(0));
    registers.put(Registers.T3.returnKey(), new Register(0));
    registers.put(Registers.T4.returnKey(), new Register(0));

    registers.put(Registers.S0.returnKey(), new Register(0));
    registers.put(Registers.S1.returnKey(), new Register(0));
    registers.put(Registers.S2.returnKey(), new Register(0));
    registers.put(Registers.S3.returnKey(), new Register(0));
  }

  public void run(String[] args) throws FileNotFoundException {
    File firstFile = new File(args[0]);
    File secondFile = new File(args[1]);
    mode = Integer.parseInt(args[2]);

    registers.clear();

    for (int i = 0; i < 10; i++)
      free[i] = true;
    Scanner scan = new Scanner(firstFile);

    // Add each instruction to the array of instructions.
    while (scan.hasNext()) {
      instructions.add(new Instruction(scan.nextLine()));
    }

    scan.close();
    scan = new Scanner(secondFile);

    // Extract all the values from memory
    while (scan.hasNext()) {
      String[] memoryLine = scan.nextLine().split(" ");
      memory.put(Integer.parseInt(memoryLine[0]), Integer.parseInt(memoryLine[1]));
    }

    scan.close();

    if (mode == 1)
      runMode1();
    else
      runMode2();

  }

  private void runMode1() {
    boolean exit = false;

    while (!exit) {
      if (WB != null) {

        if (WB.writes()) {
          registers.get(WB.reg1).setStatus(false);
        }

        switch (WB.command) {

        }

        if (EX.command.equals("EXIT"))
          exit = false;

      }

      WB = MEM;

      if (MEM != null) {
        switch (MEM.command) {
          case "LW":
            registers.get(MEM.reg1).value =
                memory.get(registers.get(MEM.reg2).value + MEM.immediate);
            break;
          case "MV":
            registers.get(MEM.reg1).value = MEM.immediate;
            break;
          case "SW":
            memory.replace(registers.get(MEM.reg2).value + MEM.immediate,
                registers.get(MEM.reg1).value);
            break;
        }

      }

      MEM = EX;

      if (EX != null) {
        switch (EX.command) {
          case "ADDI":
            registers.get(EX.reg1).value = registers.get(EX.reg2).value + EX.immediate;
            break;
          case "ADD":
            registers.get(EX.reg1).value =
                registers.get(EX.reg2).value + registers.get(EX.reg3).value;
            break;
          case "SLT":
            registers.get(EX.reg1).value =
                registers.get(EX.reg2).value < registers.get(EX.reg3).value ? 1 : 0;
            break;
          case "J":
            pc = pc + 4 + 4 * IF.immediate;
            if (IF != null) {
              System.out.println("stall");

            } else {
              ID = IF;
              IF = instructions.get(pc);
            }
            break;

          case "BEQZ":
            if (registers.get(IF.reg1).value == 0) {
              pc = pc + 4 + 4 * IF.immediate;
              if (IF != null) {
                System.out.println("stall");

              } else {
                ID = IF;
                IF = instructions.get(pc);
              }
            }
            break;

          case "BNEZ":
            if (registers.get(IF.reg1).value != 0) {
              pc = pc + 4 + 4 * IF.immediate;
              if (IF != null) {
                System.out.println("stall");

              } else {
                ID = IF;
                IF = instructions.get(pc);
              }
            }
            break;
        }

      }

      // Decode stage
      // "Assume that source registers are read in the second half of the
      // decode stage
      if (ID != null) {
        if (!registers.get(ID.reg2).getStatus() && !registers.get(ID.reg3).getStatus()) {
          if (ID.writes()) {
            registers.get(ID.reg1).setStatus(true);
          }

          if (ID.command.equals("SW")) {
            registers.get(ID.reg2).setStatus(true);
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



        }

      }

      if (EX != null) {
        IF = instructions.get(pc / 4);
      }

      // This is for the prints. It uses a while loop because there can be
      // more than one in a row, and the pc shouldn't be changed.
      boolean printFlag = true;

      while (printFlag) {
        pc += 4;

        switch (instructions.get(pc / 4).command) {
          case "PRINTPC":
            printpc();
            break;
          case "PRINTREG":
            printreg(instructions.get(pc / 4));
            break;
          case "PRINTEX":
            printex();
            break;
          default:
            printFlag = false;
        }
      }

      counter++;
    }
  }

  private void runMode2() {

  }

  private void printpc() {
    System.out.println("PC = " + pc);
  }

  // Gets the first register of the instruction.
  private void printreg(Instruction PR) {
    System.out.println(registers.get(PR.reg1));
  }

  private void printex() {
    System.out.println("Clock cycle = " + counter);

    String ifString = "IF: NOP", idString = "ID: NOP", exString = "EX: NOP", memString = "MEM: NOP", wbString =
        "WB: NOP";

    // All instructions start out in IF, so this will break. Maybe use the
    // PC to check what's in IF? Is this always correct?
    for (Instruction temp : instructions) {
      if (temp.currentStage == Stage.IF)
        ifString = temp.toString();
      if (temp.currentStage == Stage.ID)
        idString = temp.toString();
      if (temp.currentStage == Stage.EX)
        exString = temp.toString();
      if (temp.currentStage == Stage.MEM)
        memString = temp.toString();
      if (temp.currentStage == Stage.WB)
        wbString = temp.toString();
    }

    System.out.println(ifString);
    System.out.println(idString);
    System.out.println(exString);
    System.out.println(memString);
    System.out.println(wbString);

  }
}
