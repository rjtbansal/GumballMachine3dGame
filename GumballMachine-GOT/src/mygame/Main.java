package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.audio.AudioNode;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.collision.CollisionResults;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.jme3.scene.shape.Sphere.TextureMode;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.Random;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import java.util.ArrayList;
import java.util.List;

public class Main extends SimpleApplication implements PhysicsCollisionListener {

    public static void main(String args[]) {
        Main app = new Main();
        app.start();

        // Disable the default scene graph statistics
        app.setDisplayStatView(false);
        app.setDisplayFps(false);
    }
    // Prepare the Physics Application State (jBullet)
    private BulletAppState bulletAppState;
    public int count = 0;
    // Prepare Materials
    Material wall_mat;
    Material stone_mat;
    Material floor_mat;
    private Node shootables; //node for all objects
    private Node gumballMachine; //node for Gumball machine parts
    private Spatial gBall; //for gumballmachine and gumballs
    // Prepare HUD text screen
    private BitmapText userInfoScreen; // for showing user's information
    private BitmapText sysStatusInfoScreen; // for showing the system status
    private userInfo userData;
    private ScoreBoard infoCenter;
    private String gameStatus = "";
    /// Prepare geometries and physical nodes for gumballs, floor and cube.
    private RigidBodyControl ball_phy;
    // private static final Cylinder cyl;
    private static final Sphere sphere;
    private RigidBodyControl floor_phy;
    private static final Box floor;
    private RigidBodyControl gumballm_phy;
    private static final Box box;
    private static final Box box2;
    /// dimensions used for bricks and wall
    private static final float brickLength = 5f;
    private static final float brickWidth = 1f;
    private static final float brickHeight = 2f;
    //for audio nodes
    private AudioNode circus_music;
    private AudioNode coin_slot;
    private AudioNode mach_crank;
    private AudioNode ball_rel;
    private AudioNode ball_whoosh;
    //for colors
    private ColorRGBA Red = new ColorRGBA(1, 0, 0, 1);//red
    private ColorRGBA Green = new ColorRGBA(0, 1, 0, 1);//green
    private ColorRGBA Blue = new ColorRGBA(0, 0, 1, 1);//blue
    private static int moneyCollected = 0;
    //for cannonball, brick and floor

