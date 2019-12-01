package org.kobjects.android64.vic;

import android.graphics.Bitmap;

import org.kobjects.android64.Android64;
import org.kobjects.android64.IntervalTree;
import org.kobjects.android64.MemoryListener;
import org.kobjects.android64.Rom;
import org.kobjects.graphics.Sprite;

public class ScreenManager implements MemoryListener {

  private final Vic vic;
  private final Android64 android64;
  private IntervalTree.IntervalNode<MemoryListener> intervalNode;
  final Sprite screenSprite;
  final Bitmap screenBitmap;

  ScreenManager(Vic vic) {
    this.vic = vic;
    android64 = vic.android64;
    screenBitmap = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
    screenSprite = new Sprite(vic.android64.getScreen());
    screenSprite.setSize(Vic.fromPx(320));
    screenSprite.setBitmap(screenBitmap);

    intervalNode = vic.android64.addMemoryListener(1024, 2023, this);

    vic.android64.addMemoryListener(0xd800, 0xdbe8, (address, value) -> setColor(address, value&15));
  }

  void setColor(int address, int color) {
    set(address, android64.peek(1024 + address));
  }

  @Override
  public void set(int address, int value) {
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
  }
}
