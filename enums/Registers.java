package enums;

public enum Registers {

  // Zero register
  ZERO("$zero"),

  // T registers
  T0("$T0"), T1("$T1"), T2("$T2"), T3("$T3"), T4("$T4"),

  // S registers
  S0("$S0"), S1("$S1"), S2("$S2"), S3("$S3");


  public String key;

  Registers(String _key) {
    key = _key;

  }

  public String returnKey() {
    return key;
  }

}
