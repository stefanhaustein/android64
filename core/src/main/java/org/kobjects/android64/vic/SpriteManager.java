package org.kobjects.android64.vic;

import android.graphics.Bitmap;
import android.graphics.Color;

import org.kobjects.android64.IntervalTree;
import org.kobjects.android64.MemoryListener;
import org.kobjects.graphics.Sprite;

class SpriteManager implements MemoryListener {
  private Vic vic;
  boolean multiColor;
  final int index;
  final Sprite sprite;
  final int[] colorTable = {0, Vic.ARGB_GREEN, Vic.ARGB_ORANGE, Color.BLACK};
  IntervalTree.IntervalNode<MemoryListener> node;
  final Bitmap bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);

  SpriteManager(Vic vic, int index) {
    this.vic = vic;
    this.index = index;
    this.sprite = new Sprite(vic.android64.getScreen());
    // this.sprite.setOpacity(0);
    sprite.setSize(Vic.fromPx(24));
    sprite.setBitmap(bitmap);
    setAddress(0);
  }

  void setColor(int index, int value) {
    if (colorTable[index] != Vic.PALETTE[value & 15]) {
      colorTable[index] = Vic.PALETTE[value & 15];
      refresh();
    }
  }

  void setMultiColor(boolean multiColor) {
    if (multiColor != this.multiColor) {
      this.multiColor = multiColor;
      refresh();
    }
  }

  @Override
  public void set(int address, int value) {
    if (multiColor) {
      for (int i = 0; i < 8; i += 2) {
        int y = address / 3;
        int x = (address % 3) * 8 + i;
        int color = colorTable[((value & (192>>i)) >> (6-i))];
        bitmap.setPixel(x, y, color);
        bitmap.setPixel(x + 1, y, color);
      }
    } else {
      for (int i = 0; i < 8; i ++) {
        int x = (address % 3) * 8 + i;
        int y = address / 3;
        int colorIndex = ((value & (128 >> i)) >> (7-i)) << 1;
        bitmap.setPixel(x, y, colorTable[colorIndex]);
      }
    }
    sprite.setBitmap(bitmap);
  }

  void refresh() {
    for (int i = 0; i < 63; i++) {
      set(i, vic.android64.peek(node.start + i));
    }
  }

  void setAddress(int start) {
    if (node != null) {
      vic.android64.removeMemoryManager(node);
    }
    node = vic.android64.addMemoryListener(start, start + 64, this);
    refresh();
  }
}
