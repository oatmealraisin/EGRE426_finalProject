package enums;

public enum InstructionKeys {

  COMMAND_TYPE("command_type"), PC("pc"), REGISTERS("registers");

  private String key;

  InstructionKeys(String _key) {
    key = _key;
  }

  public String getKey() {
    return key;
  }

}
