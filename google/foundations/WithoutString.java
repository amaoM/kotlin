public class WithoutString {
  public static void main(String[] args) {
    withoutString("Hello there", "llo");
    withoutString("Hello there", "e");
    withoutString("Hello there", "x");
    withoutString("abxxxab", "xx");
  }
}

public static void withoutString(String base, String remove) {
  String result = "";
  int i = 0;
  for (int ii = 0; ii < base.length(); ii++) {
    if (Character.toLowerCase(base.charAt(ii)) != Character.toLowerCase(remove.charAt(i))) {
      result += remove.substring(0, i) + base.charAt(ii);
      i = 0;
    } else {
      if (remove.length() - 1 > i) {
        i += 1;
      } else {
        i = 0;
      }
    }
  }
  result += remove.substring(0, i);
  System.out.println(result);
}
