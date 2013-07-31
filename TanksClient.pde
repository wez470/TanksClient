import processing.net.*;

import procontroll.*;
import net.java.games.input.*;

import javax.swing.JOptionPane;

import sprites.utils.*;
import sprites.*;

import java.util.concurrent.*;

Client client;
ControllIO controllIO;
ControllDevice controller;
ControllStick turretStick;
ControllStick moveStick;
StopWatch timer;
HashMap<Integer, Sprite> bullets;
HashMap<Sprite, Integer> bulletIDs;
HashMap<Wall, Integer> wallIDs;
HashMap<Integer, Wall> walls;
float bulletSpeed;
float deltaTime;
float shotTimer = -1000;
int moveTimer = -20;
int rotateTimer = -20;
float scaleSize; 
float scalePosition;
ClientTank[] tanks;
boolean stopped = true;
float prevRot = 1000.0;
float prevMove = 1000.0;
float prevMagnitude = 1000.0;
int messagesReceivedTimer = 0;
int messagesReceived = 0;
int messagesReceivedPrint = 0;
int clearTimer = 0;


/**
 * Setup the game for play
 */
void setup()
{
  size(800, 600);
  //size((int) (screen.height * 4.0 / 3.0), screen.height);
  
  String inputIP = JOptionPane.showInputDialog(this, "Enter the IP address to connect to");
  client = new Client(this, inputIP, 5115);
  //client = new Client(this, "10.81.6.29", 5115);
  //client = new Client(this, "192.168.0.11", 5115);
  
  bullets = new HashMap<Integer, Sprite>();
  bulletIDs = new HashMap<Sprite, Integer>();
  
  scalePosition = height / 600.0;
  scaleSize = height / 2400.0;
  bulletSpeed = 300.0 * scalePosition;
  tanks = new ClientTank[4];
  
  timer = new StopWatch();
  
  setupController();
  setupWalls();
}

/**
 * Set up the walls for the game
 */
