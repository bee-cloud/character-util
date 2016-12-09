package com.fxiaoke.common.image;

import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import org.junit.Test;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Created by lirui on 2016-12-09 17:48.
 */
public class SimpleImageInfoTest {
  @Test
  public void testGetTransparent() throws Exception {
    try (InputStream i = Resources.asByteSource(Resources.getResource("transparent.png")).openStream()) {
      Stopwatch sw = Stopwatch.createStarted();
      SimpleImageInfo info = new SimpleImageInfo(i);
      System.out.println("ellapse: " + sw.elapsed(TimeUnit.MILLISECONDS));
      assertEquals(794, info.getWidth());
      assertEquals(1123, info.getHeight());
      assertEquals("image/png", info.getMimeType());
      assertTrue(info.isAlpha());
    }
  }

  @Test
  public void testGetInfo2() throws Exception {
    try (InputStream i = Resources.asByteSource(Resources.getResource("not-transparent.png")).openStream()) {
      Stopwatch sw = Stopwatch.createStarted();
      SimpleImageInfo info = new SimpleImageInfo(i);
      System.out.println("ellapse: " + sw.elapsed(TimeUnit.MILLISECONDS));
      assertEquals(578, info.getWidth());
      assertEquals(261, info.getHeight());
      assertEquals("image/png", info.getMimeType());
      assertFalse(info.isAlpha());
    }
  }
}
