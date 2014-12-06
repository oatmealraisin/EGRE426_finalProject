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

	reg1 = reg2 = reg3 = "$zero";

	// Try to capture the pc that comes with each instruction
	try {
	    pc = Integer.parseInt(command);
	    command = scan.next();
	} catch (NumberFormatException e) {
	    pc = -4;
	}

	// Catching an error where there's no more string left
	if (scan.hasNext()) {
	    data = scan.nextLine().trim();
	} else {
	    scan.close();
	    data = null;
	    immediate = 0;
	    return;
	}

	scan.close();

	String[] params = data.split(",");

	if (params.length > 0) {
	    reg1 = parseReg(params[0]);

	    if (command.equals("J")) {
		scan = new Scanner(params[0]);
		immediate = scan.nextInt();
		System.out.println(toString());
		scan.close();
	    }
	}

	if (params.length > 1) {
	    reg2 = parseReg(params[1]);

	    // Grabbing the immediate from LW, SW, MV, BEQZ, BNEZ
	    if (command.equals("LW") || command.equals("SW")
		    || command.equals("MV") || command.equals("BEQZ")
		    || command.equals("BNEZ")) {

		try {
		    immediate = Integer.parseInt(params[1].trim());
		} catch (NumberFormatException e) {
		    immediate = Integer.parseInt(params[1].substring(0,
			    params[1].indexOf("(")).trim());
		}
	    }

	}

	if (params.length > 2) {
	    reg3 = parseReg(params[2]);

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
	firstParam = firstParam.toUpperCase();
	if (firstParam.contains("$T0"))
	    return "$T0";
	if (firstParam.contains("$T1"))
	    return "$T1";
	if (firstParam.contains("$T2"))
	    return "$T2";
	if (firstParam.contains("$T3"))
	    return "$T3";
	if (firstParam.contains("$T4"))
	    return "$T4";
	if (firstParam.contains("$S0"))
	    return "$S0";
	if (firstParam.contains("$S1"))
	    return "$S1";
	if (firstParam.contains("$S2"))
	    return "$S2";
	if (firstParam.contains("$S3"))
	    return "$S3";

	return "$zero";
    }

    @Override
    public String toString() {
	if (command.equals("EXIT"))
	    return "NOP";

	return command + " " + data;
    }
}
