import sprites.utils.*;
import sprites.*;

/**
 * A class for a client tank
 */
public class ClientTank
{
  public Sprite tankBase;
  public Sprite tankTurret;
  
  public ClientTank(PApplet applet, float scaleSize)
  {
    tankBase = new Sprite(applet, "Images/BaseDoneFitted.png", 100);
    tankTurret = new Sprite(applet, "Images/LongGunFitted.png", 100);
    tankTurret.setScale(1.2 * scaleSize);
    tankBase.setScale(scaleSize);
  }
  
  public void drawBase()
  {
    tankBase.draw();
  }
  
  public void drawTurret()
  {
    tankTurret.draw();
  }
}
