/**
 * If you need the command, it is under Instruction.command
 * The registers are in string  form, under Instruction.reg1, reg2, reg3.
 * The immediate, which can also be the byte offset for lw and sw, is Instruction.immediate.
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

    // public Instruction(String instruction) {
    //
    // Scanner scan;
    //
    // currentStage = Stage.IF;
    //
    // /*
    // * Splits instruction into three parts. params[0] will contain the
    // * current pointer, params[1] contains the instruction type, params[2]
    // * contains the registers where the instruction is being done.
    // *
    // * Ex. ADD $S2,$S2,$S3 params[0] = ADD, params[1] = $S2,$S2,$S3
    // */
    // String[] params = instruction.split(" ");
    //
    // command = params[0];
    //
    // /*
    // * Splits the registers
    // *
    // * Ex. 12 ADD $S2,$S2,$S3 registers[0] = $S2, registers[1] = $S2,
    // * registers[2] = $S3
    // */
    // String[] registers = params[1].split(",");
    // }

    @Override
    public String toString() {
	// // Create stringbuilder
	// StringBuilder builder = new StringBuilder();
	//
	// // Use stringerbuilder to extract values from instructionStatus map
	// builder.append(instructionStatus.get(InstructionKeys.PC.getKey()))
	// .append(instructionStatus.get(InstructionKeys.COMMAND_TYPE
	// .getKey()));
	//
	// String[] registers = (String[]) instructionStatus
	// .get(InstructionKeys.REGISTERS.getKey());
	//
	// // Combine the registers again
	// for (String value : registers) {
	// // For each register add a , after so something like 1,2,3,
	// builder.append(value).append(",");
	// }
	//
	// // Remove the last , because it is unneeded
	// builder.deleteCharAt(builder.lastIndexOf(","));
	//
	// // Finally return
	// return builder.toString();
    }

    public String returnStage() {
	return currentStage.toString();
    }

    public void changeStage(Stage _stage) {
	currentStage = _stage;
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
}
