package enums;

public enum Stage {

  /**
   * First stage
   */
  IF("IF"),

  /**
   * Second stage
   */
  ID("ID"),

  /**
   * Third stage
   */
  EX("EX"),

  /**
   * Fourth stage
   */
  MEM("MEM"),

  /**
   * Fifth stage
   */
  WB("WB");

  String currentStage;

  Stage(String _currentStage) {
    currentStage = _currentStage;
  }

  public String toString() {
    return currentStage;

  }
}
