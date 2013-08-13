//A circle class for drawing and keeping track of specific circles
class Circle
{
  protected int x;
  protected int y;
  protected int diam;
  protected int opacity;

  public Circle(int startX, int startY, int d)
  {
    x = startX;
    y = startY;
    diam = d;
    opacity = 255;
  }

  public Circle(int startX, int startY, int d, int opac)
  {
    x = startX;
    y = startY;
    diam = d;
    opacity = opac;
  }  

  public void drawCircle()
  {
    stroke(0, 0, 0, opacity);
    fill(16, 92, 1, opacity);
    ellipse(x, y, diam, diam);
  }
}


