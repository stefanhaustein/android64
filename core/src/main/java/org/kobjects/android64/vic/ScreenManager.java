package org.kobjects.android64.vic;

import android.graphics.Bitmap;

import org.kobjects.android64.Android64;
import org.kobjects.android64.IntervalTree;
import org.kobjects.android64.MemoryListener;
import org.kobjects.android64.Rom;
import org.kobjects.graphics.Sprite;

public class ScreenManager {

  private final Android64 android64;
  private IntervalTree.IntervalNode<MemoryListener> intervalNode;
  final Sprite screenSprite;
  final Bitmap screenBitmap;
  final MemoryListener screenRam;
  final MemoryListener colorRam;

  ScreenManager(Vic vic) {
    android64 = vic.android64;
    screenBitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
    screenSprite = new Sprite(vic.android64.getScreen());
    screenSprite.setSize(Vic.fromPx(320));
    screenSprite.setBitmap(screenBitmap);

    screenRam = (address, value) -> {
      int x0 = (address % 40) * 8;
      int y0 = (address / 40) * 8 + 60;

      int charPos = value * 8;
      int color = Vic.PALETTE[android64.peek(0xd800 + address) & 15];

      for (int y = y0; y < y0 + 8; y++) {
        int b = Rom.CHARACTER_DATA[charPos++];
        for (int i = 0; i < 8; i++) {
          screenBitmap.setPixel(x0 + i, y, ((b << i) & 128) == 0 ? 0 : color);
        }
      }
      screenSprite.setBitmap(screenBitmap);
    };
    intervalNode = vic.android64.addMemoryListener(1024, 2024, screenRam);

    colorRam = (address, value) -> screenRam.set(address, android64.peek(1024 + address));
    vic.android64.addMemoryListener(0xd800, 0xdbe8, colorRam);
  }


  void cls() {
    for (int addr = intervalNode.start; addr < intervalNode.end; addr++) {
      android64.poke(addr, 32);
    }
    for (int addr = 0xd800; addr < 0xdbe8; addr++) {
      android64.poke(addr, 14);
    }
    // clear in the sprite library makes all sprites invisible.
    screenSprite.setVisible(true);
  }
}
