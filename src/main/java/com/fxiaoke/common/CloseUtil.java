package com.fxiaoke.common;

/**
 * 关闭资源
 * Created by lirui on 2016-12-03 21:54.
 */
public final class CloseUtil {
  private CloseUtil() {
  }

  public static void closeQuietly(AutoCloseable c) {
    if (c != null) {
      try {
        c.close();
      } catch (Exception ignored) {
      }
    }
  }
}
