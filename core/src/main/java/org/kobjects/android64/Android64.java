package org.kobjects.android64;

import org.kobjects.android64.vic.Vic;
import org.kobjects.graphics.Screen;

public class Android64 {

  byte[] memory = new byte[65536];
  final Vic vic;
  final Screen screen;
  IntervalTree<MemoryListener> memoryListeners = new IntervalTree<>();

  public Android64(Screen screen) {
    this.screen = screen;
    vic = new Vic(this);
  }

  public Screen getScreen() {
    return screen;
  }

  public IntervalTree.IntervalNode<MemoryListener> addMemoryListener(int startAddress, int endAdress, MemoryListener listener) {
    return memoryListeners.add(startAddress, endAdress, listener);
  }

  public Android64 poke(int address, int value) {
    memory[address] = (byte) value;
    for (IntervalTree.IntervalNode<? extends MemoryListener> listenerInterval : memoryListeners.find(address)) {
      listenerInterval.data.set(address - listenerInterval.start, value);
    }
    return this;
  }


  public int peek(int address) {
    return memory[address] & 255;
  }


  public void removeMemoryManager(IntervalTree.IntervalNode<MemoryListener> node) {
    memoryListeners.remove(node);
  }
}
