//A line class for storing lines to draw
class Line
{
  protected int x1;
  protected int y1;
  protected int x2;
  protected int y2;
  public color lineColor;
  public int opacity;

  public Line(int x1, int y1, int x2, int y2, color lineColor)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.lineColor = lineColor;
    opacity = 255;
  }

  public void drawLine()
  {
    //stroke(40, 150, 30, opacity);
    stroke(lineColor, opacity);
    strokeWeight(3);
    line(x1, y1, x2, y2);
  }
}
