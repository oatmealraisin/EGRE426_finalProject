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

    // Singleton
    public static Pipelining pipline = new Pipelining();

    private HashMap<String, Register> registers = new HashMap<>();
    private HashMap<Integer, Integer> memory = new HashMap<>();

    // The list of instructions
    // Note: The index of the instruction does not correlate to the actual pc
    // that it should have. Because of the print instructions, the pc is off.
    // The correct pc of the instruction is stored in the actual instruction.
    // Alternatively, there is a method that will calculate the pc.
    private ArrayList<Instruction> instructions = new ArrayList<Instruction>();

    // This is used for the data forwarding. DataFlags contain a string for the
    // register and a value for.. the value. They should be added in the ex or
    // mem stage and removed in the wb stage.
    private ArrayList<DataFlag> dataFlags = new ArrayList<DataFlag>();

    // The mode is only used for deciding the algorithm, so in theory it could
    // be a boolean. The counter keeps track of the number of cycles. The pc
    // indexes the
    // instructions, but is not the correct pc as far as the simulation goes.
    private int mode, pc = 0, counter = 0;

    // The instructions at each stage of the pipeline. These are being executed,
    // and they change the state of the machine based on what instruction they
    // are. At the end of each stage, they should become the previous
    // instruction. e.g., mem = ex at the end of the mem stage.
    private Instruction IF, ID, EX, MEM, WB;

    // This flag means that there is a branch flag before the mem stage, and it
    // keeps the IF stage from functioning, and keeps the pc from incrementing.
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

	// By doing this we make sure that the program will end.
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

    // runMode1 is the program without data forwarding.
    private void runMode1() {

	// Switch this to true to end the program.
	boolean exit = false;

	while (!exit) {

	    // The writeback stage. Blocked registers should be unblocked here.
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

	    // the memory section. Everything having to do with lw and sw should
	    // happen here. Also, branches and jumps write the pc here,
	    // according to the diagram.
	    if (MEM != null) {
		switch (MEM.command) {

		// Save a register to the proper memory location
		case "SW":
		    memory.put(registers.get(MEM.reg2).value + MEM.immediate,
			    registers.get(MEM.reg1).value);
		    break;
		case "J":

		    // This is kind of complicated. We need to get the pc that
		    // is accurate to the simulation, at 4+4j, and then convert
		    // it back to the index for our arraylist.
		    pc = getRealPC(getFakePC() + 4 + 4 * MEM.immediate);

		    branchFlag = false;
		    break;

		case "BEQZ":
		    if (registers.get(MEM.reg1).value == 0) {
			if (getRealPC(getFakePC() + 4 + 4 * MEM.immediate) / 4 < instructions
				.size())
			    pc = getRealPC(getFakePC() + 4 + 4 * MEM.immediate);

			branchFlag = false;
		    }
		    break;

		case "BNEZ":
		    if (registers.get(MEM.reg1).value != 0) {
			if (getRealPC(getFakePC() + 4 + 4 * MEM.immediate) / 4 < instructions
				.size())
			    pc = getRealPC(getFakePC() + 4 + 4 * MEM.immediate);
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

	    // Registers should be blocked in this stage, and branch flag should
	    // be set here. This is a kinda complicated one.
	    if (ID != null) {
		if (!(registers.get(ID.reg2).getStatus() || registers.get(
			ID.reg3).getStatus())
			|| (ID.command.equals("SW") && !registers.get(ID.reg1)
				.getStatus())) {

		    // Instruction.writes() is a good way to see if you need to
		    // lock any registers.
		    if (ID.writes()) {
			registers.get(ID.reg1).setStatus(true);
		    }

		    // We set EX here because we don't want instructions to
		    // progress if their registers are locked. Also, we don't
		    // want ID to progress if it's locked.
		    EX = ID;
		    ID = IF;

		    // This will prevent the jump/branch from being duplicated.
		    if (branchFlag)
			IF = null;

		} else {
		    // if the ID doesn't move on, the ex needs to be null
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

	    // If EX is null, that means we have a stall. If the counter is 0 or
	    // 1, we should load an instruction. If the branch is finished, we
	    // should load an instruction.
	    if ((EX != null || counter == 0 || counter == 1 || (WB != null && WB
		    .isBranch())) && !branchFlag) {
		IF = instructions.get(pc / 4);
	    }

	    // This is for the prints. It uses a while loop because there can be
	    // more than one in a row.
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
	// Switch this to true to end the program.
	boolean exit = false;

	while (!exit) {
	    // Used to hold the correct register value for some instructions
	    int firstFlagValue, secondFlagValue;

	    // The writeback stage. Blocked registers should be unblocked here.
	    if (WB != null) {

		if (WB.writes()) {
		    registers.get(WB.reg1).setStatus(false);
		}

		switch (WB.command) {
		case "ADD":

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(WB.reg1)
			    && dataFlags.get(0).data == registers.get(WB.reg2).value
				    + registers.get(WB.reg3).value)
			dataFlags.remove(0);

		    if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(WB.reg1)
			    && dataFlags.get(1).data == registers.get(WB.reg2).value
				    + registers.get(WB.reg3).value)
			dataFlags.remove(1);

		    registers.get(WB.reg1).value = registers.get(WB.reg2).value
			    + registers.get(WB.reg3).value;
		    break;
		// Move an immediate to a register. MV will alway be an
		// immediate
		case "MV":

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(WB.reg1)
			    && dataFlags.get(0).data == WB.immediate)
			dataFlags.remove(0);

		    if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(WB.reg1)
			    && dataFlags.get(1).data == WB.immediate)
			dataFlags.remove(1);

		    registers.get(WB.reg1).value = WB.immediate;

		    break;
		case "SLT":

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(WB.reg1)
			    && dataFlags.get(0).data == (registers.get(WB.reg2).value < registers
				    .get(WB.reg3).value ? 1 : 0))
			dataFlags.remove(0);

		    if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(WB.reg1)
			    && dataFlags.get(1).data == (registers.get(WB.reg2).value < registers
				    .get(WB.reg3).value ? 1 : 0))
			dataFlags.remove(1);

		    registers.get(WB.reg1).value = registers.get(WB.reg2).value < registers
			    .get(WB.reg3).value ? 1 : 0;
		    break;
		case "ADDI":
		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(WB.reg1)
			    && dataFlags.get(0).data == registers.get(WB.reg2).value
				    + WB.immediate)
			dataFlags.remove(0);

		    if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(WB.reg1)
			    && dataFlags.get(1).data == registers.get(WB.reg2).value
				    + WB.immediate)
			dataFlags.remove(1);

		    registers.get(WB.reg1).value = registers.get(WB.reg2).value
			    + WB.immediate;
		    break;

		// Load some memory into a register. Unknown what will happen if
		// the location doesn't exist
		case "LW":
		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(WB.reg1)
			    && dataFlags.get(0).data == memory.get(registers
				    .get(WB.reg2).value + WB.immediate))
			dataFlags.remove(0);

		    if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(WB.reg1)
			    && dataFlags.get(1).data == memory.get(registers
				    .get(WB.reg2).value + WB.immediate))
			dataFlags.remove(1);
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

	    // the memory section. Everything having to do with lw and sw should
	    // happen here. Also, branches and jumps write the pc here,
	    // according to the diagram.
	    if (MEM != null) {
		switch (MEM.command) {

		// Save a register to the proper memory location
		case "SW":
		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			firstFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			firstFlagValue = dataFlags.get(1).data;

		    else
			firstFlagValue = registers.get(EX.reg1).value;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg2))
			secondFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg2))
			secondFlagValue = dataFlags.get(1).data;

		    else
			secondFlagValue = registers.get(EX.reg2).value;

		    memory.put(secondFlagValue + MEM.immediate, firstFlagValue);
		    break;

		case "LW":
		    dataFlags.add(new DataFlag(MEM.reg1, memory.get(registers
			    .get(WB.reg2).value + WB.immediate)));
		    break;

		}
	    }

	    MEM = EX;

	    if (EX != null) {
		switch (EX.command) {

		case "ADDI":
		    // If we need one of the flags..
		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg2))
			firstFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg2))
			firstFlagValue = dataFlags.get(1).data;

		    else
			firstFlagValue = registers.get(EX.reg2).value;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			dataFlags.get(0).data = firstFlagValue + EX.immediate;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			dataFlags.get(1).data = firstFlagValue + EX.immediate;
		    else
			dataFlags.add(new DataFlag(EX.reg1, firstFlagValue
				+ EX.immediate));

		    break;
		case "ADD":
		    // We may need another flag value for ADD, because it as two
		    // registers.

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg2))
			firstFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg2))
			firstFlagValue = dataFlags.get(1).data;

		    else
			firstFlagValue = registers.get(EX.reg2).value;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg3))
			secondFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg3))
			secondFlagValue = dataFlags.get(1).data;

		    else
			secondFlagValue = registers.get(EX.reg3).value;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			dataFlags.get(0).data = firstFlagValue
				+ secondFlagValue;
		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			dataFlags.get(1).data = firstFlagValue
				+ secondFlagValue;
		    else
			dataFlags.add(new DataFlag(EX.reg1, firstFlagValue
				+ EX.immediate));
		    break;

		case "SLT":

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg2))
			firstFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg2))
			firstFlagValue = dataFlags.get(1).data;

		    else
			firstFlagValue = registers.get(EX.reg2).value;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg3))
			secondFlagValue = dataFlags.get(0).data;

		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg3))
			secondFlagValue = dataFlags.get(1).data;

		    else
			secondFlagValue = registers.get(EX.reg3).value;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			dataFlags.get(0).data = firstFlagValue < secondFlagValue ? 1
				: 0;
		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			dataFlags.get(1).data = firstFlagValue < secondFlagValue ? 1
				: 0;
		    else
			dataFlags.add(new DataFlag(EX.reg1,
				firstFlagValue < secondFlagValue ? 1 : 0));

		    break;

		// Should this go in instruction decode?
		case "MV":
		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			dataFlags.get(0).data = EX.immediate;
		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			dataFlags.get(1).data = EX.immediate;
		    else
			dataFlags.add(new DataFlag(EX.reg1, EX.immediate));
		    break;

		// I've moved these down here because they finish their
		// stuff in
		// the execution phase.
		case "J":

		    // This is kind of complicated. We need to get the pc that
		    // is accurate to the simulation, at 4+4j, and then convert
		    // it back to the index for our arraylist.
		    pc = getRealPC(getFakePC() + 4 + 4 * MEM.immediate);

		    branchFlag = false;
		    break;

		case "BEQZ":
		    firstFlagValue = 0;
		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			firstFlagValue = dataFlags.get(0).data;
		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			firstFlagValue = dataFlags.get(1).data;
		    else
			firstFlagValue = registers.get(EX.reg1).value;

		    if (firstFlagValue == 0) {
			if (getRealPC(getFakePC() + 4 + 4 * EX.immediate) / 4 < instructions
				.size())
			    pc = getRealPC(getFakePC() + 4 + 4 * EX.immediate);

			branchFlag = false;
		    }
		    break;

		case "BNEZ":

		    firstFlagValue = 0;

		    if (dataFlags.size() > 0
			    && dataFlags.get(0).register.equals(EX.reg1))
			firstFlagValue = dataFlags.get(0).data;
		    else if (dataFlags.size() > 1
			    && dataFlags.get(1).register.equals(EX.reg1))
			firstFlagValue = dataFlags.get(1).data;
		    else
			firstFlagValue = registers.get(EX.reg1).value;

		    if (firstFlagValue != 0) {
			if (getRealPC(getFakePC() + 4 + 4 * EX.immediate) / 4 < instructions
				.size())
			    pc = getRealPC(getFakePC() + 4 + 4 * EX.immediate);
			branchFlag = false;
		    }
		    break;
		}
	    }

	    // Decode stage
	    // "Assume that source registers are read in the second half of the
	    // decode stage

	    // Registers should be blocked in this stage, and branch flag should
	    // be set here. This is a kinda complicated one.
	    if (ID != null) {
		if (checkRegisters()) {

		    // Instruction.writes() is a good way to see if you need to
		    // lock any registers.
		    if (ID.writes()) {
			registers.get(ID.reg1).setStatus(true);
		    }

		    // We set EX here because we don't want instructions to
		    // progress if their registers are locked. Also, we don't
		    // want ID to progress if it's locked.
		    EX = ID;
		    ID = IF;

		    // This will prevent the jump/branch from being duplicated.
		    if (branchFlag)
			IF = null;

		} else {
		    // if the ID doesn't move on, the ex needs to be null
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

	    // If EX is null, that means we have a stall. If the counter is 0 or
	    // 1, we should load an instruction. If the branch is finished, we
	    // should load an instruction.
	    if ((EX != null || counter == 0 || counter == 1 || (MEM != null && MEM
		    .isBranch())) && !branchFlag) {
		IF = instructions.get(pc / 4);
	    }

	    // This is for the prints. It uses a while loop because there can be
	    // more than one in a row.
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
    // We need this because the pc counter in the loop doesn't represent the
    // simulated pc.

    // TODO: Actually, we could just get the pc from the last used instruction.
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

    // Does the same as getFakePC, except backwards. This is used for jump
    // commands, so that we can calculate where it wants to go.
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

    // This checks to see if a IF can move on, by checking the dataFlags and the
    // actual registers.
    private boolean checkRegisters() {
	boolean flagOneOk, flagTwoOk, flagSW;

	if (IF == null)
	    return false;

	flagOneOk = !registers.get(IF.reg2).used
		|| (dataFlags.size() > 0 && dataFlags.get(0).register
			.equals(IF.reg2))
		|| (dataFlags.size() > 1 && dataFlags.get(1).register
			.equals(IF.reg2));
	flagTwoOk = !registers.get(IF.reg3).used
		|| (dataFlags.size() > 0 && dataFlags.get(0).register
			.equals(IF.reg3))
		|| (dataFlags.size() > 1 && dataFlags.get(1).register
			.equals(IF.reg3));

	if (IF.command.equals("SW"))
	    flagSW = !registers.get(IF.reg1).used
		    || (dataFlags.size() > 0 && dataFlags.get(0).register
			    .equals(IF.reg1))
		    || (dataFlags.size() > 1 && dataFlags.get(1).register
			    .equals(IF.reg1));
	else
	    flagSW = true;

	return flagOneOk && flagTwoOk && flagSW;
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

    // A flag for the data forwarding. These contain the register the flag is
    // for, and the data needed. the IF will pick these up, and it will allow
    // instructions to move forward.
    private class DataFlag {
	String register;
	int data;

	public DataFlag(String _register, int _data) {
	    register = _register;
	    data = _data;
	}
    }
}
