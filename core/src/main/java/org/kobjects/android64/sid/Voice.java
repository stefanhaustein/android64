package org.kobjects.android64.sid;


import org.kobjects.android64.Android64;
import org.kobjects.android64.IntervalTree;
import org.kobjects.android64.MemoryListener;
import org.kobjects.atg.ToneGenerator;

public class Voice implements MemoryListener {

  static final int[] ATTACK_RATE_TABLE = {
      2,   8,  16,  24,   38,   56,   68,   80,
    100, 240, 500, 800, 1000, 3000, 5000, 8000
  };

  Android64 android64;
  ToneGenerator toneGenerator = new ToneGenerator();
  IntervalTree.IntervalNode<MemoryListener> node;
  int startAddress;
  ToneGenerator.Tone currentTone;
  Sid sid;

  Voice(Sid sid, int startAddress) {
    this.sid = sid;
    this.startAddress = startAddress;
    android64 = sid.android64;
    // We only need to listen for the trigger, everything else will be read from memory at
    // start time.
    android64.addMemoryListener(startAddress + 4, startAddress + 5, this);
  }

  @Override
  public synchronized void set(int address, int value) {
    if ((value & 1) == 1) {
      start(value);
    } else if (currentTone != null) {
      currentTone.end();
      currentTone = null;
    }
  }

  void start(int value) {
    switch (value & 0xf0) {
      case 16:
        toneGenerator.setWaveForm(ToneGenerator.TRIANGLE);
        break;
      case 32:
        toneGenerator.setWaveForm(ToneGenerator.SAWTOOTH);
        break;
      case 64:
        float pulseWidth = (android64.dpeek(startAddress + 2) & 0x0fff) / 4095f;
        toneGenerator.setWaveForm(x -> x < pulseWidth ? -1 : 1);
        break;
      case 128:
        toneGenerator.setWaveForm(ToneGenerator.NOISE);
        break;
    }
    int attackDecay = android64.peek(startAddress + 5);
    toneGenerator.setAttackTimeMs(ATTACK_RATE_TABLE[attackDecay & 15]);
    toneGenerator.setDecayTimeMs(3 * ATTACK_RATE_TABLE[attackDecay / 16]);

    int sustainRelease = android64.peek(startAddress + 6);
    toneGenerator.setSustain((sustainRelease / 16) / 15f);
    toneGenerator.setReleaseTimeMs(3 * ATTACK_RATE_TABLE[sustainRelease % 15]);

    currentTone = toneGenerator.start(android64.dpeek(startAddress) * 0.06f);
  }


}
