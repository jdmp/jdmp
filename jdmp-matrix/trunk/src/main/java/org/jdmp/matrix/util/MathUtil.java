package org.jdmp.matrix.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.random.RandomDataImpl;


public abstract class MathUtil {

  private static final Logger logger = Logger.getLogger(MathUtil.class
      .getName());

  private static final double ROOT2PI = Math.sqrt(2 * Math.PI);

  private static long seed = System.nanoTime();

  private static RandomDataImpl random = new RandomDataImpl();

  static {
    random.reSeed(seed);
  }



  public static String getMD5Sum(String text) {
    MessageDigest mdAlgorithm;
    StringBuffer hexString = new StringBuffer();

    try {
      mdAlgorithm = MessageDigest.getInstance("MD5");
      mdAlgorithm.update(text.getBytes());
      byte[] digest = mdAlgorithm.digest();

      for (int i = 0; i < digest.length; i++) {
        text = Integer.toHexString(0xFF & digest[i]);

        if (text.length() < 2) {
          text = "0" + text;
        }

        hexString.append(text);
      }
    } catch (NoSuchAlgorithmException e) {
      logger.log(Level.WARNING, "algorithm not found", e);
    }

    return hexString.toString();
  }



  public static final RandomDataImpl getRandom() {
    return random;
  }



  public static final long getSeed() {
    return seed;
  }



  public static void setSeed(long seed) {
    MathUtil.seed = seed;
    random.reSeed(seed);
  }



  public static double log2(double d) {
    return Math.log(d) / Math.log(2.0);
  }



  public static double log10(double d) {
    return Math.log(d) / Math.log(10.0);
  }



  public static int hash(int h) {
    // This function ensures that hashCodes that differ only by
    // constant multiples at each bit position have a bounded
    // number of collisions (approximately 8 at default load factor).
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
  }



  public static final double gauss(double mean, double sigma, double x) {
    return Math.exp(-0.5 * Math.pow((x - mean) / sigma, 2.0))
        / (sigma * ROOT2PI);
  }



  public static final double artanh(double x) {
    return 0.5 * Math.log((1 + x) / (1 - x));
  }



  public static final double nextGaussian(double mean, double sigma) {
    return sigma <= 0.0 ? 0.0 : random.nextGaussian(mean, sigma);
  }



  public static final double nextUniform(double min, double max) {
    if (min == max)
      max += Double.MIN_VALUE;
    return random.nextUniform(min < max ? min : max, max > min ? max : min);
  }



  /**
   * Returns a random value in the desired interval
   * 
   * @param min
   *            minimum value (inclusive)
   * @param max
   *            maximum value (inclusive)
   * @return a random value
   */
  public static final int nextInteger(int min, int max) {
    if (min == max) {
      new Exception().printStackTrace();
    }
    return random.nextInt(min < max ? min : max, max > min ? max : min);
  }



  public static boolean isEventHappening(double probability) {
    return nextUniform(0.0, 1.0) < probability;
  }



  public static boolean nextBoolean() {
    return nextGaussian(0.0, 1.0) > 0;
  }



  public static double nextDouble() {
    return random.nextUniform(0.0, 1.0);
  }



  public static double ignoreNaN(double v) {
    return (v != v) || (v == Double.POSITIVE_INFINITY)
        || (v == Double.NEGATIVE_INFINITY) ? 0.0 : v;
  }



  public static boolean isNaNOrInfinite(double v) {
    return (v != v) || (v == Double.POSITIVE_INFINITY)
        || (v == Double.NEGATIVE_INFINITY);
  }



  public static boolean isNaNOrInfinite(Object o) {
    return Double.valueOf(Double.NaN).equals(o)
        || Double.valueOf(Double.POSITIVE_INFINITY).equals(o)
        || Double.valueOf(Double.NEGATIVE_INFINITY).equals(o);
  }



  public static final double getDouble(Object o) {
    if (o == null) {
      return 0.0;
    } else if (o instanceof Date) {
      return ((Date) o).getTime();
    } else {
      try {
        return Double.parseDouble(o.toString());
      } catch (Exception e) {
      }
    }
    return Double.NaN;
  }



  public static long[] listToLong(List<? extends Number> numbers) {
    long[] ret = new long[numbers.size()];
    for (int i = ret.length; --i >= 0;) {
      ret[i] = numbers.get(i).longValue();
    }
    return ret;
  }



  public static double[] listToDouble(List<? extends Number> numbers) {
    double[] ret = new double[numbers.size()];
    for (int i = ret.length; --i >= 0;) {
      ret[i] = numbers.get(i).doubleValue();
    }
    return ret;
  }



  public static int[] listToInt(List<? extends Number> numbers) {
    int[] ret = new int[numbers.size()];
    for (int i = ret.length; --i >= 0;) {
      ret[i] = numbers.get(i).intValue();
    }
    return ret;
  }



  public static List<Long> toLongList(long[] numbers) {
    List<Long> ret = new ArrayList<Long>(numbers.length);
    for (int i = 0; i < numbers.length; i++) {
      ret.add(numbers[i]);
    }
    return ret;
  }



  public static List<Long> toLongList(int[] numbers) {
    List<Long> ret = new ArrayList<Long>(numbers.length);
    for (int i = 0; i < numbers.length; i++) {
      ret.add((long) numbers[i]);
    }
    return ret;
  }



  public static List<Double> toDoubleList(double[] numbers) {
    List<Double> ret = new ArrayList<Double>(numbers.length);
    for (int i = 0; i < numbers.length; i++) {
      ret.add(numbers[i]);
    }
    return ret;
  }



  public static List<Double> toDoubleList(int[] numbers) {
    List<Double> ret = new ArrayList<Double>(numbers.length);
    for (int i = 0; i < numbers.length; i++) {
      ret.add((double) numbers[i]);
    }
    return ret;
  }



  public static List<Double> toDoubleList(long[] numbers) {
    List<Double> ret = new ArrayList<Double>(numbers.length);
    for (int i = 0; i < numbers.length; i++) {
      ret.add((double) numbers[i]);
    }
    return ret;
  }



  public static double[] toDoubleArray(int... intArray) {
    int nmb = intArray.length;
    double[] ret = new double[nmb];
    for (int i = 0; i < nmb; i++)
      ret[i] = intArray[i];
    return ret;
  }



  public static double[][] toDoubleArray(int[]... intArray) {
    int rows = intArray.length;
    if (rows <= 0)
      return new double[0][0];
    int cols = intArray[0].length;
    double[][] ret = new double[rows][cols];
    for (int i = rows - 1; i >= 0; i--) {
      for (int j = cols - 1; j >= 0; j--) {
        ret[i][j] = intArray[i][j];
      }
    }
    return ret;
  }



  public static List<Long> sequenceListLong(long start, long end) {
    List<Long> list = new ArrayList<Long>();

    if (start < end) {
      for (long l = start; l <= end; l++) {
        list.add(l);
      }
    } else {
      for (long l = start; l >= end; l--) {
        list.add(l);
      }
    }
    return list;
  }



  public static long[] sequenceLong(long start, long end) {
    return listToLong(sequenceListLong(start, end));
  }



  public static List<Long> randPerm(long start, long end) {
    List<Long> list = sequenceListLong(start, end);
    Collections.shuffle(list);
    return list;
  }

}
