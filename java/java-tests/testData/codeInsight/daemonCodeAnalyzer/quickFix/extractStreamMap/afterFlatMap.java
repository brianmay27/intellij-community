// "Extract variable 'y' to separate mapping method" "true"
import java.util.*;
import java.util.stream.*;

public class Test {
  void testFlatMap() {
      Stream.of("xyz").mapToInt(String::length).flatMap(y -> IntStream.range(0, y)).forEach(System.out::println);
  }
}