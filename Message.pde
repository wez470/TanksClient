//A line class for storing lines to draw
class Message
{
  public float timeVisible;
  public String message;
  public int opacity;
  public boolean remove;
  public float visibleLimit = 7000.0;

  public Message(String m)
  {
    message = m;
    timeVisible = millis();
    opacity = 200;
    remove = false;
  }

/**
 * Method to draw messages from players in tank game
 * @param i: the index of the message being sent. Used for
 *           positioning the messages properly.
 */
  public void draw(int i)
  {
    if(millis() - timeVisible >= visibleLimit)
    {
      opacity = (int)(200.0 * (1 - ((millis() - (timeVisible + visibleLimit)) / 1000.0)));
    }
    if(opacity <= 0)
    {
      remove = true;
    }
    fill(0, 0, 0, opacity);
    textAlign(RIGHT);
    //draw text at a height specified by its order in the list of messages
    text(message, width - 20 * scaleSize, height - 20 * scaleSize - 80 * i * scaleSize);
  }
}
