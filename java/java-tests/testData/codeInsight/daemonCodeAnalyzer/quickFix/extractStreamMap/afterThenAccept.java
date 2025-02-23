// "Extract variable 'lc' to separate mapping method" "true"
import java.util.concurrent.CompletableFuture;

public class Test {
  public static void main(String[] args) {
      CompletableFuture.completedFuture("  XYZ  ")
              .thenApply(x -> x.trim() + "|" + x.trim()).thenApply(String::toLowerCase)
              .thenAccept(lc -> System.out.println(lc));
  }
}