void setupWalls()
{
  walls = new HashMap<Integer, Wall>();
  wallIDs = new HashMap<Wall, Integer>();
  //walls created top to bottom left to right
  float wallsX[] = {0.77 * width, 0.15 * width, 0.23 * width, 0.31 * width, 0.39 * width, 0.61 * width, 0.69 * width,
                    0.77 * width, 0.85 * width, 0.055 * width, 0.15 * width, 0.23 * width, 0.77 * width, 0.85 * width,
                    0.5 * width, 0.15 * width, 0.23 * width, 0.77 * width, 0.85 * width, 0.945 * width, 0.15 * width,
                    0.23 * width, 0.31 * width, 0.39 * width, 0.61 * width, 0.69 * width, 0.77 * width, 0.85 * width,
                    0.23 * width};
  float wallsY[] = {0.0867 * height, 0.2267 * height, 0.2267 * height, 0.2267 * height, 0.2267 * height, 0.2267 * height,
                    0.2267 * height, 0.2267 * height, 0.2267 * height, 0.3334 * height, 0.3334 * height, 0.3334 * height,
                    0.3334 * height, 0.3334 * height, 0.5 * height, 0.6667 * height, 0.6667 * height, 0.6667 * height,
                    0.6667 * height, 0.6667 * height, 0.773 * height, 0.773 * height, 0.773 * height, 0.773 * height, 
                    0.773 * height, 0.773 * height, 0.773 * height, 0.773 * height, 0.913 * height};
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
void setupController()
{
  controllIO = ControllIO.getInstance(this);
  int numDevices = controllIO.getNumberOfDevices();
  //go through all devices and find the first useable controller
  for(int i = 0; i < numDevices; i++)
  {
    int numSticks = controllIO.getDevice(i).getNumberOfSticks();
    if(numSticks == 2)
    {
      controller = controllIO.getDevice(i);
      break;
    }
  }
  controller.plug(this, "handleRBPress", ControllIO.WHILE_PRESS, 6);
  turretStick = controller.getStick(0);
  moveStick = controller.getStick(1);
//  boolean caught = false;
//  try
//  {
//    controller = controllIO.getDevice("Controller (Xbox 360 Wireless Receiver for Windows)");
//  }
//  catch (Exception e)
//  {
//    caught = true;
//  }
//  if(caught)
//  {
//    controller = controllIO.getDevice("Controller (XBOX 360 For Windows)");
//  }
//  controller.plug(this, "handleRBPress", ControllIO.WHILE_PRESS, 5);
//  turretStick = new ControllStick(controller.getSlider(3), controller.getSlider(2));
//  moveStick = new ControllStick(controller.getSlider(1), controller.getSlider(0));
}

/**
 * Function to handle shoot requests
 * triggered by right bumper presses
 */
void handleRBPress()
{
  if(millis() - shotTimer > 700)
  {
    client.write("shoot,*");
    shotTimer = millis();
  }
}

/**
 * Update and Draw everything in the game
 */
void draw()
{
  deltaTime = (float) timer.getElapsedTime();
  //Get messages from server if available
  if(client.available() > 0)
  {
    receiveMessage();
    messagesReceived++;
  }
  
  background(213, 189, 122);
  
  processUserGameInput(deltaTime);
  
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
  if(millis() - messagesReceivedTimer > 1000)
  {
     messagesReceivedTimer = millis();
     textSize(32);
     //text(messagesReceived, 10, 10);
     messagesReceivedPrint = messagesReceived;
     messagesReceived = 0; 
  }
  text(messagesReceivedPrint, 10, 50);
}

void receiveMessage()
{
  String currString = client.readStringUntil('*');
  if(currString != null)
  {
    String[] currMessage = currString.split(",");
    if(currMessage[0].equals("move"))
    {
      int playerNum = int(currMessage[1]);
      if(tanks[playerNum - 1] == null)
      {
        tanks[playerNum - 1] = new ClientTank(this, scaleSize);
      }
      tanks[playerNum - 1].tankBase.setXY(float(currMessage[2]) * scalePosition, float(currMessage[3]) * scalePosition);
      tanks[playerNum - 1].tankTurret.setXY(float(currMessage[2]) * scalePosition, float(currMessage[3]) * scalePosition);
      tanks[playerNum - 1].tankBase.setRot(float(currMessage[4]));
      tanks[playerNum - 1].tankTurret.setRot(float(currMessage[5]));
    }
    else if(currMessage[0].equals("rotate"))
    {
      int playerNum = int(currMessage[1]);
      tanks[playerNum - 1].tankTurret.setRot(float(currMessage[2]));
    }
    else if(currMessage[0].equals("hit"))
    {
      processCollision(currMessage); 
    }
    else if(currMessage[0].equals("shoot"))
    {
      createBullet(currMessage);
    }
  }
}

/**
 * Process user input during gameplay
 * @param deltaTime elapsed time since last frame (seconds)
 */
void processUserGameInput(float deltaTime) 
{
  //get tank input
  float x = moveStick.getX();
  float y = moveStick.getY();
  
  //tank movement
  if(abs(x) < 0.11 && abs(y) < 0.11) //control stick is approximately at center
  {
    if(!stopped)
    {
      client.write("stop,*");
      stopped = true;
    }
  }
  else
  {
    stopped = false;
    float currMagnitude = min(1.0, sqrt(sq(x) + sq(y)));
    float currMove = degrees(atan2(y, x));
    if(abs(prevMove - currMove) >= 90.0 && millis() - moveTimer > 15)
    {
      client.write("move," + currMagnitude + "," + currMove + ",*");
      prevMagnitude = currMagnitude;
      prevMove = currMove;
      moveTimer = millis();
    }
    if((abs(prevMagnitude - currMagnitude) > 0.2 || abs(prevMove - currMove) > 3.0) && millis() - moveTimer > 80)
    {
      client.write("move," + currMagnitude + "," + currMove + ",*");
      prevMagnitude = currMagnitude;
      prevMove = currMove;
      moveTimer = millis();
    }
  }

  //get turret input
  x = turretStick.getX();
  y = turretStick.getY();
  
  // Adjust turret direction
  if(abs(x) < 0.6 && abs(y) < 0.6)
  {
    //don't update if little to no movement registered
  }
  else
  {
    float currRot = degrees(atan2(y, x));
    if(abs(currRot - prevRot) > 2.0 && millis() - rotateTimer > 40)
    {
      client.write("rotate," + currRot + ",*");
      prevRot = currRot;
      rotateTimer = millis();
    } 
  }
}

/**
 * Method for processing collision messages
 */
void processCollision(String[] message)
{
  if(message[1].equals("bullet"))
  {
    int hitID = int(message[2]);
    Sprite hitBullet = bullets.get(hitID);
    bulletIDs.remove(hitBullet);
    bullets.remove(hitID);
  }
  else if(message[1].equals("wall"))
  {
    int hitWallID = int(message[2]);
    int hitBulletID = int(message[3]);
    Sprite hitBullet = bullets.get(hitBulletID);
    Wall hitWall = walls.get(hitWallID);
    hitWall.hitCount++;
    if(hitWall.hitCount % 2 == 0 && hitWall.hitCount < 10)
    {
      hitWall.setFrame(hitWall.getFrame() + 1);
    }
    if(hitWall.hitCount >= 10)
    {
      wallIDs.remove(hitWall);
      walls.remove(hitWallID);
    }
    bulletIDs.remove(hitBullet);
    bullets.remove(hitBulletID);
  }
  else if(message[1].equals("tank"))
  {
    //tanks[int(message[2])] = null;
    int hitBulletID = int(message[3]);
    Sprite hitBullet = bullets.get(hitBulletID);
    bulletIDs.remove(hitBullet);
    bullets.remove(hitBulletID);
  }
}

/**
 * Method that creates bullets from received server messages
 */
void createBullet(String[] currMessage)
{
  Sprite bullet = new Sprite(this, "Images/Bullet.png", 101);
  bullet.setRot(float(currMessage[1]));
  bullet.setSpeed(bulletSpeed, float(currMessage[5]));
  bullet.setXY(float(currMessage[2]) * scalePosition, float(currMessage[3]) * scalePosition);
  bullet.setScale(scaleSize);
  int bulletID = int(currMessage[4]);
  bullets.put(bulletID, bullet);
  bulletIDs.put(bullet, bulletID);
}

void exit()
{
  client.write("disconnect,*");
  super.exit();
}