    static {
        sphere = new Sphere(32, 32, 0.4f, true, false);
        sphere.setTextureMode(TextureMode.Projected);

        box = new Box(brickLength, brickHeight, brickWidth);
        box.scaleTextureCoordinates(new Vector2f(1f, .5f));
        box2 = new Box(brickWidth, brickHeight, brickLength);
        box2.scaleTextureCoordinates(new Vector2f(1f, .5f));

        floor = new Box(60f, 0.1f, 40f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
    }

    @Override
    public void simpleInitApp() {


        // Set up Physics Game
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        // Configure cam to look at scene
        cam.setLocation(new Vector3f(-1f, 6f, 18f));
        cam.lookAt(new Vector3f(0, 2f, 0), Vector3f.UNIT_Y);
        flyCam.setMoveSpeed(10); //move camera faster

        //create node for all gumballmachine world objects
        shootables = new Node("Shootables");
        rootNode.attachChild(shootables); //attach all world obj to root note


        //create node for Gumball machine (has 3 parts)
        gumballMachine = new Node("GumballMachine");

        /**
         * Add InputManager action: Left click triggers shooting.
         */
        inputManager.addMapping("shoot",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "shoot");

        //run all initialize functions
        initializeWorld();

        //for Gumball machine - get input from user?
        int gBallPrice = 50;
        int initialGBall = 100;

        makeGumballMachine(initialGBall, gBallPrice);
        getPhysicsSpace().addCollisionListener(this);


    }

    public void makePenny() {
        /**
         * Create a cannon ball geometry and attach to scene graph.
         */
        Geometry ball_geo = new Geometry("cannon ball", sphere);
        ball_geo.setMaterial(stone_mat);

        /**
         * Load a model. Uses model and texture from jme3-test-data library!
         */
        Spatial penny = assetManager.loadModel("Models/penny/penny.j3o");
        //penny.scale(1.0f,1.0f,1.0f);
        penny.setName("penny");
        rootNode.attachChild(penny);

        //rootNode.attachChild(ball_geo);
        /**
         * Position the cannon ball
         */
        penny.setLocalTranslation(cam.getLocation());
        /**
         * Make the ball physcial with a mass > 0.0f
         */
        ball_phy = new RigidBodyControl(1.0f);
        /**
         * Add physical ball to physics space.
         */
        penny.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        /**
         * Accelerate the physcial ball to shoot it.
         */
        ball_phy.setLinearVelocity(cam.getDirection().mult(50));
    }

    public void makeDime() {
        /**
         * Create a cannon ball geometry and attach to scene graph.
         */
        Geometry ball_geo = new Geometry("cannon ball", sphere);
        ball_geo.setMaterial(stone_mat);

        /**
         * Load a model. Uses model and texture from jme3-test-data library!
         */
        Spatial nickel = assetManager.loadModel("Models/nickel/nickel.j3o");
        nickel.setName("nickel");
        rootNode.attachChild(nickel);

        //rootNode.attachChild(ball_geo);
        /**
         * Position the cannon ball
         */
        nickel.setLocalTranslation(cam.getLocation());
        /**
         * Make the ball physcial with a mass > 0.0f
         */
        ball_phy = new RigidBodyControl(2f);
        /**
         * Add physical ball to physics space.
         */
        nickel.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        /**
         * Accelerate the physcial ball to shoot it.
         */
        ball_phy.setLinearVelocity(cam.getDirection().mult(50));
    }

    public void makeQuarter() {
        /**
         * Create a cannon ball geometry and attach to scene graph.
         */
        Geometry ball_geo = new Geometry("cannon ball", sphere);
        ball_geo.setMaterial(stone_mat);
        Spatial quarter = assetManager.loadModel("Models/quarter/quarter.j3o");
        //penny.scale(1.0f,1.0f,1.0f);
        quarter.setName("quarter");
        rootNode.attachChild(quarter);
        quarter.setLocalTranslation(cam.getLocation());
        ball_phy = new RigidBodyControl(2f);
        /**
         * Add physical ball to physics space.
         */
        quarter.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);
        /**
         * Accelerate the physcial ball to shoot it.
         */
        ball_phy.setLinearVelocity(cam.getDirection().mult(50));
    }
    // for showing the data of the game

    @Override
    public void simpleUpdate(float tpf) {
        // Display HUD text
        String user_data = infoCenter.observerState;
        userInfoScreen.setText(user_data);
        sysStatusInfoScreen.setText(gameStatus + "\n");

        //update Gumball elapse time, check each Gumball, if > 20s ,detach
        //test shootables.getChildren()
        List<Spatial> sChilren = shootables.getChildren();
        //for Gumball collections
        List<Spatial> gumballs = new ArrayList<Spatial>();
        for (Spatial s : sChilren) {
            //System.out.println(s.getName());
            if (s.getName().equals("gumball")) {
                gumballs.add(s);
            }
        }

        for (Spatial s : gumballs) {

            long time = s.getControl(GumballTimer.class).getElapse();

            if (time > 10000) {
                shootables.detachChild(s);
//                gameStatus = "Gumball not caught within " + time / 1000 + " secs";
//                System.out.println(gameStatus);
            }
        }

    }

    public void initializeWorld() {
        /**
         * Initialize the scene, materials, audio and physics space
         */
        initMaterials();
        initFloor();
        initCrossHairs();
        initKeys();
        initAudio();
        initWelcome();
        //initSysCtrls();
        userInfoScreen = initPlayerInfo();
        sysStatusInfoScreen = initSysStas();
        intiUserInfo();
    }

    protected Spatial makeGumball(String name, Vector3f loc, ColorRGBA color) {

        Spatial gumball = assetManager.loadModel("Models/Gumballs/GM_Red.j3o");
        gumball.setName(name);
        gumball.setLocalTranslation(loc);
        ball_phy = new RigidBodyControl(0.5f);
        gumball.addControl(ball_phy);
        //ball_phy.setLinearVelocity(cam.getDirection().mult(40));
        bulletAppState.getPhysicsSpace().add(ball_phy);
        return gumball;
    }

