public class SumNumbers {
  public static void main(String[] args) {
    sumNumbers("abc123xyz");
    sumNumbers("aa11b33");
    sumNumbers("7 11");
  }

  public static void sumNumbers(String str) {
    String strNum = "";
    Boolean isNum = false;
    int result = 0;
    for(int i = 0; i < str.length(); i++) {
      if (Character.isDigit(str.charAt(i))) {
        strNum += str.charAt(i);
        isNum = true;
      } else {
        if (isNum) {
          result += Integer.parseInt(strNum);
        }
        strNum = "";
        isNum = false;
      }
    }
    if (!strNum.isEmpty()) {
      result += Integer.parseInt(strNum);
    }
    System.out.println(result);
  }
}
