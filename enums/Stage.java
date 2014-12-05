package enums;

public enum Stage {

  /**
   * First stage
   */
  IF(0),

  /**
   * Second stage
   */
  ID(1),

  /**
   * Third stage
   */
  EX(2),

  /**
   * Fourth stage
   */
  MEM(3),

  /**
   * Fifth stage
   */
  WB(4),
  /**
   * Done stage
   */
  EXIT(5);

  int currentStage;

  Stage(int _currentStage) {
    currentStage = _currentStage;
  }

  public void incrementStage() {
    currentStage++;
  }

  public int getStage() {
    return currentStage;

  }

  public String toString() {
    String stageName = null;
    switch (currentStage) {
      case 0:
        stageName = "IF: ";
        break;
      case 1:
        stageName = "ID: ";
        break;
      case 2:
        stageName = "EX: ";
        break;
      case 3:
        stageName = "MEM: ";
        break;
      case 4:
        stageName = "WB: ";
        break;
      case 5:
        stageName = "EXIT: ";
        break;
    }
    return stageName;

  }
}