    protected Spatial makeGumball(String name, float x, float y, float z, ColorRGBA color) {

        Spatial gumball = null;
        if (color == Red) {
            gumball = assetManager.loadModel("Models/Gumballs/GM_Red.j3o");
        } else if (color == Blue) {
            gumball = assetManager.loadModel("Models/Gumballs/GM_Blue.j3o");
        } else if (color == Green) {
            gumball = assetManager.loadModel("Models/Gumballs/GM_Green.j3o");
        }

        gumball.setName(name);
        gumball.setLocalTranslation(x, y, z);
        ball_phy = new RigidBodyControl(1.0f);
        gumball.addControl(ball_phy);
        bulletAppState.getPhysicsSpace().add(ball_phy);


        //Vector3f dir = new Vector3f(0,-2,0);
        //System.out.println(cam.getDirection());
        int g_speed = randInt(40, 60);
        ball_phy.setLinearVelocity(cam.getDirection().mult(g_speed));

        return gumball;
    }

    protected void makeGumballMachine(int initialGBall, int gBallPrice) {
        
        Spatial gm = assetManager.loadModel("Models/GM/GM.j3o");
        gm.setName("Gumball Machine");
        Quaternion roll180 = new Quaternion();
        roll180.fromAngleAxis(FastMath.PI, new Vector3f(0, 1, 0));
        gm.setLocalRotation(roll180);
        gumballMachine.attachChild(gm);
        gumballMachine.setName("Gumball Machine");
        gumballm_phy = new RigidBodyControl(0.0f);
        gumballMachine.addControl(gumballm_phy);
        bulletAppState.getPhysicsSpace().add(gumballm_phy);
        produceFlames();
        shootables.attachChild(gumballMachine);

        gumballMachine.addControl((Control) new GumballMachine());
        gumballMachine.getControl(GumballMachine.class).setCount(initialGBall);
        gumballMachine.getControl(GumballMachine.class).resetAmtInSlot();
        gumballMachine.getControl(GumballMachine.class).setGBallPrice(gBallPrice);
        System.out.println("Gumball price is: " + gBallPrice + " cents");

        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        shootables.addLight(sun);
    }

    //make individual gumballs w/ random color
    private void makeGumballs(int rand_c) {


        switch (rand_c) {
            case 1:
                //ColorRGBA Red = new ColorRGBA(1,0,0,1);//red
                gBall = makeGumball("gumball", 0, 1.5f, 7, Red);
                gBall.addControl((Control) new GumballTimer(new Gumball())); //apply decorator
                gBall.getControl(GumballTimer.class).setColor("red");
                gBall.getControl(GumballTimer.class).setValue(150);
                //shootables.attachChild(gBall);
                break;
            case 2:
                //ColorRGBA Green = new ColorRGBA(0,1,0,1);//green
                gBall = makeGumball("gumball", 0, 1.5f, 7, Green);
                gBall.addControl((Control) new GumballTimer(new Gumball()));//apply decorator
                gBall.getControl(GumballTimer.class).setColor("green");
                gBall.getControl(GumballTimer.class).setValue(50);
                //shootables.attachChild(gBall);
                break;
            case 3:
                //ColorRGBA Blue = new ColorRGBA(0,0,1,1);//blue
                gBall = makeGumball("gumball", 0, 1.5f, 7, Blue);
                gBall.addControl((Control) new GumballTimer(new Gumball()));//apply decorator
                gBall.getControl(GumballTimer.class).setColor("blue");
                gBall.getControl(GumballTimer.class).setValue(100);
                //shootables.attachChild(gBall);
                break;
        }

        shootables.attachChild(gBall);


    }

