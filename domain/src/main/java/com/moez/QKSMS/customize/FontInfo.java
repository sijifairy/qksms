package com.moez.QKSMS.customize;

public class FontInfo {

  public String fontName;
  public String fontId;
  public boolean isLocalFont;

  public FontInfo(String id, String name) {
    this.fontId = id;
    this.fontName = name;
    isLocalFont = true;
  }
}
