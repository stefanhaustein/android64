package org.kobjects.android64;

import android.graphics.Bitmap;

import org.kobjects.graphics.Sprite;


public class Vic implements MemoryListener {

  public static final int ARGB_BLACK = 0xff000000;
  public static final int ARGB_WHITE = 0xffffffff;
  public static final int ARGB_RED = 0xff68372b;
  public static final int ARGB_CYAN = 0xff70a4b2;
  public static final int ARGB_PURPLE = 0xff6f3d86;
  public static final int ARGB_GREEN = 0xff588d43;
  public static final int ARGB_BLUE = 0xff352879;
  public static final int ARGB_YELLOW = 0xffb8c76f;
  public static final int ARGB_ORANGE = 0xff6f4f25;
  public static final int ARGB_BROWN = 0xff433900;
  public static final int ARGB_LIGHT_RED = 0xff9a6759;
  public static final int ARGB_DARK_GREY = 0xff444444;
  public static final int ARGB_GREY = 0xff6c6c6c;
  public static final int ARGB_LIGHT_GREEN = 0xff9ad284;
  public static final int ARGB_LIGHT_BLUE = 0xff7c5eb5;
  public static final int ARGB_LIGHT_GREY = 0xff959595;

  static final int[] PALETTE = {
      ARGB_BLACK,
      ARGB_WHITE,
      ARGB_RED,
      ARGB_CYAN,
      ARGB_PURPLE,
      ARGB_GREEN,
      ARGB_BLUE,
      ARGB_YELLOW,
      ARGB_ORANGE,
      ARGB_BROWN,
      ARGB_LIGHT_RED,
      ARGB_DARK_GREY,
      ARGB_GREY,
      ARGB_LIGHT_GREEN,
      ARGB_LIGHT_BLUE,
      ARGB_LIGHT_GREY
  };

  final Android64 android64;
  final SpriteManager[] spriteManagers = new SpriteManager[8];

  Vic(Android64 android64) {
    this.android64 = android64;
    android64.addMemoryListener(0x7f8, 0x7ff, new SpriteAddressManager());
    android64.addMemoryListener(0xD000, 0xd030, this);
    for (int i = 0; i < 8; i++) {
      spriteManagers[i] = new SpriteManager(i);
    }
  }


  public void poke(int address, int value) {
    switch (address) {
      case 0x00:
      case 0x02:
      case 0x04:
      case 0x06:
      case 0x08:
      case 0x0a:
      case 0x0c:
      case 0x0e: {
        int number = address / 2;
        spriteManagers[number].sprite.setX(fromPx(value + 256 * (android64.peek(0xD010) & 1)) - 100);
        break;
      }
      case 0x01:
      case 0x03:
      case 0x05:
      case 0x07:
      case 0x09:
      case 0x0b:
      case 0x0d:
      case 0x0f:
        spriteManagers[address / 2].sprite.setY(fromPx(value-12) - 100);
        break;
      case 0x10:
        for (int i = 0; i < 8; i++) {
          poke(i, android64.peek(0xd000 + i));
        }
        break;
      case 0x15:
        // Enable / disable
        for (int i = 0; i < 8; i++) {
          spriteManagers[i].sprite.setOpacity((value & (1 << i)) == 0 ? 0 : 1);
        }
        break;
      case 0x17:
        // double vertically
        break;
      case 0x1b:
        // Priority wrt. background
        break;
      case 0x1c:
        for (int i = 0; i < 8; i++) {
          spriteManagers[i].setMultiColor((value & (1 << i)) != 0);
        }
        break;
      case 0x1d:
        // double horizontally
        break;
      case 0x25:
      case 0x26:
        for (SpriteManager spriteDataManager : spriteManagers) {
          spriteDataManager.setColor(address == 0x25 ? 1 : 3, value);
        }
        break;
      case 0x27:
      case 0x28:
      case 0x29:
      case 0x2a:
      case 0x2b:
      case 0x2c:
      case 0x2d:
      case 0x2e:
        spriteManagers[address - 0x27].setColor(2, value);
        break;
    }
  }

  static final float fromPx(int px) {
    return 20 * px / 32f;
  }


  class SpriteAddressManager implements MemoryListener {
    @Override
    public void poke(int address, int value) {
      spriteManagers[address].setAddress(value * 64);
    }
  }

  class SpriteManager implements MemoryListener {
    boolean multiColor;
    final int index;
    final Sprite sprite;
    final int[] colorTable = new int[4];
    IntervalTree.IntervalNode<MemoryListener> node;
    final Bitmap bitmap = Bitmap.createBitmap(24, 24, Bitmap.Config.ARGB_8888);

    SpriteManager(int index) {
      this.index = index;
      this.sprite = new Sprite(android64.screen);
      // this.sprite.setOpacity(0);
      sprite.setSize(fromPx(24));
      sprite.setBitmap(bitmap);
      setAddress(0);
    }

    void setColor(int index, int value) {
      if (colorTable[index] != PALETTE[value&15]) {
        colorTable[index] = PALETTE[value&15];
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
    public void poke(int address, int value) {
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
          bitmap.setPixel((address % 3) * 8 + i, address / 3, colorTable[((value & (128 >> i)) >> (7-i)) << 1]);
        }
      }
    }

    void refresh() {
      for (int i = 0; i < 63; i++) {
        poke(i, android64.peek(node.start + i));
      }
    }

    void setAddress(int start) {
      if (node != null) {
        android64.removeMemoryManager(node);
      }
      node = android64.addMemoryListener(start, start + 64, this);
      refresh();
    }
  }
}