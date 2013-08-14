//A line class for storing lines to draw
class Line
{
  public int x1;
  public int y1;
  public int x2;
  public int y2;
  public color lineColor;
  public int opacity;
  public int strokeWidth;

  public Line(int x1, int y1, int x2, int y2, color lineColor, int strokeWidth)
  {
    this.x1 = x1;
    this.y1 = y1;
    this.x2 = x2;
    this.y2 = y2;
    this.lineColor = lineColor;
    this.strokeWidth = strokeWidth;
    opacity = 255;
  }

  public void draw()
  {
    stroke(lineColor, opacity);
    strokeWeight(strokeWidth);
    line(x1, y1, x2, y2);
  }
}
