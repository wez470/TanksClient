//A line class for storing lines to draw
class DeadTank
{
  public double x;
  public double y;
  public int index;
  public PImage img;

  public DeadTank(double x, double y, int index)
  {
    this.x = x;
    this.y = y;
    this.index = index;
    img = loadImage("Images/BaseDoneFittedCrushed.png");
  }
  
  public void draw()
  {
    image(img, (float)x, (float)y, img.width * scaleSize, img.height * scaleSize);
  }
}
