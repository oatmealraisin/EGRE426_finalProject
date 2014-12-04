import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 */

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
		free[WB.reg1] = true;
		WB = null;

		if (EX.command.equals("EXIT"))
		    exit = false;
	    }

	    if (MEM != null) {

	    }

	    if (EX != null) {
		switch (EX.command) {
		case "ADDI":
		    break;
		case "ADD":
		    break;

		}

	    }

	    // Decode stage
	    // "Assume that source registers are read in the second half of the
	    // decode stage
	    if (ID != null) {
		if (free[ID.reg2] && free[ID.reg3]) {
		    if (ID.writes()) {
			free[ID.reg1] = false;
		    }

		    ID = IF;
		} else {
		    EX = null;
		}

	    }

	    if (IF != null) {
		// Making the instruction counter higher than
		// the limit, so no instructions are loaded.
		if (IF.isBranch())
		    pc = instructions.size() * 4;

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

    // This is going to represent each command in the program. They'll each
    // handle their own flags maybe? Havn't decided yet.
    private class Instruction {

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
		if (command.equals("LW") || command.equals("SW")
			|| command.equals("MV") || command.equals("BEQZ")
			|| command.equals("BNEZ")) {
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
	    return (command.equals("J") || command.equals("BEQZ") || command
		    .equals("BNEZ"));
	}

	public boolean writes() {
	    return command.equals("SLT") || command.equals("ADD")
		    || command.equals("ADDI") || command.equals("LW")
		    || command.equals("MV");
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
	    return command + " " + data;
	}
    }
}
