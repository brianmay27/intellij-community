// "Extract variable 'lowerCase' to separate mapping method" "true"
import java.util.*;
import java.util.stream.*;

public class Test {
  boolean test(List<String> list) {
    return list.stream()
      .anyMatch(s -> {
        String <caret>lowerCase = s.toLowerCase();
        return "test".equals(lowerCase);
      });
  }
}