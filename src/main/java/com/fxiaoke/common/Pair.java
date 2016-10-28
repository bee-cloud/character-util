package com.fxiaoke.common;

/**
 * A simple object that holds onto a pair of object references, first and second.
 */
final public class Pair<F, S> {
  public final F first;
  public final S second;

  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }

  private static boolean eq(Object a, Object b) {
    return (a == b) || (a != null && a.equals(b));
  }

  /**
   * 通过值创建值对
   *
   * @param f 第一个值
   * @param s 第二个值
   * @return 值对
   */
  public static <F, S> Pair<F, S> build(F f, S s) {
    return new Pair<>(f, s);
  }

  @Override
  public int hashCode() {
    return 17 * ((first != null) ? first.hashCode() : 0) + 17 * ((second != null) ? second.hashCode() : 0);
  }

  @Override
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    }
    if (!(o instanceof Pair<?, ?>)) {
      return false;
    }
    Pair<?, ?> that = (Pair<?, ?>) o;
    return eq(this.first, that.first) && eq(this.second, that.second);
  }

  @Override
  public String toString() {
    return String.format("(%s,%s)", first, second);
  }
}
