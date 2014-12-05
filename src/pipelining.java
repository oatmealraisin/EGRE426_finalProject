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
		if (WB.writes()) {
		    free[WB.reg1] = true;
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
		    registers
			    .set(MEM.reg1, 0 /* Figure out memory stuff here */);
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
		IF = instructions.get(pc / 4);
	    }

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
	// TODO: Implement forwarding here
    }

    private void printpc() {
	System.out.println("PC = " + pc);
    }

    private void printreg(Instruction PR) {
	System.out.println(registers.get(PR.reg1));
    }

    private void printex() {
	System.out.println("Clock cycle = " + counter);

	if (IF != null) {
	    System.out.println("IF: " + IF.toString());
	} else {
	    System.out.println("IF: NOP");
	}

	if (ID != null) {
	    System.out.println("ID: " + ID.toString());
	} else {
	    System.out.println("ID: NOP");
	}

	if (EX != null) {
	    System.out.println("EX: " + EX.toString());
	} else {
	    System.out.println("EX: NOP");
	}

	if (MEM != null) {
	    System.out.println("MEM: " + MEM.toString());
	} else {
	    System.out.println("MEM: NOP");
	}

	if (WB != null) {
	    System.out.println("WB: " + WB.toString());
	} else {
	    System.out.println("WB: NOP");
	}

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
	    if (command.equals("EXIT"))
		return "NOP";

	    return command + " " + data;
	}
    }
}
