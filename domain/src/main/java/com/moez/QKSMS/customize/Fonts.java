package com.moez.QKSMS.customize;

import java.util.ArrayList;
import java.util.List;

public class Fonts {

  public static String FONT_DEFAULT = "default";

  public static List<FontInfo> fonts = new ArrayList<>();

  static {
    fonts.add(new FontInfo(FONT_DEFAULT, "Default"));
    fonts.add(new FontInfo("merienda", "Merienda"));
    fonts.add(new FontInfo("mali", "Mali"));
    fonts.add(new FontInfo("quicksand", "Quicksand"));
    fonts.add(new FontInfo("lobster_two", "Lobster Two"));
    fonts.add(new FontInfo("el_messiri", "El Messiri"));
    fonts.add(new FontInfo("averia_libre", "Averia Libre"));
    fonts.add(new FontInfo("hind_siliguri", "Hind Siliguri"));
    fonts.add(new FontInfo("charm", "Charm"));
    fonts.add(new FontInfo("dancing_script", "Dancing Script"));
    fonts.add(new FontInfo("krub", "Krub"));
    fonts.add(new FontInfo("sarpanch", "Sarpanch"));
    fonts.add(new FontInfo("laila", "Laila"));
    fonts.add(new FontInfo("lemonada", "Lemonada"));
  }
}
