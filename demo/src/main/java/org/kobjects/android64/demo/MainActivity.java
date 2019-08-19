package org.kobjects.android64.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.android64.Android64;
import org.kobjects.android64.Vic;
import org.kobjects.graphics.Screen;

import java.util.concurrent.LinkedBlockingQueue;


/**
 *
 */
public class MainActivity extends Activity implements Runnable {

  static final int V = 53248;
  static final int[] DATA = {
      // REM SPRITE C64-WIKI.DE (einfarbig; Sprite 0)
      239,81,85,139,81,20,137,81,28,137,81,89,137,213,89,142,85,93,138,
      95,85,138,91,85,238,91,85,0,0,0,0,0,0,0,0,0,
      0,199,0,0,231,0,0,164,0,0,180,0,0,151,0,0,180,0,0,164,0,0,231,0,0,199,0,

      // REM Multicolor-Sprite Linie (Sprite 1&2)
      0,255,255,255,170,85,170,170,85,170,85,170,85,85,170,85,255,255,255
  };

  Android64 a64;
  LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
  boolean paused;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // TODO: Shouldn't be needed here -- init sprites to "something else" by default...
    EmojiManager.install(new TwitterEmojiProvider());

    Screen screen = new Screen(this);
    a64 = new Android64(screen);
    screen.view.setBackgroundColor(Vic.ARGB_BLUE);
    setContentView(screen.view);


    new Thread(this).start();
  }


  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(0, 0, 0, "Restart").setOnMenuItemClickListener(x -> {new Thread(this).start(); return true;});
    return true;
  }


    public void run() {
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
        vsync();
      }

    poke(V +16, 0); poke(V, 255);
    int m = peek(V + 4);

    for (int x = 255; x >= 170; x--) {
      poke(V, x);
      z = z + 0.66;
        poke(V + 3, (int) z);
        poke(V + 4, (int) (m + (256 - x) / 1.2));
        vsync();
      }

      for(int x = 0 ; x <= 5; x++) {
        for (int y = 1; y <= 255; y++) {
          poke(V + 37 + x, y);
          vsync();
        }
      }

      poke(V +38, 2); poke(V +39, 7); poke (V +41, 6);
      for (int y = 1; y <= 65; y++) {
        poke(V + 40, y);
        poke(V + 37, y + 10);
        for (z = 0; z <= 15; z++) {
          poke(V + 39, (int) z);
        }
        vsync();
      }

      // REM Warten, löschen von Sprite 0 und Ausblendung
      for (int x = 0; x <= 30; x++){
        vsync();
      }
      for (int x = 0; x <= 32; x++) {
        poke(12832 + x, 0);
        poke(12832 - x, 0);
        vsync();
      }
      poke(V +21, 0);

  }




  private void vsync() {
    if (!paused) {
      queue.add("token");
    }
    try {
      Thread.sleep(1000/60);
      queue.take();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
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


  void poke(int addr, int value) {
    a64.poke(addr, value);
  }

  int peek(int addr) {
    return a64.peek(addr);
  }

}
