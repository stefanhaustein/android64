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
  final MemoryListener screenRam;
  final MemoryListener colorRam;

  ScreenManager(Vic vic) {
    android64 = vic.android64;

    screenRam = (address, value) -> {
      Bitmap bitmap = android64.getScreen().getBitmap();

      int x0 = (bitmap.getWidth() - 320) / 2 + (address % 40) * 8;
      int y0 = (bitmap.getHeight() - 200) / 2 + (address / 40) * 8;

      int charPos = value * 8;
      int color = Vic.PALETTE[android64.peek(0xd800 + address) & 15];

      for (int y = y0; y < y0 + 8; y++) {
        int b = Rom.CHARACTER_DATA[charPos++];
        for (int i = 0; i < 8; i++) {
          bitmap.setPixel(x0 + i, y, ((b << i) & 128) == 0 ? 0 : color);
        }
      }
    };
    intervalNode = vic.android64.addMemoryListener(1024, 2024, screenRam);

    colorRam = (address, value) -> screenRam.set(address, android64.peek(1024 + address));
    vic.android64.addMemoryListener(0xd800, 0xdbe8, colorRam);
  }


  void cls() {
    // Fill screen memory with the space character
    for (int addr = intervalNode.start; addr < intervalNode.end; addr++) {
      android64.poke(addr, 32);
    }
    // Set color to 14
    for (int addr = 0xd800; addr < 0xdbe8; addr++) {
      android64.poke(addr, 14);
    }
  }
}
