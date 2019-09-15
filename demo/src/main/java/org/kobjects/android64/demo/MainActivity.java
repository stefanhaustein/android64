package org.kobjects.android64.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.android64.Android64;
import org.kobjects.android64.vic.Vic;
import org.kobjects.graphics.Screen;

import java.util.concurrent.LinkedBlockingQueue;


public class MainActivity extends Activity {

  static final int V = 53248;
  static final int S = 54272;
  static final int FRAME_MS = 1000/60;

  Android64 a64;
  LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
  boolean paused;

  private void wait(int ms) {
    if (!paused) {
      queue.add("token");
    }
    try {
      Thread.sleep(ms);
      queue.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  void poke(int addr, int value) {
    a64.poke(addr, value);
  }

  int peek(int addr) {
    return a64.peek(addr);
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO: Shouldn't be needed here -- init sprites to "something else" by default...
    EmojiManager.install(new TwitterEmojiProvider());

    Screen screen = new Screen(this);
    a64 = new Android64(screen);
    screen.view.setBackgroundColor(Vic.ARGB_BLUE);
    setContentView(screen.view);

    about();
  }


  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add("About").setOnMenuItemClickListener(x -> {about(); return true;});
    menu.add("Character Set").setOnMenuItemClickListener(x -> {new Thread(() -> charset()).start(); return true;});
    menu.add("C64 Wiki Sprite Demo").setOnMenuItemClickListener(x -> {new Thread(() -> c64wikiSpriteDemo()).start(); return true;});
    menu.add("SID Demo").setOnMenuItemClickListener(x -> {new Thread(() -> sidDemo()).start(); return true;});

    return true;
  }

  public void charset() {
    for (int i = 1024; i <2024; i++) {
      poke(i, i & 255);
    }
  }

  public void about() {
    a64.cls();
    a64.print("ANDROID64 " + Runtime.getRuntime().freeMemory() + " KOTLIN BYTES FREE.");
  }


  public void c64wikiSpriteDemo() {
    final int[] DATA =  {
      // REM SPRITE C64-WIKI.DE (einfarbig; Sprite 0)
      239,81,85,139,81,20,137,81,28,137,81,89,137,213,89,142,85,93,138,
      95,85,138,91,85,238,91,85,0,0,0,0,0,0,0,0,0,
      0,199,0,0,231,0,0,164,0,0,180,0,0,151,0,0,180,0,0,164,0,0,231,0,0,199,0,

      // REM Multicolor-Sprite Linie (Sprite 1&2)
      0,255,255,255,170,85,170,170,85,170,85,170,85,85,170,85,255,255,255
    };

    // REM Bildschirmbereinigung
    poke(V + 33, 0);


    // REM Sprite-Generierung
    for (int x = 12800; x <= 12881; x++) {
      poke(x, DATA[x - 12800]);
    }

    // REM Multicolor für Sprite 1&2
    poke(2040, 200); poke(2041, 201); poke(2042, 201); poke(V + 21, 7);
    poke(V + 28, 6); poke(V + 37, 15); poke(V + 38, 2);

    // REM Sprite-Farbe Sprite 0&1&2
    poke(V + 39, 7); poke(V + 40, 8); poke(V + 41, 6);

    // REM Sprite-Eigenschaften Höhe, Breite, X-Position
    poke(V + 23, 7); poke(V + 29, 7); poke(V + 16, 1);

      // REM X-/Y-Positionen
    poke(V + 1, 133); poke(V + 2, 170); poke(V + 5, 115);

    // REM Bewegung und Farbänderungen
      double z =0;
      for (int x = 200; x >= 1; x--) {
        poke(V, x);
        z += 0.61;
        poke(V + 3, (int) z);
        poke(V + 4, (201 - x) / 2);
        wait(FRAME_MS);
      }

    poke(V +16, 0); poke(V, 255);
    int m = peek(V + 4);

    for (int x = 255; x >= 170; x--) {
      poke(V, x);
      z = z + 0.66;
      poke(V + 3, (int) z);
      poke(V + 4, (int) (m + (256 - x) / 1.2));
      wait(FRAME_MS);
    }

    for(int x = 0 ; x <= 5; x++) {
      for (int y = 1; y <= 255; y++) {
        poke(V + 37 + x, y);
        wait(3);
      }
    }

      poke(V +38, 2); poke(V +39, 7); poke (V +41, 6);
      for (int y = 1; y <= 65; y++) {
        poke(V + 40, y);
        poke(V + 37, y + 10);
        wait(FRAME_MS);
      }

      // REM Warten, löschen von Sprite 0 und Ausblendung
      wait(3000);

      for (int x = 0; x <= 32; x++) {
        poke(12832 + x, 0);
        poke(12832 - x, 0);
        wait(100);
      }
      poke(V +21, 0);

  }

  void sidDemo() {
    // Ported from https://www.lemon64.com/manual/manual/8_1.html

    final int[] DATA= {
        25,177,250,28,214,250,
        25,177,250,25,177,250,
        25,177,125,28,214,125,
        32,94,750,25,177,250,
        28,214,250,19,63,250,
        19,63,250,19,63,250,
        21,154,63,24,63,63,
        25,177,250,24,63,125,
        19,63,250,-1,-1,-1
    };

    for (int l = S; l < S + 24; l++) {
      poke(l, 0);
    }
    poke(S + 5, 9);
    poke(S + 6, 0);
    poke( S + 24,15); //  REM SET MAXIMUM VOLUME LEVEL

    int index = 0;
    while (true) {
      int hf = DATA[index++];
      int lf = DATA[index++];
      int dr = DATA[index++];
      if (hf < 0) {
        break;
      }
      poke(S + 1, hf);
      poke(S, lf);
      poke(S + 4,33);

      wait(dr);

      poke (S + 4,32);
      wait(50);
    }

  }



  @Override
  public void onPause() {
    paused = true;
    super.onPause();
  }

  @Override
  public void onResume() {
    paused = false;
    super.onResume();
    queue.add("Wakeup");
  }



}
