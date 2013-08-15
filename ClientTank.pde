import sprites.utils.*;
import sprites.*;

/**
 * A class for a client tank
 */
public class ClientTank
{
  public Sprite tankBase;
  public Sprite tankTurret;
  public double prevX;
  public double prevY;
  public HealthBar health;
  public int lastSeenHealth;
  
  public ClientTank(PApplet applet, float scaleSize)
  {
    tankBase = new Sprite(applet, "Images/BaseDoneFitted.png", 100);
    tankTurret = new Sprite(applet, "Images/LongGunFitted.png", 100);
    tankTurret.setScale(1.2 * scaleSize);
    tankBase.setScale(scaleSize);
    prevX = 0;
    prevY = 0;
    health = new HealthBar((int)(tankBase.getX() - 100 * scalePosition), (int)(tankBase.getY() - 100 * scalePosition));
    lastSeenHealth = 100;
  }
  
  public void drawBase()
  {
    tankBase.draw();
  }
  
  public void drawTurret()
  {
    tankTurret.draw();
  }
  
  public void drawHealth()
  {
    health.draw(lastSeenHealth);
  }
}
