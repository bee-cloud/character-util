/*
 *	SimpleImageInfo.java
 *
 *	@version 0.1
 *	@author  Jaimon Mathew <http://www.jaimon.co.uk>
 *
 *	A Java class to determine image width, height and MIME types for a number of image file formats without loading the whole image data.
 *
 *	Revision history
 *	0.1 - 29/Jan/2011 - Initial version created
 *
 *  -------------------------------------------------------------------------------

 	This code is licensed under the Apache License, Version 2.0 (the "License");
 	You may not use this file except in compliance with the License.

 	You may obtain a copy of the License at

 	http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.

 *  -------------------------------------------------------------------------------
 */

package com.fxiaoke.common.image;

import java.io.*;

@SuppressWarnings("all")
public class SimpleImageInfo {
  private int height;
  private int width;
  private String mimeType = "";
  private boolean alpha;

  private SimpleImageInfo() {

  }

  public SimpleImageInfo(File file) throws IOException {
    try (InputStream is = new FileInputStream(file)) {
      processStream(is);
    }
  }

  public SimpleImageInfo(InputStream is) throws IOException {
    processStream(is);
  }

  public SimpleImageInfo(byte[] bytes) throws IOException {
    try (InputStream is = new ByteArrayInputStream(bytes)) {
      processStream(is);
    }
  }

  private void processStream(InputStream is) throws IOException {
    int c1 = is.read();
    int c2 = is.read();
    int c3 = is.read();

    mimeType = null;
    width = height = -1;

    if (c1 == 'G' && c2 == 'I' && c3 == 'F') { // GIF
      is.skip(3);
      width = readInt(is, 2, false);
      height = readInt(is, 2, false);
      mimeType = "image/gif";
    } else if (c1 == 0xFF && c2 == 0xD8) { // JPG
      while (c3 == 255) {
        int marker = is.read();
        int len = readInt(is, 2, true);
        if (marker == 192 || marker == 193 || marker == 194) {
          is.skip(1);
          height = readInt(is, 2, true);
          width = readInt(is, 2, true);
          mimeType = "image/jpeg";
          break;
        }
        is.skip(len - 2);
        c3 = is.read();
      }
    } else if (c1 == 137 && c2 == 80 && c3 == 78) { // PNG
      is.skip(13);
      width = readInt(is, 4, true);
      height = readInt(is, 4, true);
      is.skip(1); // bit depth
      int colorType = is.read();
      alpha = colorType > 4;
      mimeType = "image/png";
    } else if (c1 == 66 && c2 == 77) { // BMP
      is.skip(15);
      width = readInt(is, 2, false);
      is.skip(2);
      height = readInt(is, 2, false);
      mimeType = "image/bmp";
    } else if (c1 == 'R' && c2 == 'I' && c3 == 'F') {
      int c4 = is.read();
      is.skip(4);
      int c9 = is.read();
      int c10 = is.read();
      int c11 = is.read();
      int c12 = is.read();
      is.skip(14);
      int c26 = is.read();
      int c27 = is.read();
      int c28 = is.read();
      int c29 = is.read();
      if (c4 == 'F' && c9 == 'W' && c10 == 'E' && c11 == 'B' && c12 == 'P') { //WEBP
        mimeType = "image/webp";
        //WebP格式图片的长宽信息位于文件头第26、27（高），28、29（宽）这四个字节中。
        height = (((int) c29 & 0xff) << 8) | (int) c28 & 0xff;
        width = (((int) c27 & 0xff) << 8) | (int) c26 & 0xff;
      }
    } else {
      int c4 = is.read();
      if ((c1 == 'M' && c2 == 'M' && c3 == 0 && c4 == 42) || (c1 == 'I' && c2 == 'I' && c3 == 42 && c4 == 0)) { //TIFF
        boolean bigEndian = c1 == 'M';
        int ifd = 0;
        int entries;
        ifd = readInt(is, 4, bigEndian);
        is.skip(ifd - 8);
        entries = readInt(is, 2, bigEndian);
        for (int i = 1; i <= entries; i++) {
          int tag = readInt(is, 2, bigEndian);
          int fieldType = readInt(is, 2, bigEndian);
          long count = readInt(is, 4, bigEndian);
          int valOffset;
          if ((fieldType == 3 || fieldType == 8)) {
            valOffset = readInt(is, 2, bigEndian);
            is.skip(2);
          } else {
            valOffset = readInt(is, 4, bigEndian);
          }
          if (tag == 256) {
            width = valOffset;
          } else if (tag == 257) {
            height = valOffset;
          }
          if (width != -1 && height != -1) {
            mimeType = "image/tiff";
            break;
          }
        }
      }
    }
  }


  private int readInt(InputStream is, int noOfBytes, boolean bigEndian) throws IOException {
    int ret = 0;
    int sv = bigEndian ? ((noOfBytes - 1) * 8) : 0;
    int cnt = bigEndian ? -8 : 8;
    for (int i = 0; i < noOfBytes; i++) {
      ret |= is.read() << sv;
      sv += cnt;
    }
    return ret;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public boolean isAlpha() {
    return alpha;
  }

  public void setAlpha(boolean alpha) {
    this.alpha = alpha;
  }

  @Override
  public String toString() {
    return "MIME Type: " + mimeType + "\t Width: " + width + "\t Height: " + height + "\talpha: " + alpha;
  }
}
