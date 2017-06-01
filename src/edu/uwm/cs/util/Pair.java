package edu.uwm.cs.util;

public class Pair<T1, T2> {

  public final T1 fst;
  public final T2 snd;
  
  public Pair(T1 e1, T2 e2) {
    fst = e1;
    snd = e2;
  }
  
  @Override
  public int hashCode() {
    final int h1, h2;
    if (fst == null) h1 = 0; else h1 = fst.hashCode();
    if (snd == null) h2 = 0; else h2 = snd.hashCode();
    return h1 << 7 + h2;
  }
  
  @Override
  public boolean equals(Object x) {
    if (!(x instanceof Pair<?,?>)) return false;
    Pair<?,?> pair = (Pair<?,?>)x;
    return (fst == null ? pair.fst == null : fst.equals(pair.fst)) &&
           (snd == null ? pair.snd == null : snd.equals(pair.snd));
  }
  
  @Override
  public String toString() {
    return "<" + fst + "," + snd + ">";
  }
  
  public static <T1,T2> Pair<T1,T2> create(T1 e1, T2 e2) {
    return new Pair<T1,T2>(e1,e2);
  }
}
