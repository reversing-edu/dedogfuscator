package edu.reversing.commons;

import java.util.function.*;

public class Pair<L, R> {

  private final L left;
  private final R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public static <L, R> Pair<L, R> nil() {
    return new Pair<>(null, null);
  }

  public static <L, R> Pair<L, R> toMapPair(L left, Function<L, R> right) {
    R r = right.apply(left);
    return r == null ? null : new Pair<>(left, r);
  }

  public L getLeft() {
    return left;
  }

  public R getRight() {
    return right;
  }

  public <T> T accumulate(BiFunction<L, R, T> function) {
    return function.apply(left, right);
  }

  public <LL, RR> Pair<LL, RR> map(Function<L, LL> fun1, Function<R, RR> fun2) {
    return new Pair<>(fun1.apply(left), fun2.apply(right));
  }

  public boolean isPresent() {
    return left != null && right != null;
  }

  /**
   * @param predicate The predicate to test the elements
   * @return The pair if the predicate accepts it, else returns null
   */
  public Pair<L, R> filter(BiPredicate<L, R> predicate) {
    if (predicate.test(left, right)) {
      return this;
    }
    return new Pair<>(null, null);
  }

  /**
   * If neither element is null, applies the given consumer
   *
   * @param consumer The operation to perform on the elements
   */
  public void ifPresent(BiConsumer<L, R> consumer) {
    if (left != null && right != null) {
      consumer.accept(left, right);
    }
  }

  @Override
  public String toString() {
    return "Pair[L=" + left + " | R=" + right + "]";
  }
}