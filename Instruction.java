import java.util.HashMap;

import enums.InstructionKeys;
import enums.Stage;

/*
 * Class to create an instruction. The instruction is stored in a map with 3 key:value pairs
 * returning the pc, command type and the registers being used for the commands
 */
class Instruction {

  HashMap<String, Object> instructionStatus = new HashMap<>();

  Stage currentStage;

  public Instruction(String instruction) {

    currentStage = Stage.IF;

    /*
     * Splits instruction into three parts. params[0] will contain the current pointer, params[1]
     * contains the instruction type, params[2] contains the registers where the instruction is
     * being done.
     * 
     * Ex. ADD $S2,$S2,$S3 params[0] = ADD, params[1] = $S2,$S2,$S3
     */
    String[] params = instruction.split(" ");
    instructionStatus.put(InstructionKeys.COMMAND_TYPE.getKey(), params[0]);

    /*
     * Splits the registers
     * 
     * Ex. 12 ADD $S2,$S2,$S3 registers[0] = $S2, registers[1] = $S2, registers[2] = $S3
     */
    String[] registers = params[1].split(",");

    instructionStatus.put(InstructionKeys.REGISTERS.getKey(), registers);
  }

  @Override
  public String toString() {
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

  public String returnStage() {
    return currentStage.toString();
  }

  public void changeStage(Stage _stage) {
    currentStage = _stage;
  }
}
