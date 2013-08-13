//Thread for processing eyes and face in camera images
class ImageProcessingThread extends Thread
{ 
  void run()
  {
    while(true)
    {
      try
      {
        //wait
        synchronized(this)
        {
          waiting = true;
          this.wait();
        }
        waiting = false;
        //Face Detection
        cam.update(); //get new frame/info from kinect
        opencv.copy(cam.rgbImage()); //get the current frame into opencv
        faceRect = opencv.detect(false); //get rectangle array of faces
        setCurrNumFaces(faceRect.length);
      } catch(Exception e)
      {
        e.printStackTrace();
        exit();
      }
    }
  }
}
