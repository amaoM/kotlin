public class StringSplosion {
  public static void main(String[] args) {
    stringSplosion("Code");
    stringSplosion("abc");
    stringSplosion("ab");
  }

  public static void stringSplosion(String str) {
    String result = "";
    for (int i = 0; i < str.length(); i++) {
      result += str.substring(0, i + 1);
    }
    System.out.println(result);
  }
}
