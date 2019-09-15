package org.kobjects.android64;

import org.kobjects.android64.sid.Sid;
import org.kobjects.android64.vic.Vic;
import org.kobjects.graphics.Screen;

public class Android64 {

  byte[] memory = new byte[65536];
  final Vic vic;
  final Sid sid;
  final Screen screen;
  IntervalTree<MemoryListener> memoryListeners = new IntervalTree<>();

  public Android64(Screen screen) {
    this.screen = screen;
    vic = new Vic(this);
    sid = new Sid(this);
    memory[1] = -1;
  }

  public Screen getScreen() {
    return screen;
  }

  public IntervalTree.IntervalNode<MemoryListener> addMemoryListener(int startAddress, int endAdress, MemoryListener listener) {
    return memoryListeners.add(startAddress, endAdress, listener);
  }

  public void poke(int address, int value) {
    if (memory[address] != value) {
      memory[address] = (byte) value;
      for (IntervalTree.IntervalNode<? extends MemoryListener> listenerInterval : memoryListeners.find(address)) {
        listenerInterval.data.set(address - listenerInterval.start, value);
      }
    }
  }

  public int peek(int address) {
    return 255 & ((address >= 0xd000 && address < 0xf000 && (memory[1] & 4) == 0)
        ? Rom.CHARACTER_DATA[address - 0xd000]
        : memory[address]);
  }

  public int dpeek(int address) {
    return peek(address) | (peek(address + 1) << 8);
  }


  void setCursor(int row, int col) {
    poke(0xd4, row % 25);
    poke(0xd6, col % 40);
  }

  public void cls() {
    for (int i = 0; i < 80* 25; i++) {
      poke(1024+i, 32);
    }
    setCursor(0, 0);
  }

  public void print(String s) {
    for (int i = 0; i < s.length(); i++) {
      print(s.charAt(i));
    }
  }


  public void print(char c) {
    int pos = 40 * peek(0xd3) + peek(0xd6);

    poke(1024 + pos, c > 64 ? c - 64 : c);
    pos++;

    poke(0xd3, pos / 40);
    poke(0xd6, pos % 40);
  }


  public void removeMemoryManager(IntervalTree.IntervalNode<MemoryListener> node) {
    memoryListeners.remove(node);
  }
}
