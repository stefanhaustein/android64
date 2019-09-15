package org.kobjects.android64.sid;

import org.kobjects.android64.Android64;

public class Sid {

  final Android64 android64;
  final Voice[] voices = new Voice[3];

  public Sid(Android64 android64) {
    this.android64 = android64;

    for (int i = 0; i < 3; i++) {
      voices[i] = new Voice(this, 54272 + i * 7);
    }
  }

}