    public static int randInt(int min, int max) {
        //inclusive of min and max
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;
        return randomNum;
    }

   
    //initialize audio nodes
    private void initAudio() {
        circus_music = new AudioNode(assetManager, "Sounds/GameOfThrones.ogg");
        circus_music.setLooping(true);
        circus_music.setPositional(false);
        circus_music.setVolume(1);
        rootNode.attachChild(circus_music);
        circus_music.play();

        coin_slot = new AudioNode(assetManager, "Sounds/CoinSlot.ogg");
        coin_slot.setPositional(false);
        coin_slot.setLooping(false);
        coin_slot.setVolume(2);
        rootNode.attachChild(coin_slot);

        mach_crank = new AudioNode(assetManager, "Sounds/Crank.ogg");
        mach_crank.setPositional(false);
        mach_crank.setLooping(false);
        mach_crank.setVolume(2);
        rootNode.attachChild(mach_crank);

        ball_rel = new AudioNode(assetManager, "Sounds/Ball.ogg");
        ball_rel.setPositional(false);
        ball_rel.setLooping(false);
        ball_rel.setVolume(2);
        rootNode.attachChild(ball_rel);

        ball_whoosh = new AudioNode(assetManager, "Sounds/Whoosh.ogg");
        ball_whoosh.setPositional(false);
        ball_whoosh.setLooping(false);
        ball_whoosh.setVolume(2);
        rootNode.attachChild(ball_whoosh);
    }

    /**
     * Initialize the materials used in this scene.
     */
    public void initMaterials() {

        stone_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stone_mat.setTexture("ColorMap", tex2);

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        //floor_mat.setColor("Color", ColorRGBA.Gray);
        TextureKey key3 = new TextureKey("Textures/Terrain/Rocky/RockyTexture.jpg");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floor_mat.setTexture("ColorMap", tex3);

        assetManager.registerLocator("town.zip", ZipLocator.class);
        Spatial gameLevel = assetManager.loadModel("main.scene");
        gameLevel.setLocalTranslation(0, -5.2f, 0);
        gameLevel.setLocalScale(2);
        rootNode.attachChild(gameLevel);

    }

