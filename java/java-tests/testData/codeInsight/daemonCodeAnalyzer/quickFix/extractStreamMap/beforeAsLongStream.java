// "Extract variable 'l' to separate mapping method" "true"
import java.util.*;
import java.util.stream.*;

public class Test {
  long[] testAsLongStream(int[] x) {
    return Arrays.stream(x).mapToLong(i -> {
      int <caret>l = i * 2;
      return l;
    }).toArray();
  }
}