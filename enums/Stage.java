package enums;
public enum Stage {

  /**
   * First stage
   */
  IF,

  /**
   * Second stage
   */
  ID,

  /**
   * Third stage
   */
  EX,

  /**
   * Fourth stage
   */
  MEM,

  /**
   * Fifth stage
   */
  WB;

  public String returnStage(Stage _currentStage) {

    String currentStage = null;
    switch (_currentStage) {
      case IF:
        currentStage = "IF";
      case ID:
        currentStage = "ID";
      case EX:
        currentStage = "EX";
      case MEM:
        currentStage = "MEM";
      case WB:
        currentStage = "WB";
    }
    return currentStage;
  }
}