    /**
     * Make a solid floor and add it to the scene.
     */
    public void initFloor() {

        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);
        floor_geo.setLocalTranslation(0, -0.1f, 0);
        this.rootNode.attachChild(floor_geo);
        /* Make the floor physical with mass 0.0f! */
        floor_phy = new RigidBodyControl(0.0f);
        floor_geo.addControl(floor_phy);
        bulletAppState.getPhysicsSpace().add(floor_phy);
    }

    /**
     * A plus sign used as crosshairs to help the player with aiming.
     */
    protected void initCrossHairs() {
        guiNode.detachAllChildren();
        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText ch = new BitmapText(guiFont, false);
        ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
        ch.setText("+");        // fake crosshairs :)
        ch.setLocalTranslation( // center
                settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
                settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
        guiNode.attachChild(ch);
    }

    /**
     * Declaring the "Shoot" action and mapping to its triggers.
     */
    private void initKeys() {
        inputManager.addMapping("Shoot",
                new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(actionListener, "Shoot");

        inputManager.addMapping("Click",
                new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
        inputManager.addListener(actionListener, "Click");

        inputManager.addMapping("Refill",
                new KeyTrigger(KeyInput.KEY_R));//R button is trigger for refill action
        inputManager.addListener(actionListener, "Refill");
    }

    /**
     * Display a Greeting Title on top of the game screen
     */
    protected void initWelcome() {
        guiFont = assetManager.loadFont("Interface/Fonts/Verdana.fnt");
        BitmapText hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()); // font size
        hudText.setColor(ColorRGBA.White);  // font color
        hudText.setLocalTranslation(570, 575, 0); // position
        guiNode.attachChild(hudText);
        guiNode.setQueueBucket(Bucket.Gui);

    }

    /*Display game controls*/
    protected void initSysCtrls() {
        guiFont = assetManager.loadFont("Interface/Fonts/Verdana.fnt");
        BitmapText hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()); // font size
        hudText.setColor(ColorRGBA.White);  // font color
        hudText.setText("Controls:\nClick (grab)\nSpace (shoot)\nR (refill)"); // the text
        hudText.setLocalTranslation(660, 120, 0); // position
        guiNode.attachChild(hudText);
        guiNode.setQueueBucket(Bucket.Gui);

    }

    /*Display the game status*/
    protected BitmapText initSysStas() {
        guiFont = assetManager.loadFont("Interface/Fonts/Verdana.fnt");
        BitmapText hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()); // font size
        hudText.setColor(ColorRGBA.Yellow);  // font color
        hudText.setLocalTranslation(500, 700, 0); // position
        guiNode.attachChild(hudText);
        guiNode.setQueueBucket(Bucket.Gui);
        return hudText;
    }

    /*Display the player's information*/
    protected BitmapText initPlayerInfo() {
        guiFont = assetManager.loadFont("Interface/Fonts/Verdana.fnt");
        BitmapText hudText = new BitmapText(guiFont, false);
        hudText.setSize(guiFont.getCharSet().getRenderedSize()); // font size
        hudText.setColor(ColorRGBA.White);  // font color
        hudText.setLocalTranslation(hudText.getLineWidth(), 720 + hudText.getLineHeight(), 0); // position
        guiNode.attachChild(hudText);
        guiNode.setQueueBucket(Bucket.Gui);
        return hudText;

    }

    private void intiUserInfo() {
        userData = new userInfo();
        infoCenter = new ScoreBoard(userData);
        userData.attach(infoCenter);
        userData.setState("score", 0);
        userData.setState("numOfGumballs", 0);
        userData.setState("coinAmt", moneyCollected);
        // coinAmt will be initialized in makeCoins function
    }

    private PhysicsSpace getPhysicsSpace() {
        return bulletAppState.getPhysicsSpace();
    }
    private ActionListener actionListener = new ActionListener() {
        int g_color;
        boolean taken_gball = false;
        int gBalls = 0;

        public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Click") && !keyPressed) {
                CollisionResults results = new CollisionResults();
                Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                shootables.collideWith(ray, results);

                if (results.size() > 0) {//not missed
                    //String hit = results.getCollision(0).getGeometry().getName();
                    //System.out.println("  You hit " + hit);
                    gBall = results.getCollision(0).getGeometry();
                    gBall.addControl((Control) new Gumball());

                    Spatial s = results.getCollision(0).getGeometry();
                    Spatial p = s.getParent();
                    s = p.getParent();
                    //System.out.println("You hit " + s.getName());
                    if ("Gumball Machine".equals(s.getName())) {
                        System.out.println(gumballMachine.getControl(GumballMachine.class).getState());
                        gameStatus = gumballMachine.getControl(GumballMachine.class).turnCrank();
                        moneyCollected = 0;
                        userData.setState("coinAmt", moneyCollected);
                        //System.out.println(gameStatus); 
                        //gumballMachine.getControl(GumballMachine.class).turnCrank();
                        mach_crank.playInstance();
                        //to delay release of Gumball until after audio finishes
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        if (gumballMachine.getControl(GumballMachine.class).makeGumball()) {
                            g_color = randInt(1, 3);
                            makeGumballs(g_color);//random # btwn 1-3 for color
                            ball_rel.playInstance();
                            gumballMachine.getControl(GumballMachine.class).resetAmtInSlot();
                            gameStatus = "Catch gumball to earn points.";
                        }
                    } else if ("gumball".equals(s.getName())) {
                        String status = s.getUserData("color")
                                + " gumball : ";
                        int gumball_score = s.getUserData("value");
                        //System.out.println(gBall);
                        System.out.println(status + gumball_score + " points");
                        System.out.println(gBall.getControl(Gumball.class).getState());
                        System.out.println(gBall.getControl(Gumball.class).catchIt()); //change state
                        System.out.println(gBall.getControl(Gumball.class).getState());                        

                        // change the Hud text in the playing screen
                        gameStatus = status + gumball_score + " points";
                        int origScore = userData.getState().get("score");
                        userData.setState("score", origScore + gumball_score);

                        //remove Gumball from scene
                        bulletAppState.getPhysicsSpace().remove(ball_phy);
                        s.removeFromParent();
                        shootables.detachChild(s);
                        System.out.print("You can now shoot a ");
                        System.out.print(s.getUserData("color"));
                        System.out.println(" gumball!");
                        taken_gball = true;
                        gBalls++;
                        //System.out.println("You have " + gBalls + " Gumball(s)!");
                        // Show number of gumballs in HUD screen
                        int origGumballs = userData.getState().get("numOfGumballs");
                        userData.setState("numOfGumballs", origGumballs + 1);
                    }
                }//end hit
            }//end Click
            else if (name.equals("Refill") && !keyPressed) {
                gumballMachine.getControl(GumballMachine.class).refill(5);
                gameStatus = "Refilling machine... +5 gumballs";
                //default refill by 5 gumballs
            }//end Refill
            else if (name.equals("Shoot") && !keyPressed) {
                int it = randInt(1, 3);
                count = 0;
                //int it = 1;
                if (it == 1) {
                    makePenny();
                } else if (it == 2) {
                    makeDime();
                } else if (it == 3) {
                    makeQuarter();
                }
            }//end Shoot
        }//end onAction
    };//end ActionListener

    public void collision(PhysicsCollisionEvent event) {

        if ("penny".equals(event.getNodeA().getName()) || "penny".equals(event.getNodeB().getName()) || "nickel".equals(event.getNodeA().getName()) || "nickel".equals(event.getNodeB().getName()) || "quarter".equals(event.getNodeA().getName()) || "quarter".equals(event.getNodeB().getName())) {
            if ("Gumball Machine".equals(event.getNodeA().getName()) || "Gumball Machine".equals(event.getNodeB().getName())) {
                count++;
                if (count == 1) {
                    if (event.getNodeA().getName().equalsIgnoreCase("penny")) {
                        moneyCollected = moneyCollected + 1;
                        userData.setState("coinAmt", moneyCollected);
                        gumballMachine.getControl(GumballMachine.class).acceptCoin(1);
                        gameStatus = "Penny Inserted. " + gumballMachine.getControl(GumballMachine.class).getPayment()
                                + " cent(s) inserted";
                        System.out.println(gameStatus);
                    } else if (event.getNodeA().getName().equalsIgnoreCase("nickel")) {
                        moneyCollected = moneyCollected + 5;
                        userData.setState("coinAmt", moneyCollected);
                        gumballMachine.getControl(GumballMachine.class).acceptCoin(5);

                        gameStatus = "Nickel Inserted. " + gumballMachine.getControl(GumballMachine.class).getPayment()
                                + " cent(s) inserted";
                        System.out.println(gameStatus);
                    } else if (event.getNodeA().getName().equalsIgnoreCase("Quarter")) {
                        moneyCollected = moneyCollected + 25;
                        userData.setState("coinAmt", moneyCollected);
                        gumballMachine.getControl(GumballMachine.class).acceptCoin(25);
                        gameStatus = "Quarter Inserted. " + gumballMachine.getControl(GumballMachine.class).getPayment()
                                + " cent(s) inserted";
                        System.out.println(gameStatus);
                    }
                    if (event.getNodeA().getParent() != null) {

                        bulletAppState.getPhysicsSpace().remove(event.getNodeA());
                        Node p = event.getNodeA().getParent();
                        p.detachChild(event.getNodeA());
                    }
                    mach_crank.playInstance();
                    System.out.println("Money in Gumball Machine: " + moneyCollected + " cents");
                }
            }
        }
    }
    
    private void produceFlames() {
        
        Node flames2 = new Node("Flames");
        rootNode.attachChild(flames2);
        flames2.move(-6.0f, 0,-4.0f);
        ParticleEmitter fireEffect2 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat2 = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect2.setMaterial(fireMat2);
        fireEffect2.setImagesX(4);
        fireEffect2.setImagesY(4); // 2x2 texture animation
        fireEffect2.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fireEffect2.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireEffect2.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect2.setStartSize(0.6f);
        fireEffect2.setEndSize(0.1f);
        fireEffect2.setGravity(0f, 0f, 0f);
        fireEffect2.setLowLife(0.5f);
        fireEffect2.setHighLife(3f);
        fireEffect2.getParticleInfluencer().setVelocityVariation(0.3f);
        flames2.attachChild(fireEffect2);
        shootables.attachChild(flames2);
        
        Node flames3 = new Node("Flames");
        rootNode.attachChild(flames3);
        flames3.move(6.0f, 0,-5.0f);
        ParticleEmitter fireEffect3 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat3 = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect3.setMaterial(fireMat3);
        fireEffect3.setImagesX(4);
        fireEffect3.setImagesY(4); // 2x2 texture animation
        fireEffect3.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fireEffect3.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireEffect3.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect3.setStartSize(0.6f);
        fireEffect3.setEndSize(0.1f);
        fireEffect3.setGravity(0f, 0f, 0f);
        fireEffect3.setLowLife(0.5f);
        fireEffect3.setHighLife(3f);
        fireEffect3.getParticleInfluencer().setVelocityVariation(0.3f);
        flames3.attachChild(fireEffect3);
        shootables.attachChild(flames3);
        
        Node flames4 = new Node("Flames");
        rootNode.attachChild(flames4);
        flames4.move(-12.0f, 0,-12.0f);
        ParticleEmitter fireEffect4 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat4 = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect4.setMaterial(fireMat4);
        fireEffect4.setImagesX(4);
        fireEffect4.setImagesY(4); // 2x2 texture animation
        fireEffect4.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fireEffect4.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireEffect4.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect4.setStartSize(0.6f);
        fireEffect4.setEndSize(0.1f);
        fireEffect4.setGravity(0f, 0f, 0f);
        fireEffect4.setLowLife(0.5f);
        fireEffect4.setHighLife(3f);
        fireEffect4.getParticleInfluencer().setVelocityVariation(0.3f);
        flames4.attachChild(fireEffect4);
        shootables.attachChild(flames4);
        
        Node flames5 = new Node("Flames");
        rootNode.attachChild(flames5);
        flames5.move(12.0f, 0,-14.0f);
        ParticleEmitter fireEffect5 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat5 = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect5.setMaterial(fireMat5);
        fireEffect5.setImagesX(4);
        fireEffect5.setImagesY(4); // 2x2 texture animation
        fireEffect5.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fireEffect5.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireEffect5.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect5.setStartSize(0.6f);
        fireEffect5.setEndSize(0.1f);
        fireEffect5.setGravity(0f, 0f, 0f);
        fireEffect5.setLowLife(0.5f);
        fireEffect5.setHighLife(3f);
        fireEffect5.getParticleInfluencer().setVelocityVariation(0.3f);
        flames5.attachChild(fireEffect5);
        shootables.attachChild(flames5);
        
        Node flames6 = new Node("Flames");
        rootNode.attachChild(flames6);
        flames6.move(-18.0f, 0,-18.0f);
        ParticleEmitter fireEffect6 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat6 = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect6.setMaterial(fireMat6);
        fireEffect6.setImagesX(4);
        fireEffect6.setImagesY(4); // 2x2 texture animation
        fireEffect6.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fireEffect6.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireEffect6.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect6.setStartSize(0.6f);
        fireEffect6.setEndSize(0.1f);
        fireEffect6.setGravity(0f, 0f, 0f);
        fireEffect6.setLowLife(0.5f);
        fireEffect6.setHighLife(3f);
        fireEffect6.getParticleInfluencer().setVelocityVariation(0.3f);
        flames6.attachChild(fireEffect6);
        shootables.attachChild(flames6);
        
        Node flames7 = new Node("Flames");
        rootNode.attachChild(flames7);
        flames7.move(18.0f, 0,-18.0f);
        ParticleEmitter fireEffect7 = new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
        Material fireMat7 = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        //fireMat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        fireEffect7.setMaterial(fireMat7);
        fireEffect7.setImagesX(4);
        fireEffect7.setImagesY(4); // 2x2 texture animation
        fireEffect7.setEndColor(new ColorRGBA(1f, 0f, 0f, 1f));   // red
        fireEffect7.setStartColor(new ColorRGBA(1f, 1f, 0f, 0.5f)); // yellow
        fireEffect7.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
        fireEffect7.setStartSize(0.6f);
        fireEffect7.setEndSize(0.1f);
        fireEffect7.setGravity(0f, 0f, 0f);
        fireEffect7.setLowLife(0.5f);
        fireEffect7.setHighLife(3f);
        fireEffect7.getParticleInfluencer().setVelocityVariation(0.3f);
        flames7.attachChild(fireEffect7);
        shootables.attachChild(flames7);
//To change body of generated methods, choose Tools | Templates.
    }
}//end Main class
