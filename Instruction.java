import java.util.HashMap;

import enums.InstructionKeys;

class Instruction {

  // The command and then whatever else is after the command.
  String command, data;

  HashMap<String, Object> instructionStatus = new HashMap<>();

  public Instruction(String instruction) {

    /*
     * Splits instruction into three parts. params[0] will contain the current pointer, params[1]
     * contains the instruction type, params[2] contains the registers where the instruction is
     * being done.
     * 
     * Ex. 12 ADD $S2,$S2,$S3 params[0] = 12, params[1] = ADD, params[2] = $S2,$S2,$S3
     */
    String[] params = instruction.split(" ");

    instructionStatus.put(InstructionKeys.PC.getKey(), params[0]);
    instructionStatus.put(InstructionKeys.COMMAND_TYPE.getKey(), params[1]);

    /*
     * Splits the registers
     * 
     * Ex. $S2,$S2,$S3 registers[0] = $S2, registers[1] = $S2, registers[2] = $S3
     */
    String[] registers = params[2].split(",");

    instructionStatus.put(InstructionKeys.REGISTERS.getKey(), registers);


  }

  @Override
  public String toString() {
    if (command.equals("EXIT"))
      return "NOP";

    // Create stringbuilder
    StringBuilder builder = new StringBuilder();

    // Use stringerbuilder to extract values from instructionStatus map
    builder.append(instructionStatus.get(InstructionKeys.PC.getKey())).append(
        instructionStatus.get(InstructionKeys.COMMAND_TYPE.getKey()));

    String[] registers = (String[]) instructionStatus.get(InstructionKeys.REGISTERS.getKey());

    // Combine the registers again
    for (String value : registers) {
      // For each register add a , after so something like 1,2,3,
      builder.append(value).append(",");
    }

    // Remove the last , because it is unneeeded
    builder.deleteCharAt(builder.lastIndexOf(","));

    // Finally return
    return builder.toString();
  }
}
