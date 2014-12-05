import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import com.sun.xml.internal.bind.v2.model.core.ID;

import enums.InstructionKeys;
import enums.Registers;
import enums.Stage;

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

	if (mode == 1)
	    runMode1();
	else
	    runMode2();

    }

    private void runMode1() {
	boolean exit = false;

	while (!exit) {

	}

	// old code
	while (!exit) {
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

    // Gets the first register of the instruction.
    private void printreg(Instruction PR) {
	System.out.println(registers.get(((String[]) PR.instructionStatus
		.get(InstructionKeys.REGISTERS))[0]));
    }

    private void printex() {
	System.out.println("Clock cycle = " + counter);

	String ifString = "IF: NOP", idString = "ID: NOP", exString = "EX: NOP", memString = "MEM: NOP", wbString = "WB: NOP";

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
