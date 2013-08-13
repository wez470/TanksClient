import processing.core.*; 
import processing.xml.*; 

import com.esotericsoftware.kryonet.*; 
import procontroll.*; 
import net.java.games.input.*; 
import javax.swing.JOptionPane; 
import sprites.utils.*; 
import sprites.*; 
import SimpleOpenNI.*; 
import monclubelec.javacvPro.*; 
import java.awt.*; 
import java.util.concurrent.*; 
import java.util.ArrayList; 
import sprites.utils.*; 
import sprites.*; 
import com.esotericsoftware.kryo.Kryo; 
import com.esotericsoftware.kryonet.*; 
import com.esotericsoftware.kryonet.EndPoint; 

import java.applet.*; 
import java.awt.Dimension; 
import java.awt.Frame; 
import java.awt.event.MouseEvent; 
import java.awt.event.KeyEvent; 
import java.awt.event.FocusEvent; 
import java.awt.Image; 
import java.io.*; 
import java.net.*; 
import java.text.*; 
import java.util.*; 
import java.util.zip.*; 
import java.util.regex.*; 

public class TanksClient extends PApplet {

 //networking

 //gamepad input
 //gamepad input

 //JOptionPane

 //Sprite
 //Sprite

 //Kinect
 //Opencv
 //Rectangle
 //ConcurrentHashMap
 //ArrayList

Client client;
ControllIO controllIO;
ControllDevice controller;
ControllStick turretStick;
ControllStick moveStick;
StopWatch timer;
int backgroundColor = color(213, 189, 122);
ConcurrentHashMap<Integer, Sprite> bullets;
HashMap<Sprite, Integer> bulletIDs;
HashMap<Wall, Integer> wallIDs;
ConcurrentHashMap<Integer, Wall> walls;
float bulletSpeed;
float deltaTime;
float shotTimer = -1000;
int moveTimer = -20;
int rotateTimer = -20;
float scaleSize; 
float scalePosition;
ClientTank[] tanks;
boolean stopped = true;
float prevRot = 1000.0f;
float prevDirection = 1000.0f;
float prevMagnitude = 1000.0f;
int controllerUsedTimer = 0;
SimpleOpenNI cam;
OpenCV opencv; 
ImageProcessingThread imgProcThread;
boolean waiting = true;
Rectangle[] faceRect;
int currNumFaces = 0;
int imgProcIndex = 20;
//ArrayList<Circle> trails = new ArrayList<Circle>();
ArrayList<Line> trails = new ArrayList<Line>();
int trailDiam = 20;
int trailTimer = 0;

/**
 * Setup the game for play
 */
public void setup()
{
  size(800, 600);
  //size((int) (screen.height * 4.0 / 3.0), screen.height);
  
  cam = new SimpleOpenNI(this); //initialize kinect camera
  cam.setMirror(true);
  cam.enableRGB();
  opencv = new OpenCV(this);
  opencv.allocate(cam.rgbWidth(), cam.rgbHeight()); //size of image buffer
  opencv.cascade("C:/opencv/data/haarcascades/", "haarcascade_frontalface_alt_tree.xml"); //initialize detection of face
  imgProcThread = new ImageProcessingThread();
  imgProcThread.setPriority(Thread.MIN_PRIORITY);
  imgProcThread.start();
  
  bullets = new ConcurrentHashMap<Integer, Sprite>();
  bulletIDs = new HashMap<Sprite, Integer>();
  
  scalePosition = height / 600.0f;
  scaleSize = height / 2400.0f;
  bulletSpeed = 300.0f * scalePosition;
  tanks = new ClientTank[4];
  
  timer = new StopWatch();
  
  setupController();
  setupWalls();
  
  client = new Client();
  client.start();
  Network.register(client);
  client.addListener(new Listener()
  {
    public void received(Connection connection, Object object)
    {
      if(object instanceof Network.MoveClientMsg)
      {
        Network.MoveClientMsg moveMsg = (Network.MoveClientMsg) object;
        int playerNum = moveMsg.player;
        if(tanks[playerNum - 1] == null)
        {
          tanks[playerNum - 1] = newTank();
        }
        tanks[playerNum - 1].tankBase.setXY(moveMsg.x * scalePosition, moveMsg.y * scalePosition);
        tanks[playerNum - 1].tankTurret.setXY(moveMsg.x * scalePosition, moveMsg.y * scalePosition);
        tanks[playerNum - 1].tankBase.setRot(moveMsg.baseRot);
        tanks[playerNum - 1].tankTurret.setRot(moveMsg.turretRot);
      }
      else if(object instanceof Network.RotateClientMsg)
      {
        Network.RotateClientMsg rotateMsg = (Network.RotateClientMsg) object;
        int playerNum = rotateMsg.player;
        tanks[playerNum - 1].tankTurret.setRot(rotateMsg.turretRot);
      }
      else if(object instanceof Network.HitBulletMsg || object instanceof Network.HitWallMsg || object instanceof Network.HitTankMsg)
      {
        processCollision(object); 
      }
      else if(object instanceof Network.ShootClientMsg)
      {
        createBullet(object);
      }
    }
  });
  String inputIP = JOptionPane.showInputDialog(this, "Enter the IP address to connect to");
  try
  {
    client.connect(5000, inputIP, Network.TCPPort, Network.UDPPort);
  } 
  catch (IOException e) 
  {
    e.printStackTrace();
  }
}

/**
 * Create a new tank.
 * This method is made for the purpose of accessing "this" (the outer class) from
 * inner classes.
 */
public ClientTank newTank()
{
   return new ClientTank(this, scaleSize);
}

/**
 * Set up the walls for the game
 */
public void setupWalls()
{
  walls = new ConcurrentHashMap<Integer, Wall>();
  wallIDs = new HashMap<Wall, Integer>();
  //walls created top to bottom left to right
  float wallsX[] = {0.77f * width, 0.15f * width, 0.23f * width, 0.31f * width, 0.39f * width, 0.61f * width, 0.69f * width,
                    0.77f * width, 0.85f * width, 0.055f * width, 0.15f * width, 0.23f * width, 0.77f * width, 0.85f * width,
                    0.5f * width, 0.15f * width, 0.23f * width, 0.77f * width, 0.85f * width, 0.945f * width, 0.15f * width,
                    0.23f * width, 0.31f * width, 0.39f * width, 0.61f * width, 0.69f * width, 0.77f * width, 0.85f * width,
                    0.23f * width};
  float wallsY[] = {0.0867f * height, 0.2267f * height, 0.2267f * height, 0.2267f * height, 0.2267f * height, 0.2267f * height,
                    0.2267f * height, 0.2267f * height, 0.2267f * height, 0.3334f * height, 0.3334f * height, 0.3334f * height,
                    0.3334f * height, 0.3334f * height, 0.5f * height, 0.6667f * height, 0.6667f * height, 0.6667f * height,
                    0.6667f * height, 0.6667f * height, 0.773f * height, 0.773f * height, 0.773f * height, 0.773f * height, 
                    0.773f * height, 0.773f * height, 0.773f * height, 0.773f * height, 0.913f * height};
  int numWalls = 29;
  for(int i = 0; i < numWalls; i++)
  {
    Wall wall = new Wall(this, "Images/BlockTiles5Cracked.png", 5, 1, 100);
    wall.setFrame(0);
    wall.setXY(wallsX[i], wallsY[i]);
    wall.setScale(2 * scaleSize);
    walls.put(i + 1, wall);
    wallIDs.put(wall, i + 1);
  }
}

/**
 * Set up controller
 */
public void setupController()
{
  boolean sticks = false;
  boolean sliders = false;
  controllIO = ControllIO.getInstance(this);
  int numDevices = controllIO.getNumberOfDevices();
  //go through all devices and find the first useable controller
  for(int i = 0; i < numDevices; i++)
  {
    int numSticks = controllIO.getDevice(i).getNumberOfSticks();
    int numSliders = controllIO.getDevice(i).getNumberOfSliders();
    if(numSticks == 2)
    {
      //for logitech controllers
      sticks = true;
      controller = controllIO.getDevice(i);
      break;
    }
    else if(numSliders >= 4)
    {
      //for xbox controllers
      sliders = true;
      controller = controllIO.getDevice(i);
      break;
    }
  }
  if(sticks)
  {
    controller.plug(this, "handleRBPress", ControllIO.WHILE_PRESS, 6);
    turretStick = controller.getStick(0);
    moveStick = controller.getStick(1);
  }
  else if(sliders)
  {
    controller.plug(this, "handleRBPress", ControllIO.WHILE_PRESS, 5);
    turretStick = new ControllStick(controller.getSlider(3), controller.getSlider(2));
    moveStick = new ControllStick(controller.getSlider(1), controller.getSlider(0));    
  }
}

/**
 * Function to handle shoot requests
 * triggered by right bumper presses
 */
public void handleRBPress()
{
  controllerUsedTimer = millis();
  if(millis() - shotTimer > 700)
  {
    Network.ShootServerMsg shootMsg = new Network.ShootServerMsg();
    client.sendTCP(shootMsg);
    shotTimer = millis();
  }
}

/**
 * Update and Draw everything in the game
 */
public void draw()
{
  deltaTime = (float) timer.getElapsedTime();
  background(backgroundColor);
  processUserGameInput(deltaTime);
  for(Line currLine: trails)
  {
    currLine.drawLine();
  }
  for(int i = 0; i < 4; i++)
  {
    if(tanks[i] != null)
    {
      tanks[i].drawBase();
    }
  }
  for(Wall currWall: walls.values())
  {
    currWall.draw();
  }
  for(int i = 0; i < 4; i++)
  {
    if(tanks[i] != null)
    {
      tanks[i].drawTurret();
    }
  }
  for(Sprite currBullet: bullets.values())
  {
    currBullet.update(deltaTime);
    currBullet.draw();
  }  
  if(imgProcIndex >= 20 && waiting)
  {
    synchronized(imgProcThread)
    {
      imgProcThread.notify();
    }
    imgProcIndex = 0;
  }
  else
  {
    imgProcIndex++;
  }
  attentionCalculation();
}

/**
 * Method to change what is being drawn depending on if the user is paying attention or not
 */
public void attentionCalculation()
{
  if(getCurrNumFaces() < 1)
  {
    //not looking
    if(millis() - trailTimer >= 0)
    {
      for(int i = 0; i < 4; i++)
      {
        if(tanks[i] != null)
        {
          if(trails.size() < 1)
          {
            tanks[i].prevX = tanks[i].tankBase.getX();
            tanks[i].prevY = tanks[i].tankBase.getY();
          }
          trails.add(new Line((int)tanks[i].prevX, (int)tanks[i].prevY, (int)tanks[i].tankBase.getX(), (int)tanks[i].tankBase.getY(), color(40, 150, 30)));
          tanks[i].prevX = tanks[i].tankBase.getX();
          tanks[i].prevY = tanks[i].tankBase.getY();
        }
      }
      trailTimer = millis();
    }
  }
  else
  {
    //equation for finding how fast to remove old images.  Exponential equation so older images get removed faster
    //Doesn't get below 5 so that the tail will continue to be removed when it gets short 
    //Equation:   trails.size() = (removeNumber ^ 2) / 2
    int removeNumber = max(15, (int) pow(((float) trails.size() * 3.0f), (1.0f / 2.0f)));
    if(millis() - trailTimer > 0 && trails.size() > removeNumber + 1)
    {
      for(int i = 0; i < 4; i++)
      {
        if(tanks[i] != null)
        {
          trails.add(new Line((int)tanks[i].prevX, (int)tanks[i].prevY, (int)tanks[i].tankBase.getX(), (int)tanks[i].tankBase.getY(), color(40, 150, 30)));
          tanks[i].prevX = tanks[i].tankBase.getX();
          tanks[i].prevY = tanks[i].tankBase.getY();
        }
      }
      trailTimer = millis();
    }
    for(int i = 0; i < min(removeNumber, trails.size()); i++)
    {
      trails.get(i).opacity -= 30;
      if(trails.get(i).opacity <= 30)
      {
        trails.remove(i);
      }
    }
  }
}

/**
 * Process user input during gameplay
 * @param deltaTime elapsed time since last frame (seconds)
 */
public void processUserGameInput(float deltaTime) 
{
  //get tank input
  float x = moveStick.getX();
  float y = moveStick.getY();
  
  //tank movement
  if(abs(x) < 0.11f && abs(y) < 0.11f) //control stick is approximately at center
  {
    if(!stopped)
    {
      Network.StopMsg stopMsg = new Network.StopMsg();
      client.sendTCP(stopMsg);
      stopped = true;
    }
  }
  else
  {
    controllerUsedTimer = millis();
    stopped = false;
    float currMagnitude = min(1.0f, sqrt(sq(x) + sq(y)));
    float currDirection = degrees(atan2(y, x));
    if(abs(prevDirection - currDirection) >= 90.0f && millis() - moveTimer > 10)
    {
      Network.MoveServerMsg moveMsg = new Network.MoveServerMsg();
      moveMsg.magnitude = currMagnitude;
      moveMsg.direction = currDirection;
      client.sendTCP(moveMsg);
      prevMagnitude = currMagnitude;
      prevDirection = currDirection;
      moveTimer = millis();
    }
    if((abs(prevMagnitude - currMagnitude) > 0.2f || abs(prevDirection - currDirection) > 3.0f) && millis() - moveTimer > 60)
    {
      Network.MoveServerMsg moveMsg = new Network.MoveServerMsg();
      moveMsg.magnitude = currMagnitude;
      moveMsg.direction = currDirection;
      client.sendUDP(moveMsg);
      prevMagnitude = currMagnitude;
      prevDirection = currDirection;
      moveTimer = millis();
    }
  }

  //get turret input
  x = turretStick.getX();
  y = turretStick.getY();
  
  // Adjust turret direction
  if(abs(x) < 0.6f && abs(y) < 0.6f)
  {
    //don't update if little to no movement registered
  }
  else
  {
    controllerUsedTimer = millis();
    float currRot = degrees(atan2(y, x));
    if(abs(currRot - prevRot) > 2.0f && millis() - rotateTimer > 30)
    {
      Network.RotateServerMsg rotateMsg = new Network.RotateServerMsg();
      rotateMsg.turretRot = currRot;
      client.sendUDP(rotateMsg);
      prevRot = currRot;
      rotateTimer = millis();
    } 
  }
}

/**
 * Method for processing collision messages
 */
public void processCollision(Object object)
{
  if(object instanceof Network.HitBulletMsg)
  {
    Network.HitBulletMsg hitMsg = (Network.HitBulletMsg) object;
    Sprite hitBullet = bullets.get(hitMsg.bulletID);
    bulletIDs.remove(hitBullet);
    bullets.remove(hitMsg.bulletID);
  }
  else if(object instanceof Network.HitWallMsg)
  {
    Network.HitWallMsg hitMsg = (Network.HitWallMsg) object;
    Sprite hitBullet = bullets.get(hitMsg.bulletID);
    Wall hitWall = walls.get(hitMsg.wallID);
    hitWall.hitCount++;
    if(hitWall.hitCount % 2 == 0 && hitWall.hitCount < 10)
    {
      hitWall.setFrame(hitWall.getFrame() + 1);
    }
    if(hitWall.hitCount >= 10)
    {
      wallIDs.remove(hitWall);
      walls.remove(hitMsg.wallID);
    }
    bulletIDs.remove(hitBullet);
    bullets.remove(hitMsg.bulletID);
  }
  else if(object instanceof Network.HitTankMsg)
  {
    Network.HitTankMsg hitMsg = (Network.HitTankMsg) object;
    //tanks[hitMsg.player - 1] = null;
    Sprite hitBullet = bullets.get(hitMsg.bulletID);
    bulletIDs.remove(hitBullet);
    bullets.remove(hitMsg.bulletID);
  }
}

/**
 * Method that creates bullets from a received server message
 * @precond: object is a ShootClientMsg
 */
public void createBullet(Object object)
{
  Network.ShootClientMsg shootMsg = (Network.ShootClientMsg) object;
  Sprite bullet = new Sprite(this, "Images/Bullet.png", 101);
  bullet.setRot(shootMsg.bulletRot);
  bullet.setSpeed(bulletSpeed, shootMsg.heading);
  bullet.setXY(shootMsg.x * scalePosition, shootMsg.y * scalePosition);
  bullet.setScale(scaleSize);
  bullets.put(shootMsg.bulletID, bullet);
  bulletIDs.put(bullet, shootMsg.bulletID);
}

public synchronized int getCurrNumFaces()
{
  return currNumFaces;
}
  
  
public synchronized void setCurrNumFaces(int faces)
{
  currNumFaces = faces;
}

public void exit()
{
  Network.DisconnectMsg disconnectMsg = new Network.DisconnectMsg();
  client.sendTCP(disconnectMsg);
  super.exit();
}
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





/**
 * A class for a client tank
 */
public class ClientTank
{
  public Sprite tankBase;
  public Sprite tankTurret;
  public double prevX;
  public double prevY;
  
