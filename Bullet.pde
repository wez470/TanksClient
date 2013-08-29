//A bullet class
//TODO make bullet extend sprite
class Bullet
{
  public Sprite bullet;
  public int prevX;
  public int prevY;
  public boolean trail;
  
  public Bullet(Sprite bullet)
  {
    this.bullet = bullet;
    prevX = (int)bullet.getX();
    prevY = (int)bullet.getY();
    trail = false;
  }
}
