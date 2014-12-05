/**
 * If you need the command, it is under Instruction.command The registers are in string form, under
 * Instruction.reg1, reg2, reg3. The immediate, which can also be the byte offset for lw and sw, is
 * Instruction.immediate.
 */

import java.util.Scanner;

import enums.Stage;

/*
 * Class to create an instruction. The instruction is stored in a map with 3 key:value pairs
 * returning the pc, command type and the registers being used for the commands
 */
class Instruction {

    Stage currentStage;

    // The command and then whatever else is after the command.
    String command, data;

    // Each of the registers. There are going to be at max three, but not
    // all of them have to be initialized.
    String reg1, reg2, reg3;

    // The immediate
    int immediate, pc;

    public Instruction(String instruction) {
	Scanner scan = new Scanner(instruction);

	command = scan.next();

	// Try to capture the pc that comes with each instruction
	try {
	    pc = Integer.parseInt(command);
	    command = scan.next();
	} catch (NumberFormatException e) {
	    pc = -4;
	}

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

    private String parseReg(String firstParam) {
	if (firstParam.contains("$t0"))
	    return "$t0";
	if (firstParam.contains("$t1"))
	    return "$t1";
	if (firstParam.contains("$t2"))
	    return "$t2";
	if (firstParam.contains("$t3"))
	    return "$t3";
	if (firstParam.contains("$t4"))
	    return "$t4";
	if (firstParam.contains("$s0"))
	    return "$s0";
	if (firstParam.contains("$s1"))
	    return "$s1";
	if (firstParam.contains("$s2"))
	    return "$s2";
	if (firstParam.contains("$s3"))
	    return "$s3";
	return "$zero";
    }

    @Override
    public String toString() {
	if (command.equals("EXIT"))
	    return "NOP";

	return command + " " + data;
    }
}
