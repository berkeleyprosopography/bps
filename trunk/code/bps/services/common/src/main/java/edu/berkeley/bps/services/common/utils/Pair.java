/**
 * A generic-typed pair of objects.
 * @author pschmitz
 */
package edu.berkeley.bps.services.common.utils;

public class Pair<F,S> {
  F first;
  S second;

  public F getFirst() {
    return first;
  }

  public S getSecond() {
    return second;
  }

  public void setFirst(F pFirst) {
    first = pFirst;
  }

  public void setSecond(S pSecond) {
    second = pSecond;
  }

  @SuppressWarnings("unchecked")
@Override
public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Pair)) return false;

    final Pair<F, S> pair = (Pair<F, S>) o;

    if (first != null ? !first.equals(pair.first) : pair.first != null) return false;
    if (second != null ? !second.equals(pair.second) : pair.second != null) return false;

    return true;
  }

  @Override
public int hashCode() {
    int result;
    result = (first != null ? first.hashCode() : 0);
    result = 29 * result + (second != null ? second.hashCode() : 0);
    return result;
  }

  @Override
public String toString() {
    return "(" + getFirst() + ", " + getSecond() + ")";
  }

  public Pair(F first, S second) {
    this.first = first;
    this.second = second;
  }
}
