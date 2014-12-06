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

    private HashMap<String, Register> registers = new HashMap<>();
    private HashMap<Integer, Integer> memory = new HashMap<>();

    // The list of instructions
    private ArrayList<Instruction> instructions = new ArrayList<Instruction>();
    private ArrayList<DataFlag> dataFlags = new ArrayList<DataFlag>();

    // The mode is only used for deciding the algorithm, so in theory it could
    // be a boolean.
    private int mode, pc = 0, counter = 0;

    private Instruction IF, ID, EX, MEM, WB;

    boolean branchFlag = false;

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

	Scanner scan = new Scanner(firstFile);

	// Add each instruction to the array of instructions.
	while (scan.hasNext()) {
	    instructions.add(new Instruction(scan.nextLine()));
	}

	instructions.add(new Instruction("EXIT"));

	scan.close();
	scan = new Scanner(secondFile);

	// Extract all the values from memory
	while (scan.hasNext()) {
	    String[] memoryLine = scan.nextLine().split(" ");
	    memory.put(Integer.parseInt(memoryLine[0]),
		    Integer.parseInt(memoryLine[1]));
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
		case "ADD":
		    registers.get(WB.reg1).value = registers.get(WB.reg2).value
			    + registers.get(WB.reg3).value;
		    break;
		// Move an immediate to a register. MV will alway be an
		// immediate
		case "MV":
		    registers.get(WB.reg1).value = WB.immediate;
		    break;
		case "SLT":
		    registers.get(WB.reg1).value = registers.get(WB.reg2).value < registers
			    .get(WB.reg3).value ? 1 : 0;
		    break;
		case "ADDI":
		    registers.get(WB.reg1).value = registers.get(WB.reg2).value
			    + WB.immediate;
		    break;

		// Load some memory into a register. Unknown what will happen if
		// the location doesn't exist
		case "LW":
		    registers.get(WB.reg1).value = memory.get(registers
			    .get(WB.reg2).value + WB.immediate);
		    break;
		case "EXIT":
		    exit = true;
		    break;
		}

	    }

	    // Nothing can keep the WB from moving forward, so this should
	    // happen every cycle
	    WB = MEM;

	    if (MEM != null) {
		switch (MEM.command) {

		// Save a register to the proper memory location
		case "SW":
		    memory.put(registers.get(MEM.reg2).value + MEM.immediate,
			    registers.get(MEM.reg1).value);
		    break;
		case "J":
		    pc = getRealPC(getFakePC() + 4 + 4 * MEM.immediate);

		    branchFlag = false;
		    break;

		case "BEQZ":
		    if (registers.get(MEM.reg1).value == 0) {
			if (getRealPC(getFakePC() + 4 + 4 * MEM.immediate) / 4 < instructions
				.size())
			    pc = pc + 4 + 4 * MEM.immediate;

			branchFlag = false;
		    }
		    break;

		case "BNEZ":
		    if (registers.get(IF.reg1).value != 0) {
			if (getRealPC(getFakePC() + 4 + 4 * MEM.immediate) / 4 < instructions
				.size())
			    pc = pc + 4 + 4 * MEM.immediate;
			branchFlag = false;
		    }
		    break;
		}
	    }

	    MEM = EX;

	    if (EX != null) {
		switch (EX.command) {
		// Nothing significant really happens here. At least, not until
		// forwarding.
		}
	    }

	    // Decode stage
	    // "Assume that source registers are read in the second half of the
	    // decode stage
	    if (ID != null) {
		if (!(registers.get(ID.reg2).getStatus() || registers.get(
			ID.reg3).getStatus())
			|| (ID.command.equals("SW") && !registers.get(ID.reg1).used)) {

		    if (ID.writes()) {
			registers.get(ID.reg1).setStatus(true);
		    }

		    EX = ID;
		    ID = IF;

		    if (branchFlag)
			IF = null;

		} else {
		    // Because this stage also handles passing instructions onto
		    // the next, we have to manage when it is null, too
		    EX = null;
		}
	    } else {
		EX = null;
		ID = IF;
	    }

	    if (IF != null) {

		// Flagging so that unwanted instructions aren't loaded into
		// memory
		if (IF.isBranch()) {
		    branchFlag = true;
		}

	    }

	    // If EX is null, that means we have a stall. So no more
	    if ((EX != null || counter == 0 || counter == 1 || (WB != null && WB
		    .isBranch())) && !branchFlag) {
		IF = instructions.get(pc / 4);
	    }

	    // This is for the prints. It uses a while loop because there can be
	    // more than one in a row, and the pc shouldn't be changed.
	    boolean printFlag = true;

	    while (printFlag && !branchFlag) {
		if (pc / 4 < instructions.size() - 1)
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
	boolean exit = false;
	boolean dataFlag;

	while (!exit) {

	    if (WB != null) {

		if (WB.writes()) {
		    registers.get(WB.reg1).setStatus(false);
		}

		switch (WB.command) {
		case "ADDI":
		    registers.get(EX.reg1).value = registers.get(EX.reg2).value
			    + EX.immediate;
		    break;
		case "ADD":
		    registers.get(EX.reg1).value = registers.get(EX.reg2).value
			    + registers.get(EX.reg3).value;
		    break;
		case "SLT":
		    registers.get(EX.reg1).value = registers.get(EX.reg2).value < registers
			    .get(EX.reg3).value ? 1 : 0;
		    break;

		case "EXIT":
		    exit = true;
		    break;
		}

	    }

	    // Nothing can keep the WB from moving forward, so this should
	    // happen every cycle
	    WB = MEM;

	    if (MEM != null) {
		switch (MEM.command) {

		// Load some memory into a register. Unknown what will happen if
		// the location doesn't exist
		case "LW":
		    dataFlags.add(new DataFlag(MEM.reg1, memory.get(registers
			    .get(MEM.reg2).value + MEM.immediate)));
		    dataFlag = true;
		    break;

		// Move an immediate to a register. MV will alway be an
		// immediate
		case "MV":
		    dataFlags.add(new DataFlag(MEM.reg1, MEM.immediate));
		    dataFlag = true;
		    break;

		// Save a register to the proper memory location
		case "SW":
		    memory.put(registers.get(MEM.reg2).value + MEM.immediate,
			    registers.get(MEM.reg1).value);
		    break;
		case "J":
		    pc = pc + 4 + 4 * MEM.immediate;

		    branchFlag = false;
		    break;

		case "BEQZ":
		    if (registers.get(MEM.reg1).value == 0) {
			if ((pc + 4 + 4 * MEM.immediate) / 4 < instructions
				.size())
			    pc = pc + 4 + 4 * MEM.immediate;

			branchFlag = false;
		    }
		    break;

		case "BNEZ":
		    if (registers.get(MEM.reg1).value != 0) {
			if ((pc + 4 + 4 * MEM.immediate) / 4 < instructions
				.size())
			    pc = pc + 4 + 4 * MEM.immediate;
			branchFlag = false;
		    }
		    break;
		}
	    }

	    MEM = EX;

	    if (EX != null) {
		switch (EX.command) {
		case "ADDI":
		    dataFlags.add(new DataFlag(EX.reg1,
			    registers.get(EX.reg2).value + EX.immediate));
		    dataFlag = true;
		    break;
		case "ADD":
		    dataFlags.add(new DataFlag(EX.reg1,
			    registers.get(EX.reg2).value
				    + registers.get(EX.reg3).value));
		    dataFlag = true;
		    break;
		case "SLT":
		    dataFlags.add(new DataFlag(EX.reg1,
			    registers.get(EX.reg2).value < registers
				    .get(EX.reg3).value ? 1 : 0));
		    dataFlag = true;
		    break;

		}
	    }

	    // Decode stage
	    // "Assume that source registers are read in the second half of the
	    // decode stage
	    if (ID != null) {
		if (checkRegisters()) {

		    if (ID.writes()) {
			registers.get(ID.reg1).setStatus(true);
		    }

		    EX = ID;
		    ID = IF;

		    if (branchFlag)
			IF = null;

		} else {
		    EX = null;
		}
	    }

	    if (IF != null) {
		// Making the instruction counter higher than
		// the limit, so no instructions are loaded.

		if (IF.isBranch()) {
		    branchFlag = true;
		}

	    }

	    if (counter == 1) {
		ID = IF;
	    }

	    // If EX is null, that means we have a stall. So no more
	    if ((EX != null || counter == 0 || counter == 1 || (WB != null && WB
		    .isBranch())) && !branchFlag) {
		IF = instructions.get(pc / 4);
	    }

	    // This is for the prints. It uses a while loop because there can be
	    // more than one in a row, and the pc shouldn't be changed.
	    boolean printFlag = true;

	    while (printFlag && !branchFlag) {
		if (pc / 4 < instructions.size() - 1)
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

    private void printpc() {
	System.out.println("PC = " + getFakePC());
    }

    // Gets the first register of the instruction.
    private void printreg(Instruction PR) {
	System.out.println(PR.reg1 + " = " + registers.get(PR.reg1).value);
    }

    // This is used for calculating the "PC" without the print functions.
    private int getFakePC() {

	int tempPC = 0;

	for (int i = 0; i < pc; i += 4) {
	    Instruction temp = instructions.get(i / 4);

	    if (!(temp.command.equals("PRINTPC")
		    || temp.command.equals("PRINTREG")
		    || temp.command.equals("PRINTEX") || temp.command
			.equals("EXIT"))) {
		tempPC += 4;
	    }

	}

	return tempPC;
    }

    private int getRealPC(int fakeTarget) {
	int result;

	for (result = 0; fakeTarget > 0;) {
	    Instruction temp = instructions.get(result / 4);

	    if (!(temp.command.equals("PRINTPC")
		    || temp.command.equals("PRINTREG")
		    || temp.command.equals("PRINTEX") || temp.command
			.equals("EXIT"))) {
		fakeTarget -= 4;
	    }
	    result += 4;
	}

	return result - 4;
    }

    private boolean checkRegisters() {
	boolean flagOneOk, flagTwoOk;

	flagOneOk = !registers.get(IF.reg2).used
		|| (dataFlags.size() >= 1 && dataFlags.get(0).register
			.equals(IF.reg2))
		|| (dataFlags.size() >= 2 && dataFlags.get(1).register
			.equals(IF.reg2));
	flagTwoOk = !registers.get(IF.reg3).used
		|| (dataFlags.size() >= 1 && dataFlags.get(0).register
			.equals(IF.reg3))
		|| (dataFlags.size() >= 2 && dataFlags.get(1).register
			.equals(IF.reg3));
	return flagOneOk && flagTwoOk;
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

    private class DataFlag {
	String register;
	int data;

	public DataFlag(String _register, int _data) {
	    register = _register;
	    data = _data;
	}
    }
}
