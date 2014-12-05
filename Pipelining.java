import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import enums.Registers;

/**
 * @author Ryan Murphy, Nachiket Chauhan
 *
 */
public class Pipelining {

  public static Pipelining pipline = new Pipelining();

  private HashMap<String, Object> registers = new HashMap<>();
  private HashMap<String, Object> memory = new HashMap<>();

  // The list of instructions
  private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

  // Flags to show if a register is being used or not.
  private boolean[] free = new boolean[10];

  // The mode is only used for deciding the algorithm, so in theory it could
  // be a boolean.
  private int mode, pc = 0, counter = 0;

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
    registers.put(Registers.ZERO.returnKey(), 0);

    registers.put(Registers.T0.returnKey(), 0);
    registers.put(Registers.T1.returnKey(), 0);
    registers.put(Registers.T2.returnKey(), 0);
    registers.put(Registers.T3.returnKey(), 0);
    registers.put(Registers.T4.returnKey(), 0);

    registers.put(Registers.S0.returnKey(), 0);
    registers.put(Registers.S1.returnKey(), 0);
    registers.put(Registers.S2.returnKey(), 0);
    registers.put(Registers.S3.returnKey(), 0);
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
      memory.put(memoryLine[0], memoryLine[1]);
    }

    scan.close();

    switch (mode) {
      case 1:
        runMode1();
        break;
      case 2:
        runMode2();
        break;
      default:
        System.out.println("Did not dectect valid mode");
        break;
    }

  }

  private void runMode1() {


  }

  private void runMode2() {
    // TODO: Implement forwarding here
  }

  private void printpc() {
    System.out.println("PC = " + pc);
  }

  // Gets the first register of the instruction.
  private void printreg(String _registerName) {
    System.out.println(registers.get(_registerName.replace("$", "")));
  }

  private void printex() {
    System.out.println("Clock cycle = " + counter);

    // All instructions start out in IF, so this will break. Maybe use the
    // PC to check what's in IF? Is this always correct?
    for (Instruction temp : instructions) {
      System.out.println(temp.returnStage().toString() + temp.toString());
    }

  }
}
