package enums;
/*
 * Enum that stores the different types of instructions.
 */

public enum InstructionTypes {

  // Arithmetic
  ADD, ADDI,

  // Data transfer
  LW, SW, MV,

  // Branches
  SLT, BEQZ, BNEZ, J;

}