  public ClientTank(PApplet applet, float scaleSize)
  {
    tankBase = new Sprite(applet, "Images/BaseDoneFitted.png", 100);
    tankTurret = new Sprite(applet, "Images/LongGunFitted.png", 100);
    tankTurret.setScale(1.2f * scaleSize);
    tankBase.setScale(scaleSize);
    prevX = 0;
    prevY = 0;
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
//Thread for processing eyes and face in camera images
class ImageProcessingThread extends Thread
{ 
  public void run()
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
//A line class for storing lines to draw
class Line
{
  protected int x1;
  protected int y1;
  protected int x2;
  protected int y2;
  public int lineColor;
  public int opacity;

  public Line(int x1, int y1, int x2, int y2, int lineColor)
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




static public class Network 
{
  static public final int TCPPort = 5115;
  static public final int UDPPort = 5116;
  
  static public void register(EndPoint endPoint)
  {
    Kryo kryo = endPoint.getKryo();
    kryo.register(ShootClientMsg.class);
    kryo.register(ShootServerMsg.class);
    kryo.register(MoveClientMsg.class);
    kryo.register(MoveServerMsg.class);
    kryo.register(RotateClientMsg.class);
    kryo.register(RotateServerMsg.class);
    kryo.register(StopMsg.class);
    kryo.register(DisconnectMsg.class);
    kryo.register(HitBulletMsg.class);
    kryo.register(HitTankMsg.class);
    kryo.register(HitWallMsg.class);
  } 
 
  static public class ShootClientMsg
  {
    double x;
    double y;
    double bulletRot;
    int bulletID;
    double heading;
  }
  
  static public class ShootServerMsg
  {
  }
  
  static public class HitBulletMsg
  {
    int bulletID;
  }
  
  static public class HitWallMsg
  {
    int wallID;
    int bulletID;
  }
  
  static public class HitTankMsg
  {
    int player;
    int bulletID;
  }
 
  static public class MoveClientMsg
  {
    int player;
    double x;
    double y;
    double baseRot;
    double turretRot;
  }
  
  static public class MoveServerMsg
  {
    float magnitude;
    float direction;
  }
  
  static public class RotateClientMsg
  {
    int player;
    double turretRot;
  }
  
  static public class RotateServerMsg
  {
    double turretRot;
  }
 
  static public class StopMsg
  {
  }
 
  static public class DisconnectMsg
  {
    int player;
  } 
}


public class Wall extends Sprite
{
  public int hitCount;
  
  public Wall(PApplet applet, String name, int cols, int rows, int zOrder)
  {
    super(applet, name, cols, rows, zOrder);
    hitCount = 0;
  }
}
  static public void main(String args[]) {
    PApplet.main(new String[] { "--bgcolor=#F0F0F0", "TanksClient" });
  }
}
