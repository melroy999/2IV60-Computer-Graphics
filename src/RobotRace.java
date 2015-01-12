/**
 * RobotRace
 */
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL;
import static javax.media.opengl.GL2.*;
import javax.media.opengl.GL2;
import robotrace.Base;
import robotrace.Texture1D;
import robotrace.Vector;

/**
 * Handles all of the RobotRace graphics functionality, which should be extended
 * per the assignment.
 *
 * OpenGL functionality: - Basic commands are called via the gl object; -
 * Utility commands are called via the glu and glut objects;
 *
 * GlobalState: The gs object contains the GlobalState as described in the
 * assignment: - The camera viewpoint angles, phi and theta, are changed
 * interactively by holding the left mouse button and dragging; - The camera
 * view width, vWidth, is changed interactively by holding the right mouse
 * button and dragging upwards or downwards; - The center point can be moved up
 * and down by pressing the 'q' and 'z' keys, forwards and backwards with the
 * 'w' and 's' keys, and left and right with the 'a' and 'd' keys; - Other
 * settings are changed via the menus at the top of the screen.
 *
 * Textures: Place your "track.jpg", "brick.jpg", "head.jpg", and "torso.jpg"
 * files in the same folder as this file. These will then be loaded as the
 * texture objects track, bricks, head, and torso respectively. Be aware, these
 * objects are already defined and cannot be used for other purposes. The
 * texture objects can be used as follows:
 *
 * gl.glColor3f(1f, 1f, 1f); track.bind(gl); gl.glBegin(GL_QUADS);
 * gl.glTexCoord2d(0, 0); gl.glVertex3d(0, 0, 0); gl.glTexCoord2d(1, 0);
 * gl.glVertex3d(1, 0, 0); gl.glTexCoord2d(1, 1); gl.glVertex3d(1, 1, 0);
 * gl.glTexCoord2d(0, 1); gl.glVertex3d(0, 1, 0); gl.glEnd();
 *
 * Note that it is hard or impossible to texture objects drawn with GLUT. Either
 * define the primitives of the object yourself (as seen above) or add
 * additional textured primitives to the GLUT object.
 */
public class RobotRace extends Base {
    /**
     * Array of the four robots.
     */
    private final Robot[] robots;
    /**
     * Instance of the camera.
     */
    private final Camera mainCamera;
    private final Camera screenCamera;

    /**
     * Currentyl active camera
     */
    private Camera camera;

    /**
     * Instance of the race track.
     */
    private final RaceTrack raceTrack;

    /**
     * Instance of the terrain.
     */
    private final Terrain terrain;

    /**
     * Constructs this robot race by initializing robots, camera, track, and
     * terrain.
     */
    public RobotRace() {

        // Create a new array of four robots
        robots = new Robot[] {
            /// Instantiate swag robot
            new Robot(Material.GOLD,4).setNeckModifier(2.f).setSpeed(50f),

            // Instantiate bender, kiss my shiny metal ass
            new Robot(Material.SILVER,3).setSpeed(56f),

            // Instantiate oldschool robot
            new Robot(Material.WOOD,2).setNeckModifier(0.5f).setSpeed(51f),

            // Hey look at me, I'm an annoying orange robot!
            new Robot(Material.ORANGE,1).setSpeed(53f)
        };

        // Initialize the cameras
        mainCamera = new Camera();
        camera = mainCamera;

        screenCamera = new Camera();

        // Initialize the race track
        raceTrack = new RaceTrack();

        // Initialize the terrain
        terrain = new Terrain();
    }

    /**
     * Called upon the start of the application. Primarily used to configure
     * OpenGL.
     */
    @Override
    public void initialize() {

        // Enable blending.
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Anti-aliasing can be enabled by uncommenting the following 4 lines.
        // This can however cause problems on some graphics cards.
        gl.glEnable(GL_LINE_SMOOTH);
        gl.glEnable(GL_POLYGON_SMOOTH);
        gl.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        // Enable depth testing.
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);

        // Normalize normals.
        gl.glEnable(GL_NORMALIZE);

        // Converts colors to materials when lighting is enabled.
        gl.glEnable(GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL_FRONT_AND_BACK, GL_AMBIENT_AND_DIFFUSE);

        // Enable textures.
        gl.glEnable(GL_TEXTURE_2D);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glBindTexture(GL_TEXTURE_2D, 0);

        // Enable lightning
        gl.glEnable(GL_LIGHTING);

        // Create an ambient light
        gl.glEnable(GL_LIGHT0);
        {
            // Configuration
            float ambientLight[]    = { 0.2f,   0.2f,   0.2f,   1.0f };
            float diffuseLight[]    = { 0.8f,   0.8f,   0.8f,   1.0f };
            float specularLight[]   = { 0.5f,   0.5f,   0.5f,   1.0f };

            // Setup the light with the configuration specified above.
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_AMBIENT, ambientLight, 0);
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_DIFFUSE, diffuseLight, 0);
            gl.glLightfv(gl.GL_LIGHT0, gl.GL_SPECULAR, specularLight, 0);
        }

        // Create a positional light
        gl.glEnable(GL_LIGHT1);
        {
            // Configuration
            float ambientLight[]    = { 0f,     0f,     0f,     0f};
            float diffuseLight[]    = { 1f,     0.8f,   0.8f,   1.0f };
            float specularLight[]   = { 0.5f,   0.5f,   0.5f,   1.0f };

            // Set up the positional light with the configuration specified above
            gl.glLightfv(gl.GL_LIGHT1, gl.GL_AMBIENT, ambientLight, 0);
            gl.glLightfv(gl.GL_LIGHT1, gl.GL_DIFFUSE, diffuseLight, 0);
            gl.glLightfv(gl.GL_LIGHT1, gl.GL_SPECULAR, specularLight, 0);
            // Position is changed every camera update.
        }

        // Try to load four textures, add more if you like.
        gl.glEnable(GL_TEXTURE_2D);
            track = loadTexture("track.png");
            brick = loadTexture("brick.jpg");
            head = loadTexture("head.jpg");
            torso = loadTexture("textureRobot.jpg");//full texture of the robot
        gl.glDisable(GL_TEXTURE_2D);

        // setup cameras
        mainCamera.frameBuffer = new FrameBuffer(0);
        screenCamera.frameBuffer = new FrameBuffer();
        screenCamera.frameBuffer.create();
        screenCamera.onChangeMode(3);

    }

    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        // Select part of window.
        Dimensions dimensions = camera.frameBuffer.getDimensions();
        float vDist = camera.getViewingDistance();

        gl.glViewport(0, 0, dimensions.w(), dimensions.h());

        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        // Set the perspective.

        float aspectRatio = (float) dimensions.w() / (float) dimensions.h();

        float fovY, vHeight;

        // Calculate the view height from the aspect ratio
        if(camera == mainCamera) {
            vHeight = gs.vWidth / aspectRatio;

        // Calculate the view height from the horizontal fov
        } else {
            vHeight = (float)Math.tan(camera.getFov()/2.f) * (2.f*vDist);
        }

        // Calculate vertical fov
        //        /|
        //      /  | (2)
        //    /    |
        //  /(1)   |
        //  =======|
        //  \  (3) |
        //    \    |
        //      \  |
        //        \|
        //
        // (1): 1/2 fovY
        // (2): 1/2 vHeight
        // (3): vDist
        fovY = (float)Math.atan(
            vHeight/(
                2.f * vDist
            )
        ) * 2.f;

        // Initialize perspective from calculated values
        glu.gluPerspective(
            (float) Math.toDegrees(fovY),
            aspectRatio,
            0.1 * vDist,
            100 * vDist
        );

        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // Initialize camera modes on change
        if (camera == mainCamera && camera == mainCamera && camera.getMode() != gs.camMode) {
            camera.onChangeMode(gs.camMode);
        }

        // Update the view according to the camera mode
        camera.update(gs.theta, gs.phi);


        // Initialize the viewing matrix

        glu.gluLookAt(
            camera.eye.x(),    camera.eye.y(),    camera.eye.z(),
            camera.center.x(), camera.center.y(), camera.center.z(),
            camera.up.x(),     camera.up.y(),     camera.up.z());



        // Update the light only for the main camera
        if(camera == mainCamera)
        {
            // Calculate the direction that is being looked at
            Vector viewDirection    = camera.eye.subtract(camera.center);

            // Calculate a vector to the left (relative to the eye)
            Vector leftDirection    = viewDirection.cross(camera.up).normalized();

            // Calculate a vector upwards (relative to the eye)
            Vector upDirection      = leftDirection.cross(viewDirection).normalized();

            // Calculate the direction the light is relative to the camera
            Vector leftUpDirection  = leftDirection.add(upDirection)
                                                .normalized()
                                                .scale(1.1f);

            // Calculate the position of the light
            Vector leftUp = camera.eye.add(leftUpDirection);

            // Update the light's properties
            float lightPosition[] = { (float)leftUp.x(), (float)leftUp.y(), (float)leftUp.z(), 0 };
            gl.glLightfv(gl.GL_LIGHT1, gl.GL_POSITION, lightPosition, 0);
        }
    }

    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        drawAs(mainCamera);
        drawAs(screenCamera);
//         mainCamera.frameBuffer.bind();
    }

    public void drawAs(Camera cam) {
        if(cam != camera) {
            camera = cam;
            camera.frameBuffer.bind();
            setView();
        }

        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Axes should not be affected by light
        gl.glDisable(GL_LIGHTING);

            gl.glEnable(GL_TEXTURE_2D);
                gl.glColor3f(1.f, 1.f, 1.f);

                screenCamera.frameBuffer.bindColorBuffer();

                gl.glPushMatrix();
                    Dimensions textureDimensions = screenCamera.frameBuffer.getDimensions();

                    Vector screenPosition = raceTrack.getPoint(0);
                    Vector outerScreenPosition = raceTrack.getOuter(0, screenPosition);

                    Vector screenDelta = outerScreenPosition.subtract(screenPosition);
                    Vector screenDelta2D = new Vector(screenDelta.x(), screenDelta.y(), 0);

                    gl.glPushMatrix();
                        gl.glTranslated(screenPosition.x(), screenPosition.y(), screenPosition.z()+2);
                        gl.glRotated(Math.toDegrees(
                            Math.acos(
                                screenDelta2D.normalized().dot(Vector.X)
                            )), 0.f, 0.f, 1.f);
                        gl.glScaled(
                            screenDelta2D.length(),
                            textureDimensions.w()/(textureDimensions.w()*screenDelta2D.length()),
                            1.f
                        );
                        gl.glBegin(GL_QUADS);
                            gl.glTexCoord2d(0.f, 0.f); gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glTexCoord2d(1.f, 0.f); gl.glVertex3f(1.f, 0.f, 0.f);
                            gl.glTexCoord2d(1.f, 1.f); gl.glVertex3f(1.f, 0.f, 1.f);
                            gl.glTexCoord2d(0.f, 1.f); gl.glVertex3f(0.f, 0.f, 1.f);
                        gl.glEnd();
                    gl.glPopMatrix();

                    gl.glPushMatrix();
                        gl.glTranslated(screenPosition.x(), screenPosition.y(), screenPosition.z()+1);
                        gl.glColor3d(0.2f,0.2f,0.2f);
                        gl.glScaled(0.18f, 0.18f, 4f);
                        glut.glutSolidCube(1.f);
                    gl.glPopMatrix();
                    
                    gl.glPushMatrix();
                        gl.glTranslated(outerScreenPosition.x(), outerScreenPosition.y(), outerScreenPosition.z()+1);
                        gl.glColor3d(0.2f,0.2f,0.2f);
                        gl.glScaled(0.18f, 0.18f, 4f);
                        glut.glutSolidCube(1.f);
                    gl.glPopMatrix();
                gl.glPopMatrix();
            gl.glDisable(GL_TEXTURE_2D);
            

            // Draw the axis frame
            if (gs.showAxes) {
                drawAxisFrame();
            }
        gl.glEnable(GL_LIGHTING);

        // Draw all robots
        int i = 0;
        for(Robot bob : robots) {
            gl.glPushMatrix();
                // Draw bob, all our robots are named bob
                float t = gs.tAnim/robots[i].getSpeed();
                Vector position = raceTrack.getPoint(t);
                Vector tangent = raceTrack.getTangent(t);
                
                position = position.add(
                        tangent.cross(Vector.Z).normalized().scale(.5f+i++)
                );
                
                gl.glTranslated(position.x(), position.y(), position.z());

                gl.glRotatef(
                    (float)Math.toDegrees(Math.atan(tangent.y() / tangent.x())) + (tangent.x() < 0 ? 90 : -90),
                    0.f, 0.f, 1.f
                );
                
                bob.draw(gs.showStick, t);
                
            gl.glPopMatrix();
        }

        
        // Draw race track
        Material.GOLD.set(gl);
        raceTrack.draw(gs.trackNr);

        // Draw terrain
        terrain.draw();
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin
     * (yellow).
     */
    public void drawAxisFrame() {
        // Configuration
        final float CONE_LENGTH = 0.25f;
        final float CONE_SIZE   = CONE_LENGTH * 0.2f;
        final float AXIS_LENGTH = 1f;
        final float LINE_LENGTH = AXIS_LENGTH - CONE_LENGTH;
        final float LINE_WIDTH = 0.015f;

        // All the axis are drawn by us
        Vector [] directions = {
            Vector.X,
            Vector.Y,
            Vector.Z
        };
   
        
        
        for (Vector direction : directions) {
            // Set the color, colors match the directions
            gl.glColor4d(direction.x(), direction.y(), direction.z(), 1.);

            gl.glPushMatrix();
                // Move to the center position
                gl.glTranslated(
                    direction.x() * LINE_LENGTH/2,
                    direction.y() * LINE_LENGTH/2,
                    direction.z() * LINE_LENGTH/2);

                gl.glPushMatrix();
                    // Scale to the line length on the direction
                    // And line width on other directions
                    gl.glScaled(
                        LINE_LENGTH * direction.x() + (1.f - direction.x()) * LINE_WIDTH,
                        LINE_LENGTH * direction.y() + (1.f - direction.y()) * LINE_WIDTH,
                        LINE_LENGTH * direction.z() + (1.f - direction.z()) * LINE_WIDTH);

                    // Draw the "line"
                    glut.glutSolidCube(1f);
                gl.glPopMatrix();

                // Move to the end of the line
                gl.glTranslated(
                    direction.x() * LINE_LENGTH/2,
                    direction.y() * LINE_LENGTH/2,
                    direction.z() * LINE_LENGTH/2);

                // Rotate in the correct direction
                gl.glRotated(90f, -direction.y(), direction.x(), direction.z());

                // Draw the tip
                glut.glutSolidCone(CONE_SIZE, CONE_LENGTH, 50, 51);

            gl.glPopMatrix();
        }

        // Draw the origin
        gl.glColor4f(255, 255, 0, 1);
        glut.glutSolidSphere(0.05f, 50, 51);
    }

    /**
     * Materials that can be used for the robots.
     * Source: http://devernay.free.fr/cours/opengl/materials.html
     */
    public enum Material {
        /**
         * Gold material properties. Modify the default values to make it look
         * like gold.
         */
        GOLD(
        new float[]{0.75164f, 0.60648f, 0.22648f, 1f},
        new float[]{0.628281f, 0.555802f, 0.366065f, 1f}),
        /**
         * Silver material properties. Modify the default values to make it look
         * like silver.
         */
        SILVER(
        new float[]{0.50754f, 0.50754f, 0.50754f, 1f},
        new float[]{0.508273f, 0.508273f, 0.508273f, 1f}),
        /**
         * Wood material properties. Modify the default values to make it look
         * like wood.
         */
        WOOD(
        new float[]{0.227f, 0.13f, 0.065f, 1.0f},
        new float[]{0.3f, 0.14f, 0.071f, 1.0f}),
        /**
         * Orange material properties. Modify the default values to make it look
         * like orange.
         */
        ORANGE(
        new float[]{1f, 0.5f, 0f, 1.0f},
        new float[]{1f, 0.5f, 0f, 1.0f}),
        
        BLANK(
        new float[]{1.0f, 1.0f, 1.0f, 1.0f},
        new float[]{1f, 0.5f, 0f, 1.0f}),
        
        WATER(
        new float[]{0.50754f, 0.50754f, 0.50754f, 0.5f},
        new float[]{0.508273f, 0.508273f, 0.508273f, 0.3f}),
        
        LEAF(
                new float[]{0.0f, 0.2f, 0.0f, 1.0f},
                new float[]{0.0f, 0.1f, 0.0f, 1.0f});
        /* 
        
        /**
         * The diffuse RGBA reflectance of the material.
         */
        float[] diffuse;
        /**
         * The specular RGBA reflectance of the material.
         */
        float[] specular;

        /**
         * Constructs a new material with diffuse and specular properties.
         */
        private Material(float[] diffuse, float[] specular) {
            this.diffuse = diffuse;
            this.specular = specular;
        }

        /**
         * @return The shine shininess
         */
        public int getShine() {
            switch(this) {
                case GOLD:      return (int)Math.round(0.4*128);
                case SILVER:    return (int)Math.round(0.4*128);
                case WOOD:      return (int)Math.round(0.1*128);
                case ORANGE:    return (int)Math.round(0.25*128);
                case WATER:     return (int)Math.round(0.7*128);
                case LEAF:      return (int)Math.round(0.05*128);
                default:        return 0;
            }
        }

        /**
         * Set the material
         * @param gl The context to set it on
         */
        public void set(GL2 gl) {
            // OpenGL, Do the thing!
            gl.glMaterialfv(GL_FRONT_AND_BACK,  GL_SPECULAR,    specular,   0);
            gl.glMaterialfv(GL_FRONT_AND_BACK,  GL_DIFFUSE,     diffuse,    0);
            gl.glMaterialfv(GL_FRONT_AND_BACK,  GL_AMBIENT,     diffuse,    0);
            gl.glMateriali(GL_FRONT_AND_BACK,   GL_SHININESS,   getShine());
            // Nooo, not the thing!
            gl.glColor4fv(diffuse, 0);
        }
    }
      
    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    private class Robot {
        /**
         * The material from which this robot is built.
         */        
        private Material material = null;

        float speedModifier = 80;
        
        int id;
        
        Material headColor = null;
        Material neckColor = null;
        Material shoulderColor = null;
        Material chestColor = null;
        Material hipColor = null;

        Material legColor = null;

        Material [] singleLegColor = new Material[] {
            legColor,
            legColor
        };

        Material [][] legPartColor = new Material[][] {
            singleLegColor,
            singleLegColor,
        };

        Material [][] legJointColor = new Material[][] {
            singleLegColor,
            singleLegColor,
        };

        Material[][] armPartColor   = legPartColor;
        Material[][] armJointColor  = legJointColor;

        Material [] footColor = new Material[] {
            legColor,
            legColor,
        };

        float neckHeightModifier = 1.f;

        /**
         * Change the materials that are set to the default material
         * @param material The material to change to
         */
        void setDefaultMaterial(Material material) {
            headColor           = headColor         == this.material ? material : headColor;
            neckColor           = neckColor         == this.material ? material : neckColor;
            shoulderColor       = shoulderColor     == this.material ? material : shoulderColor;
            chestColor          = chestColor        == this.material ? material : chestColor;
            hipColor            = hipColor          == this.material ? material : hipColor;
            legColor            = legColor          == this.material ? material : legColor;
            singleLegColor[0]   = singleLegColor[0] == this.material ? material : singleLegColor[0];
            singleLegColor[1]   = singleLegColor[1] == this.material ? material : singleLegColor[1];
            footColor[0]        = footColor[0]      == this.material ? material : footColor[0];
            footColor[1]        = footColor[1]      == this.material ? material : footColor[1];
            this.material = material;
        }

        /**
         * Constructs the robot with initial parameters.
         * @param material The default material
         */
        public Robot(Material material, int id) {
            setDefaultMaterial(material);
            this.id = id;

        }
        
        public Robot setSpeed(float speed){
            this.speedModifier = speed;
            return this;
        }
        
        public float getSpeed(){
            return speedModifier;
        }

        /**
         * Set the material of the head
         * @param material The material to set it to
         * @return this for method chaining
         */
        public Robot setHeadMaterial(Material material) {
            this.headColor = material;
            return this;
        }

        /**
         * Set the neck modifier
         * @param neckHeightModifier The modifier to set on the neck
         * @return this for method chaining
         */
        public Robot setNeckModifier(float neckHeightModifier) {
            this.neckHeightModifier = neckHeightModifier;
            return this;
        }
        
        /*public Vector getPos(){
            return new Vector(posX,posY,posZ);
        }*/

        /**
         * Draws the robot
         * @param stickFigure Whether to draw this robot as a stick figure
         * @param t The position in the cycle (0 - 1), used for animation.
         */
        
        public void draw(boolean stickFigure, float t) {
            // The magic number, TODO: calculate this somehow
            t *= 100 * Math.PI;

            // The mother of all magic numbers
            final float VAKJE                   = 0.1f;
            final float SHOULDER_OVERLAP_MAGIC  = 1.f;

            final float TORSO_HEIGHT            = 5     *VAKJE;
            final float TORSO_THICKNESS         = 1.5f  *VAKJE;
            final float SHOULDER_HEIGHT         = 2     *VAKJE;
            final float SHOULDER_WIDTH          = TORSO_HEIGHT;
            final float NECK_HEIGHT             = 1     *VAKJE  *neckHeightModifier;
            final float NECK_WIDTH              = 0.5f  *VAKJE;
            final float HEAD_HEIGHT             = 3     *VAKJE;
            final float HEAD_WIDTH              = 2     *VAKJE;
            final float SHOUlDER_JOINT_HEIGHT   = 1     *VAKJE;
            final float SHOULDER_JOINT_WIDTH    = 1.3f  *VAKJE;
            final float ARM_PART_LENGTH         = 4     *VAKJE;
            final float LEG_PART_LENGTH         = 5     *VAKJE;
            final float TORSO_BOTTOM_HEIGHT     = 1.5f  *VAKJE;
            final float TORSO_BOTTOM_WIDTH      = 0.95f *TORSO_HEIGHT;
            final float FEET_LENGTH             = 2.f   *VAKJE;

            final int PRECISION                 = 40;
            final int PRECISION2                = PRECISION+1;

            final float ARM_WIDTH               = SHOULDER_JOINT_WIDTH * 0.8f;
            final float ELBOW_JOINT_WIDTH       = SHOULDER_JOINT_WIDTH;
            final float ARM_HEIGHT              = ARM_WIDTH * 0.8f;

            final float LEG_WIDTH               = ARM_WIDTH;
            final float KNEE_JOINT_WIDTH        = ELBOW_JOINT_WIDTH;
            final float KNEE_JOINT_HEIGHT       = SHOUlDER_JOINT_HEIGHT;
            final float LEG_HEIGHT              = LEG_WIDTH * 0.8f;
            final float FEET_HEIGHT             = KNEE_JOINT_HEIGHT;
            final float FEET_WIDTH              = LEG_WIDTH;

            final float TORSO_RELATIVE_HEIGHT   = 2*LEG_PART_LENGTH+TORSO_HEIGHT/2+TORSO_BOTTOM_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC)+KNEE_JOINT_HEIGHT/2;
        
            
            
            gl.glPushMatrix();
                // Move up till torso level
                gl.glTranslatef(0.f, 0.f, TORSO_RELATIVE_HEIGHT);

                // Draw the torso
                gl.glPushMatrix();
                    gl.glTranslatef(0.f, TORSO_THICKNESS/2, 0.f);
                    gl.glRotatef(90, 1.f, 0.f, 0.f);

                    chestColor.set(gl);

                    if(stickFigure) {
                        gl.glBegin(gl.GL_LINES);
                            gl.glVertex3f(0.f, (TORSO_HEIGHT+SHOULDER_HEIGHT)/2, TORSO_THICKNESS/2);
                            gl.glVertex3f(0.f, TORSO_HEIGHT/2, 0.f);
                            gl.glVertex3f(0.f, TORSO_HEIGHT/2, 0.f);
                            gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glVertex3f(0.f, -TORSO_HEIGHT/2, 0.f);
                            gl.glVertex3f(0.f, -TORSO_HEIGHT/2, 0.f);
                            gl.glVertex3f(0.f, -TORSO_HEIGHT/2-TORSO_BOTTOM_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC), TORSO_THICKNESS/2);
                        gl.glEnd();
                    } else {
                        //glut.glutSolidCylinder(TORSO_HEIGHT/2, TORSO_THICKNESS, PRECISION, PRECISION2);
                        drawCylinderFront(TORSO_HEIGHT/2, TORSO_THICKNESS, PRECISION, 0, 168, 420*id+252, 420*id+252+168);
                    }
                gl.glPopMatrix();

                gl.glPushMatrix();
                    // Draw the hips
                    gl.glPushMatrix();
                        gl.glTranslatef(0.f, 0.f, -TORSO_HEIGHT/2-TORSO_BOTTOM_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC));
                        gl.glTranslatef(-TORSO_BOTTOM_WIDTH/2, 0.f, 0.f);
                        gl.glRotatef(90, 0.f, 1.f, 0.f);

                        hipColor.set(gl);
                        if(stickFigure) {
                            gl.glBegin(gl.GL_LINES);
                                gl.glVertex3f(0.f, 0.f, 0);
                                gl.glVertex3f(0.f, 0.f, TORSO_BOTTOM_WIDTH);
                            gl.glEnd();
                        } else {
                            //glut.glutSolidCylinder(TORSO_BOTTOM_HEIGHT/2, TORSO_BOTTOM_WIDTH, PRECISION, PRECISION2);
                            drawCylinder(TORSO_BOTTOM_HEIGHT/2, TORSO_BOTTOM_WIDTH, PRECISION, 150, 250, 420*id, 420*id+100);
                        }
                    gl.glPopMatrix();

                    // Draw the legs
                    gl.glTranslatef(0.f, 0.f, -TORSO_BOTTOM_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC));
                    for(int i = 0; i < 2; i++)
                    {
                        gl.glPushMatrix();
                            gl.glTranslatef(0.f, 0.f, -TORSO_HEIGHT/2);
                            double r = 25 * Math.sin(t) * (i == 0 ? 1 : -1);
                            gl.glRotatef((float)r, 1.f, 0.f, 0.f);

                            // Mirror to the other side on the 2nd leg
                            if(i == 1) {
                                gl.glScalef(-1.f, 1.f, 1.f);
                            }

                            gl.glTranslatef(TORSO_BOTTOM_WIDTH/2-3*LEG_WIDTH/4,0.f,0.f);
                            for(int j = 0; j < 2; j++)
                            {
                                // Draw a  leg part
                                if(j==1){
                                gl.glRotatef(-1.5f*(float)(r), 1.f, 0.f, 0.f);
                                }
                                
                                gl.glPushMatrix();
                                    gl.glTranslatef(0.f, 0.f, -LEG_PART_LENGTH/2);
                                    gl.glScalef(LEG_WIDTH, LEG_HEIGHT, LEG_PART_LENGTH);

                                    legPartColor[i][j].set(gl);

                                    if(stickFigure) {
                                        gl.glBegin(gl.GL_LINES);
                                            gl.glVertex3f(0.f, 0.f, 0.f);
                                            gl.glVertex3f(0.f, 0.f, 0.5f);
                                            gl.glVertex3f(0.f, 0.f, 0.f);
                                            gl.glVertex3f(0.f, 0.f, -0.5f);
                                        gl.glEnd();
                                    } else {
                                        drawBox(gl, 1.0f, 0, 0+j*60, 60+j*60, id*420, id*420+252);
                                    }
                                gl.glPopMatrix();

                                gl.glTranslatef(0.f, 0.f, -LEG_PART_LENGTH);

                                // Draw a leg joint
                                gl.glPushMatrix();
                                    gl.glTranslatef(-KNEE_JOINT_WIDTH/2, 0.f, 0.f);
                                    gl.glRotatef(90, 0.f, 1.f, 0.f);
                                    legJointColor[i][j].set(gl);
                                    if(stickFigure) {
                                        gl.glBegin(gl.GL_LINES);
                                            gl.glVertex3f(0.f, 0.f, 0.f);
                                            gl.glVertex3f(0.f, 0.f, KNEE_JOINT_WIDTH);
                                        gl.glEnd();
                                    } else {
                                        //glut.glutSolidCylinder(KNEE_JOINT_HEIGHT/2, KNEE_JOINT_WIDTH, PRECISION, PRECISION2);
                                        drawCylinder(KNEE_JOINT_HEIGHT/2, KNEE_JOINT_WIDTH, PRECISION, 150, 250, 420*id, 420*id+100);
                                    }
                                gl.glPopMatrix();

                                if(j == 0) {
                                    gl.glRotatef((float)-Math.abs(r) * 1.2f, 1.f, 0.f, 0.f);
                                }
                            }

                            // Draw a foot
                            gl.glRotatef(90, 0.f, 0.f, 1.f);
                            gl.glTranslatef(0.f, -KNEE_JOINT_WIDTH/2.5f, -KNEE_JOINT_HEIGHT/2);
                            gl.glScalef(FEET_LENGTH, FEET_WIDTH, FEET_HEIGHT);

                            footColor[i].set(gl);

                            /* Feet look like this:
                             * z=1  __ ==\
                             *  \==--      \
                             *  | \   ---    \
                             *  |   \    ---   \
                             *  |     \      --==\
                             *  |       \   __
                             *  =========/==
                             *  O        x=1
                             */

                             // Draw a stick figure or a triangle strip depending on the mode
                            gl.glEnable(gl.GL_TEXTURE_2D);
                            torso.bind(gl);
                            //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, dimensions.w(), dimensions.h(), 0, GL_RGB, GL_UNSIGNED_BYTE, null);
                            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
                            gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
                            
                            float textureX = 600;
                            float textureX2 = 650;
                            float textureY = id*420;
                            float textureY2 = id*420 + 50;
                            
                            float deltaX = 720;
                            float deltaY = 2100;
                            
                             gl.glBegin(stickFigure ? gl.GL_LINE_STRIP : gl.GL_TRIANGLE_STRIP);
                                // Left side
                                gl.glNormal3f(0.f, -1.f, 0.f); gl.glTexCoord2f(textureX/deltaX,textureY/deltaY); gl.glVertex3f(0.f, 0.f, 0.f);
                                gl.glNormal3f((float)Math.sqrt(2), -1.f, (float)Math.sqrt(2)); gl.glTexCoord2f(textureX2/deltaX,textureY/deltaY); gl.glVertex3f(1.f, 0.f, 0.f);
                                gl.glNormal3f((float)Math.sqrt(2), -1.f, (float)Math.sqrt(2)); gl.glTexCoord2f(textureX/deltaX,textureY2/deltaY); gl.glVertex3f(0.f, 0.f, 1.f);

                                //Front quad
                                gl.glNormal3f((float)Math.sqrt(2), 1.f, (float)Math.sqrt(2)); gl.glTexCoord2f(textureX2/deltaX,textureY/deltaY); gl.glVertex3f(1.f, 1.f, 0.f);
                                gl.glNormal3f((float)Math.sqrt(2)-1.f, 1.f, (float)Math.sqrt(2)); gl.glTexCoord2f(textureX/deltaX,textureY2/deltaY); gl.glVertex3f(0.f, 1.f, 1.f);

                                //Right side
                                gl.glNormal3f(-1.f, 1.f, 0.f); gl.glTexCoord2f(textureX/deltaX,textureY2/deltaY); gl.glVertex3f(0.f, 1.f, 0.f);

                                //Back side
                                gl.glNormal3f(-1.f, 0.f, 0.f); gl.glTexCoord2f(textureX/deltaX,textureY2/deltaY); gl.glVertex3f(0.f, 0.f, 1.f);
                                gl.glNormal3f(-1.f, 0.f, -1.f); gl.glTexCoord2f(textureX/deltaX,textureY/deltaY); gl.glVertex3f(0.f, 0.f, 0.f);
                                gl.glNormal3f(-1.f, 0.f, -1.f); gl.glTexCoord2f(textureX2/deltaX,textureY/deltaY); gl.glVertex3f(0.f, 1.f, 0.f);

                                //Bottom side
                                gl.glNormal3f(0.f, 0.f, -1.f); gl.glTexCoord2f(textureX2/deltaX,textureY/deltaY); gl.glVertex3f(1.f, 0.f, 0.f);
                                gl.glNormal3f(0.f, 0.f, -1.f); gl.glTexCoord2f(textureX2/deltaX,textureY2/deltaY); gl.glVertex3f(1.f, 1.f, 0.f);

                                // Stick figure needs additional vertexes
                                if(stickFigure) {
                                    gl.glVertex3f(1.f, 0.f, 0.f);
                                    gl.glVertex3f(1.f, 1.f, 0.f);
                                    gl.glVertex3f(0.f, 1.f, 0.f);
                                    gl.glVertex3f(0.f, 1.f, 1.f);
                                    gl.glVertex3f(0.f, 0.f, 1.f);
                                }
                            gl.glEnd();
                          gl.glDisable(gl.GL_TEXTURE_2D);
                        gl.glPopMatrix();
                    }
                gl.glPopMatrix();

                // Draw the shoulders
                gl.glTranslatef(0.f, 0.f, TORSO_HEIGHT/2+SHOULDER_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC));
                gl.glPushMatrix();
                    gl.glTranslatef(-SHOULDER_WIDTH/2, 0.f, 0.f);
                    gl.glRotatef(90, 0.f, 1.f, 0.f);

                    shoulderColor.set(gl);

                    if(stickFigure) {
                        gl.glBegin(gl.GL_LINES);
                            gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glVertex3f(0.f, 0.f, SHOULDER_WIDTH);
                        gl.glEnd();
                    } else {
                        //glut.glutSolidCylinder(SHOULDER_HEIGHT/2, SHOULDER_WIDTH, PRECISION, PRECISION2);
                        drawCylinder(SHOULDER_HEIGHT/2, SHOULDER_WIDTH, PRECISION, 150, 250, 420*id, 420*id+100);
                    }
                gl.glPopMatrix();

                //Draw the neck
                gl.glPushMatrix();
                    neckColor.set(gl);

                    if(stickFigure) {
                        gl.glBegin(gl.GL_LINES);
                            gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glVertex3f(0.f, 0.f, NECK_HEIGHT+SHOULDER_HEIGHT/2);
                        gl.glEnd();
                    } else {
                        //glut.glutSolidCylinder(NECK_WIDTH/2 ,NECK_HEIGHT+SHOULDER_HEIGHT/2, PRECISION, PRECISION2);
                        drawCylinder(NECK_WIDTH/2 ,NECK_HEIGHT+SHOULDER_HEIGHT/2, PRECISION, 150, 250, 420*id, 420*id+100);
                    }
                gl.glPopMatrix();

                // Draw the head
                gl.glPushMatrix();
                    gl.glTranslatef(0.f, 0.f, NECK_HEIGHT+SHOULDER_HEIGHT/2);

                    headColor.set(gl);

                    if(stickFigure) {
                        gl.glBegin(gl.GL_LINES);
                            gl.glVertex3f(0.f, 0.f, 0.f);
                            gl.glVertex3f(0.f, 0.f, HEAD_HEIGHT);
                        gl.glEnd();
                    } else {
                        //glut.glutSolidCylinder(HEAD_WIDTH/2, HEAD_HEIGHT, PRECISION, PRECISION2);
                        gl.glRotated(-90, 0, 0, 1);
                        drawCylinder(HEAD_WIDTH/2, HEAD_HEIGHT, PRECISION, 108, 108+260, 420*id+147, 420*id+147+105);
                    }
                gl.glPopMatrix();

                // Draw the arms
                for(int i = 0; i < 2; i++)
                {
                    gl.glPushMatrix();
                        shoulderColor.set(gl);

                        double r = - 25 * Math.sin(t) * (i == 0 ? 1 : -1);//set angle of arm
                        gl.glRotatef((float)r, 1.f, 0.f, 0.f);
                        
                        // Mirror the 2nd arm
                        if(i == 1) {
                            gl.glScalef(-1.f, 1.f, 1.f);
                        }

                        gl.glTranslatef(SHOULDER_WIDTH/2, 0.f, 0.f);

                        // Draw the shoulder joint
                        gl.glPushMatrix();
                            gl.glRotatef(90, 0.f, 1.f, 0.f);

                            if(stickFigure) {
                                gl.glBegin(gl.GL_LINES);
                                    gl.glVertex3f(0.f, 0.f, 0.f);
                                    gl.glVertex3f(0.f, 0.f, SHOULDER_JOINT_WIDTH);
                                gl.glEnd();
                            } else {
                                //glut.glutSolidCylinder(SHOUlDER_JOINT_HEIGHT/2, SHOULDER_JOINT_WIDTH, PRECISION, PRECISION2);
                                drawCylinder(SHOUlDER_JOINT_HEIGHT/2, SHOULDER_JOINT_WIDTH, PRECISION, 150, 250, 420*id, 420*id+100);
                            }
                        gl.glPopMatrix();

                        gl.glTranslatef(SHOULDER_JOINT_WIDTH, 0.f, 0.f);

                        // Draw the shoulder joint joint end
                        if(stickFigure) {
                            gl.glBegin(gl.GL_LINES);
                                gl.glVertex3f(0.f, 0.f, -SHOUlDER_JOINT_HEIGHT/3);
                                gl.glVertex3f(0.f, 0.f, SHOUlDER_JOINT_HEIGHT/3);
                            gl.glEnd();
                        } else {
                            glut.glutSolidSphere(SHOUlDER_JOINT_HEIGHT/3, PRECISION, PRECISION2);
                        }

                        gl.glTranslatef(-SHOULDER_JOINT_WIDTH/2, 0.f, 0.f);

                        // Draw the actual arms
                        for(int j = 0; j < 2; j++)
                        {
                            armPartColor[i][j].set(gl);

                            if(j==1){
                                gl.glRotatef(20-(float)(0.25f*r), 1.f, 0.f, 0.f);//set angle of arm
                            }
                            // Draw the arm part
                            gl.glPushMatrix();
                                gl.glTranslatef(0.f, 0.f, -ARM_PART_LENGTH/2);
                                gl.glScalef(ARM_WIDTH, ARM_HEIGHT, ARM_PART_LENGTH);

                                
                                if(stickFigure) {
                                    gl.glBegin(gl.GL_LINES);
                                        gl.glVertex3f(0.f, 0.f, -1.f);
                                        gl.glVertex3f(0.f, 0.f, 1.f);
                                    gl.glEnd();
                                } else {
                                    //glut.glutSolidCube(1.f);
                                    drawBox(gl, 1.0f, 0, 344+j*54, 344+54+j*54, id*420+252, id*420+420);
                                }
                            gl.glPopMatrix();

                            armJointColor[i][j].set(gl);

                            gl.glTranslatef(0.f, 0.f, -ARM_PART_LENGTH);

                            // Draw the arm joint
                            gl.glPushMatrix();
                                gl.glTranslatef(-ELBOW_JOINT_WIDTH/2, 0.f, 0.f);
                                gl.glRotatef(90, 0.f, 1.f, 0.f);

                                if(stickFigure) {
                                    gl.glBegin(gl.GL_LINES);
                                        gl.glVertex3f(0.f, 0.f, 0.f);
                                        gl.glVertex3f(0.f, 0.f, ELBOW_JOINT_WIDTH);
                                    gl.glEnd();
                                } else {
                                    //glut.glutSolidCylinder(SHOUlDER_JOINT_HEIGHT/2, ELBOW_JOINT_WIDTH, PRECISION, PRECISION2);
                                    drawCylinder(SHOUlDER_JOINT_HEIGHT/2, ELBOW_JOINT_WIDTH, PRECISION, 150, 250, 420*id, 420*id+100);
                                }
                            gl.glPopMatrix();
                        }
                    gl.glPopMatrix();
                }
            gl.glPopMatrix();
        }
    }

    public class Dimensions {
        private int w, h;
        public Dimensions() {
            this(-1, -1);
        }
        public Dimensions(int w, int h) {
            set(w, h);
        }
        public int w() {
            return w;
        }
        public int h() {
            return h;
        }
        public void set(int w, int h) {
            this.w = w;
            this.h = h;
        }
        public String toString() {
            return "Dimensions{w:"+w+",h:"+h+"}";
        }
    }

    public class FrameBuffer {
        int handle = -1;
        int colorBufferHandle = -1;
        int deptBufferHandle = -1;
        Dimensions dimensions = null;

        FrameBuffer () {}

        // Initialize from existing buffer (Such as 0, the primary screen buffer)
        FrameBuffer (int handle) {
            this.handle = handle;
        }

        public void create() {
            {
                int [] x = new int [1]; // Java is fun
                gl.glGenFramebuffers(1, x, 0);
                handle = x[0];
            }

            bind();

            //Initialize color buffer
            {
                int [] x = new int [1];
                gl.glGenTextures(1, x, 0);
                colorBufferHandle = x[0];
            }

            bindColorBuffer();

            dimensions = dimensions != null ? dimensions : new Dimensions(1024, 768);

            // Give an empty image to OpenGL ( the last "0" )
            gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, dimensions.w(), dimensions.h(), 0, GL_RGB, GL_UNSIGNED_BYTE, null);

            // Poor filtering. Needed !
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);

            {
                int [] x = new int [1];
                gl.glGenRenderbuffers(1, x, 0);
                deptBufferHandle = x[0];
            }

            bindDepthBuffer();

            gl.glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, dimensions.w(), dimensions.h());
            gl.glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, deptBufferHandle);

            // Set color buffer to primary storage
            gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorBufferHandle, 0);

            // Set the list of draw buffers.
            int [] drawBuffers = new int[1];
            drawBuffers[0] = GL_COLOR_ATTACHMENT0;
            gl.glDrawBuffers(1, drawBuffers, 0);

            if(gl.glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
                throw new RuntimeException("Could not create camera buffer");
            }
        }

        void bind() {
            gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, handle);
        }

        void bindColorBuffer() {
            gl.glBindTexture(gl.GL_TEXTURE_2D, colorBufferHandle);
        }

        void bindDepthBuffer() {
            gl.glBindRenderbuffer(GL_RENDERBUFFER, deptBufferHandle);
        }

        Dimensions getDimensions() {
            return dimensions == null ? new Dimensions(gs.w, gs.h) : dimensions;
        }
    }

    /**
     * Implementation of a camera with a position and orientation.
     */
    private class Camera {
        /**
         * The position of the camera.
         */
        public Vector eye = new Vector(3f, 6f, 5f);//starting eye position
        /**
         * The point to which the camera is looking.
         */
        public Vector center = Vector.O;//starting center
        /**
         * The up vector.
         */
        public Vector up = Vector.Z;//up direction

        private int mode = 0;//the viewing mode we are in
        private int robot = 0;//the robot you look at

        public float fov = (float)Math.PI/2.f;//fov of the scene

        public FrameBuffer frameBuffer = null;
        private float viewingDistance = 12;//default viewing distance

        public float getViewingDistance() {
            // This is why we don't use global state ..
            return this == mainCamera ? gs.vDist : viewingDistance;
        }

        public void setViewingDistance(int distance) {
            if(this == mainCamera) {
                gs.vDist = distance;
            }
            viewingDistance = distance;
        }

        public int getMode() {
            return mode;
        }

        public void onChangeMode(int mode) {
            if (-1 == mode) {
            // Helicopter mode
            } else if (1 == mode) {
                setViewingDistance(10);//set some values, to make sure the view doesn't look horrible.
                if(this == mainCamera) {
                    gs.vWidth = 10+(int)(Math.random()*10);//randomize the vWidth a bit.
                }

            // Motor cycle mode
            } else if (2 == mode) {
                setViewingDistance(5);//set some values, to make sure the view doesn't look horrible.

                if(this == mainCamera) {
                    gs.vWidth = 10+(int)(Math.random()*10);//randomize the vWidth a bit.
                }

            // First person mode
            } else if (3 == mode) {
                if(this == mainCamera) {
                    gs.vWidth = 15+(int)(Math.random()*5);//randomize the vWidth a bit.
                }

                for(int i = 0; i < robots.length ; i++) {//find the slowest robot
                    float speed = robots[i].getSpeed();
                    if(speed>=robots[robotFPV].getSpeed()){
                        robotFPV = i;
                    }
                }

            // Auto mode
            } else if (4 == mode) {
                setViewingDistance(12);//set some values, to make sure the view doesn't look horrible.
                startTime = System.currentTimeMillis();
                for(int i = 0; i < robots.length ; i++) {//find the slowest robot
                    float speed = robots[i].getSpeed();

                    if(speed>=robots[robotFPV].getSpeed()) {
                        robotFPV = i;
                    }
                }
            // Default mode
            } else {
                setViewingDistance(25);//set some values, to make sure the view doesn't look horrible.
            }

            robot = (int)(Math.random()*robots.length);//randomly select a robot.
            this.mode = mode;
        }

        public float getFov() {
            return fov;
        }

        public int robotFPV = 0;
        long startTime;
        int interval = 6000;//in miliseconds


        /**
         * Updates the camera viewpoint and direction based on the selected
         * camera mode.
         */
        public void update(float theta, float phi) {
            float vDist = getViewingDistance();

            eye = new Vector (
                vDist * Math.cos(phi) * Math.cos(theta) + center.x(),
                vDist * Math.cos(phi) * Math.sin(theta) + center.y(),
                vDist * Math.sin(phi) + center.z()
            );

            // Helicopter mode
            if (1 == mode) {
                setHelicopterMode();

            // Motor cycle mode
            } else if (2 == mode) {
                setMotorCycleMode();

            // First person mode
            } else if (3 == mode) {
                setFirstPersonMode();

            // Auto mode
            } else if (4 == mode) {
                if(System.currentTimeMillis() - startTime < interval){//change perspective on interval
                    setHelicopterMode();
                }
                else if(System.currentTimeMillis() - startTime < 2*interval){
                    setMotorCycleMode();
                }
                else if(System.currentTimeMillis() - startTime < 3*interval){
                    setFirstPersonMode();
                }
                else{
                    startTime = System.currentTimeMillis();//go back to first perspective
                    setHelicopterMode();
                }

            // Default mode
            } else {
                assert(this == mainCamera);
                center = gs.cnt;
            }
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * helicopter mode.
         */
        private void setHelicopterMode() {
            float t = gs.tAnim/robots[robot].getSpeed();//get the speed of the selected robot, and calculate its actual time
            Vector position = raceTrack.getPoint(t);//find the position
            Vector tangent = raceTrack.getTangent(t);//find the current tangent
            position = position.add(tangent.cross(Vector.Z).normalized().scale(0.5f+robot));//recalculate the position, considering the number of the robot.
            
            center = position;//set the eye position
            eye = center.add(up.scale(10)).add(Vector.X.normalized());//eye position is above the robot.
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * motorcycle mode.
         */
        private void setMotorCycleMode() {
            float t = gs.tAnim/robots[robot].getSpeed();//get the speed of the selected robot, and calculate its actual time
            Vector position = raceTrack.getPoint(t);//find the position
            Vector tangent = raceTrack.getTangent(t);//find the current tangent
            position = position.add(tangent.cross(Vector.Z).normalized().scale(0.5f+robot));//recalculate the position, considering the number of the robot.
                    
            center = position.add(Vector.Z.scale(1.5));  //center is just above the track, on the robot position     
            eye = center.add(tangent.cross(Vector.Z).normalized().scale(-1.75*(robot+1)));//set eye position to be on the inner side of the track.
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * first person mode.
         */
        private void setFirstPersonMode() {
            float t = gs.tAnim/robots[robotFPV].getSpeed();//this time, use the slowest robot
            Vector position = raceTrack.getPoint(t);
            Vector tangent = raceTrack.getTangent(t);
            position = position.add(tangent.cross(Vector.Z).normalized().scale(0.5f+robotFPV));
            
            eye = position.add(Vector.Z.scale(2));//eye position is on top of the head        
            center = eye.add(tangent.normalized().scale(0.5));//eye is on the head, just in front of the robot.
        }
    }

    public interface CurveInterface {
        /**
         * @param t Parameter {@code 0 <= t <= 1}
         * @return The location of the curve at {@code t}
         */
        public Vector getPoint(double t);

        /**
         * @param t Parameter {@code 0 <= t <= 1}
         * @return The tangent of the curve at {@code t}
         */
        public Vector getTangent(double t);
    }

    public class OvalCurve implements CurveInterface {
        protected double x, y;

        public OvalCurve(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public OvalCurve() {
            this(10, 14);
        }
        /**
         * {@inheritdoc}
         */
        public Vector getPoint(double t) {
            return new Vector(
                x * Math.cos(2 * Math.PI * t),
                y * Math.sin(2 * Math.PI * t),
                1
            );
        }

        /**
         * {@inheritdoc}
         */
        public Vector getTangent(double t) {
            return new Vector(
                x * -2 * Math.PI * Math.sin(2 * Math.PI * t),
                y *  2 * Math.PI * Math.cos(2 * Math.PI * t),
                0
            ).normalized();
        }
    }

    public class BezierCurve implements CurveInterface {
        private Vector [] controlPoints;

        public BezierCurve(Vector [] controlPoints) {
            assert(controlPoints != null);
            this.controlPoints = controlPoints;
        }

        protected Vector B(double t, Vector [] points, boolean derative) {
            Vector [] newPoints = new Vector[points.length-1];

            for(int i = 1; i < points.length ; i ++) {
                newPoints[i-1] = points[i-1].add(
                    points[i]
                        .subtract(points[i-1])
                        .scale(t)
                );
            }

            if(derative && newPoints.length == 2) {
                return newPoints[1].subtract(newPoints[0]).normalized();
            } else if(newPoints.length == 1) {
                return newPoints[0];
            } else {
                return B(t, newPoints, derative);
            }
        }

        @Override
        public Vector getPoint(double t) {
            return B(t, controlPoints, false);
        }

        @Override
        public Vector getTangent(double t) {
            return B(t, controlPoints, true);
        };
    }

    /**
     * Implementation of a race track that is made from Bezier segments.
     */
    private class RaceTrack {
        protected CurveInterface currentCurve;

        protected CurveInterface [] curves = new CurveInterface[] {
            new OvalCurve(),
            new BezierCurve(new Vector[] {
                new Vector( 0,	 10,    1),
                new Vector( 5.5, 10,    1),
                new Vector( 10,  5.5,   1),
                new Vector( 10,  0,     1),

                new Vector( 10, -5.5,   1),
                new Vector( 5.5,-10,    1),
                new Vector( 0,  -10,    1),

                new Vector(-5.5,-10,    1),
                new Vector(-10, -5.5,   1),
                new Vector(-10,  0,     1),

                new Vector(-10, 5.5,    1),
                new Vector(-5.5, 10,    1),
                new Vector( 0,   10,    1),
            }),

        };

        public RaceTrack() {
            setCurrentCurve(curves[0]);
        }

        public void setCurrentCurve(CurveInterface curve) {
            currentCurve = curve;
        }

        /**
         * Draws this track, based on the selected track number.
         */
        
        float x = 0;
        public void draw(int trackNr) {
            currentCurve = curves[trackNr];
            x+=0.2;
            
            final double STEP = 0.01;
            for(int j = 0; j < 4; j++) {
                if(j == 0) {
                    gl.glEnable(gl.GL_TEXTURE_2D);

                    track.bind(gl);

                    gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
                    gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
                }

                gl.glBegin(gl.GL_LINES);
                {
                    Vector pos = getPoint(0);
                    Vector dir = getTangent(0);
                    gl.glVertex3d(pos.x(), pos.y(), pos.z());
                    pos = pos.add(dir);
                    gl.glVertex3d(pos.x(), pos.y(), pos.z());
                }
                gl.glEnd();

                gl.glBegin(gl.GL_TRIANGLE_STRIP);
                    for(double i = -3*STEP; i <= 1; i += STEP) {
                        Vector initialPoint = getPoint(i),
                                point = initialPoint;
                        if(j == 1) {
                            point = getLower(point);
                        } else if (j == 2) {
                            point = getOuter(i, point);
                        }

                        float texcoordy = (float)(i * 100) - x;
                        if(j != 0) gl.glColor3d(
                                Math.sin(i*2*Math.PI+x)/2+.5,
                                Math.sin(i*4*Math.PI+x)/2+.5,
                                Math.sin(i*8*Math.PI+x)/2+.5);
                        if(i==0) {
                            gl.glNormal3f(0.f, 0.f, 1.f);
                        } else if(i == 1) {
                            gl.glNormal3f(0.f, 0.f, -1.f);
                        } else {
                            Vector outsideDirection = point.subtract(initialPoint);
                            if(i == 3) {
                                outsideDirection.scale(-1);
                            }
                            gl.glNormal3d(outsideDirection.x(), outsideDirection.y(), outsideDirection.z());
                        }
                        if(j == 0) gl.glTexCoord2f(0.f, texcoordy);
                        gl.glVertex3d(point.x(), point.y(), point.z());

                        Vector outerPoint = (j == 2 || j == 3) ? getLower(point) : getOuter(i, point);
                        if(i==0) {
                            gl.glNormal3f(0.f, 0.f, 1.f);
                        } else if(i == 1) {
                            gl.glNormal3f(0.f, 0.f, -1.f);
                        } else if(i == 2) {
                            Vector outsideDirection = point.subtract(initialPoint);
                            if(i == 3) {
                                outsideDirection.scale(-1);
                            }
                            gl.glNormal3d(outsideDirection.x(), outsideDirection.y(), outsideDirection.z());
                        }
                        if(j == 0) gl.glTexCoord2f(1.f, texcoordy);
                        gl.glVertex3d(outerPoint.x(), outerPoint.y(), outerPoint.z());
                    }
                gl.glEnd();
                if(j == 0) gl.glDisable(gl.GL_TEXTURE_2D);
            }
        }
        
        Vector getLower(Vector initialPosition) {
            return new Vector(initialPosition.x(), initialPosition.y(), -1);
        }
        
        Vector getOuter(double t, Vector initialPosition) {
            Vector tangent = getTangent(t);
            Vector upVector = initialPosition.cross(tangent);
            Vector directionVector = tangent.cross(upVector)
                                            .normalized()
                                            .scale(4);

            return initialPosition.add(directionVector);
        }

        /**
         * Returns the position of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getPoint(double t) {
            return currentCurve.getPoint(t);
        }

        /**
         * Returns the tangent of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getTangent(double t) {
            return currentCurve.getTangent(t);
        }
        
        public class TrackCollision {
            public final boolean isCollision;
            public final double collisionPosition;

            public TrackCollision(boolean isCollision, double collisionPosition) {
                this.isCollision = isCollision;
                this.collisionPosition = collisionPosition;
            }

            public TrackCollision(boolean isCollision) {
                this(false, 0);
                assert(!isCollision);
            }
        };

        public TrackCollision findCollision(float x, float y){
            // Brute force
            Vector initialVector = new Vector(x, y, 1);
            for(double t = 0; t < 1; t += 0.01) {
                Vector point = currentCurve.getPoint(t);

                if(initialVector.subtract(point).length() <= 4.2 &&
                    initialVector.subtract(getOuter(t, point)).length() <= 4.2) {
                    return new TrackCollision(true, t);
                }
            }

            return new TrackCollision(false);
        }

        public float changeHeight(float x, float y, float height){
            if(height > 0.5f){
                TrackCollision collision = findCollision(x, y);
                if(collision.isCollision) {
                    height = 0.5f;
                }
            }
            return height;
        }
    }

    public static final int FLOAT_SIZE = 4;
    enum VertexDefinitionPart {
        POSITION_1D(1   * FLOAT_SIZE),
        POSITION_2D(2   * FLOAT_SIZE),
        POSITION_3D(3   * FLOAT_SIZE),

        NORMAL(3        * FLOAT_SIZE),

        TEXTCOORD_1D(1  * FLOAT_SIZE),
        TEXTCOORD_2D(2  * FLOAT_SIZE),
        TEXTCOORD_3D(3  * FLOAT_SIZE);

        private int stride;

        private VertexDefinitionPart(int stride) {
            this.stride = stride;
        }

        public int getStride() {
            return stride;
        }
    };

    public class VertexDefinition {
        protected VertexDefinitionPart [] parts;

        public VertexDefinition(VertexDefinitionPart [] parts) {
            assert(parts != null);
            this.parts = parts;
        }

        public int getStride() {
            int stride = 0;

            for(VertexDefinitionPart part : parts) {
                stride += part.getStride();
            }

            return stride;
        }

        public VertexDefinitionPart getPartAtStride(int stride) {
            for(VertexDefinitionPart part : parts) {
                if(stride == 0) {
                    return part;
                }

                stride -= part.getStride();
            }

            throw new RuntimeException("Stride out of bounds.");
        }

        public int getStrideOfPart(VertexDefinitionPart part) {
            int stride = 0;

            for(VertexDefinitionPart p : parts) {
                if(p == part) {
                    return stride;
                }
                stride += p.getStride();
            }
            throw new RuntimeException("Part does not exist in definition.");
        }

        public boolean hasPart(VertexDefinitionPart part) {
            for(VertexDefinitionPart p : parts) {
                if(p == part) {
                    return true;
                }
            }
            return false;
        }
    };

    public class VBOBuilder {
        private VertexDefinition definition = null;
        // TODO: directly use buffer in order to allow for double / int arguments
        private ArrayList<Float> buffer = new ArrayList<Float>();
        private int stride = 0;
        private int nVertex = 0;

        public VBOBuilder(VertexDefinition definition) {
            this.definition = definition;
        }

        protected void checkStride(VertexDefinitionPart part) {
            if(definition.getPartAtStride(stride) != part) {
                throw new RuntimeException("This is not expected at this time.");
            }
            stride += part.getStride();
        }

        public void endVertex() {
            if(stride != definition.getStride()) {
                throw new RuntimeException("Vertex stride invalid");
            }

            stride = 0;
            nVertex++;
        }

        public ArrayList<Float> finish() {
            if(stride != 0) {
                throw new RuntimeException("Unterminated vertex");
            }
            return buffer;
        }

        public void addNormal(Vector normal) {
            checkStride(VertexDefinitionPart.NORMAL);

            buffer.add((float)normal.x());
            buffer.add((float)normal.y());
            buffer.add((float)normal.z());
        }

        public void addPositionn(int n, Vector coord) {
            if(n < 0 || n > 3) {
                throw new RuntimeException("Too many dimensions.");
            }
            checkStride(
                n == 1 ? VertexDefinitionPart.POSITION_1D :
                n == 2 ? VertexDefinitionPart.POSITION_2D :
                         VertexDefinitionPart.POSITION_3D
            );

            buffer.add((float)coord.x());
            if(n < 2) return;
            buffer.add((float)coord.y());
            if(n < 3) return;
            buffer.add((float)coord.z());
        }

        public void addPosition1(Vector coord) {
            addPositionn(1, coord);
        }

        public void addPosition(float x) {
            addPositionn(3, new Vector(x, 0, 0));
        }

        public void addPosition2(Vector coord) {
            addPositionn(2, coord);
        }

        public void addPosition(float x, float y) {
            addPositionn(3, new Vector(x, y, 0));
        }

        public void addPosition3(Vector coord) {
            addPositionn(3, coord);
        }

        public void addPosition(float x, float y, float z) {
            addPositionn(3, new Vector(x, y, z));
        }

        public void addTexCoordn(int n, Vector coord) {
            if(n < 0 || n > 3) {
                throw new RuntimeException("Too many texture dimensions.");
            }
            checkStride(
                n == 1 ? VertexDefinitionPart.TEXTCOORD_1D :
                n == 2 ? VertexDefinitionPart.TEXTCOORD_2D :
                         VertexDefinitionPart.TEXTCOORD_3D
            );

            buffer.add((float)coord.x());
            if(n < 2) return;
            buffer.add((float)coord.y());
            if(n < 3) return;
            buffer.add((float)coord.z());
        }

        public void addTexCoord1(Vector coord) {
            addTexCoordn(1, coord);
        }

        public void addTexCoord(float x) {
            addTexCoord1(new Vector(x, 0, 0));
        }

        public void addTexCoord2(Vector coord) {
            addTexCoordn(2, coord);
        }

        public void addTexCoord(float x, float y) {
            addTexCoord1(new Vector(x, y, 0));
        }

        public void addTexCoord3(Vector coord) {
            addTexCoordn(3, coord);
        }

        public void addTexCoord(float x, float y, float z) {
            addTexCoord3(new Vector(x, y, z));
        }

        public int getVertexCount() {
            return nVertex;
        }
    }

    public class VBO {
        private VertexDefinition layout;
        private int nVertex = 0;
        private int vbo = -1;

        public VBO(VertexDefinition layout) {
            this.layout = layout;
        }

        public VBOBuilder getVBOBuilder() {
            return new VBOBuilder(layout);
        }

        public boolean isOpened() {
            return vbo != -1;
        }

        public void open() {
            if(!isOpened()) {
                int [] x = new int [1];
                gl.glGenBuffers(1, x, 0);
                vbo = x[0];
            }
        }

        public void close() {
            // not used
        }

        public void bind() {
            gl.glBindBuffer(gl.GL_ARRAY_BUFFER, vbo);
        }

        public void enable() {
            int vertexPosition = -1;
            int vertexSize = 0;

            int texturePosition = -1;
            int textureSize = 0;

            int normalPosition = -1;

            if(layout.hasPart(VertexDefinitionPart.POSITION_1D)) {
                gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
                vertexPosition = layout.getStrideOfPart(VertexDefinitionPart.POSITION_1D);
                vertexSize = 1;
            } else if(layout.hasPart(VertexDefinitionPart.POSITION_2D)) {
                gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
                vertexPosition = layout.getStrideOfPart(VertexDefinitionPart.POSITION_2D);
                vertexSize = 2;
            } else if(layout.hasPart(VertexDefinitionPart.POSITION_3D)) {
                gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
                vertexPosition = layout.getStrideOfPart(VertexDefinitionPart.POSITION_3D);
                vertexSize = 3;
            }

            if(layout.hasPart(VertexDefinitionPart.TEXTCOORD_1D)) {
                gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);
                texturePosition = layout.getStrideOfPart(VertexDefinitionPart.TEXTCOORD_1D);
                textureSize = 1;
            } else if(layout.hasPart(VertexDefinitionPart.TEXTCOORD_2D)) {
                gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);
                texturePosition = layout.getStrideOfPart(VertexDefinitionPart.TEXTCOORD_2D);
                textureSize = 2;
            } else if(layout.hasPart(VertexDefinitionPart.TEXTCOORD_3D)) {
                gl.glEnableClientState(gl.GL_TEXTURE_COORD_ARRAY);
                texturePosition = layout.getStrideOfPart(VertexDefinitionPart.TEXTCOORD_3D);
                textureSize = 3;
            }

            if(layout.hasPart(VertexDefinitionPart.NORMAL)) {
                gl.glEnableClientState(gl.GL_NORMAL_ARRAY);
                normalPosition = layout.getStrideOfPart(VertexDefinitionPart.NORMAL);
            }

            int stride = layout.getStride();

            if(vertexPosition != -1) {
                gl.glVertexPointer(vertexSize, GL_FLOAT, stride, vertexPosition);
            }

            if(texturePosition != -1) {
                gl.glTexCoordPointer(textureSize, GL_FLOAT, stride, texturePosition);
            }

            if(normalPosition != -1) {
                gl.glNormalPointer(GL_FLOAT, stride, normalPosition);
            }
        }

        public void disable() {
            if(layout.hasPart(VertexDefinitionPart.POSITION_1D) ||
               layout.hasPart(VertexDefinitionPart.POSITION_2D) ||
               layout.hasPart(VertexDefinitionPart.POSITION_3D)) {
                gl.glDisableClientState(gl.GL_VERTEX_ARRAY);
            }

            if(layout.hasPart(VertexDefinitionPart.TEXTCOORD_1D) ||
               layout.hasPart(VertexDefinitionPart.TEXTCOORD_2D) ||
               layout.hasPart(VertexDefinitionPart.TEXTCOORD_3D)) {
                gl.glDisableClientState(gl.GL_TEXTURE_COORD_ARRAY);
            }

            if(layout.hasPart(VertexDefinitionPart.NORMAL)) {
                gl.glDisableClientState(gl.GL_NORMAL_ARRAY);
            }
        }

        public void upload(VBOBuilder builder) {
            ArrayList<Float> buf = builder.finish();

            FloatBuffer vertexData = ByteBuffer
                .allocateDirect(buf.size()*FLOAT_SIZE)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();

            for(float f : buf) {
                vertexData.put(f);
            }

            vertexData.rewind();

            nVertex = builder.getVertexCount();


            System.out.println("vbo="+vbo+"nTris="+nVertex+"buf.size()="+buf.size());

            bind();
            gl.glBufferData(gl.GL_ARRAY_BUFFER, buf.size() * FLOAT_SIZE, vertexData, gl.GL_STATIC_DRAW);
        }

        public int getTriangleCount() {
            return nVertex;
        }

    }

    /**
     * Implementation of the terrain.
     */
    private class Terrain {
        float gridSize = 25;//grid is -25 to 25
        float step = 0.25f;//size of each polygon
        float waterHeight = 0f;//height of the water
        int treeCount = 15;//amount of trees
        PerlinNoise perlin;//the perlinNoise
        int OneDColorId = -1;
        ArrayList<Tree> trees = null;
        float terrainHeightLevel = 5.0f;//severity of the surface
        private Color[] colors = {
            new Color(0,0,255),//blue
            new Color(255,255,0),//yellow
            new Color(0,255,0),//green
            new Color(0,150,0),//dark green
            new Color(0,100,0),//darker dark green
            new Color(0,100,0),//same darker dark green
            new Color(0,50,0),//even darker darker dark green
            new Color(0,50,0),//as dark as previous one.
        };

        VertexDefinition definition = new VertexDefinition(new VertexDefinitionPart [] {
            VertexDefinitionPart.POSITION_3D,
            VertexDefinitionPart.NORMAL,
            VertexDefinitionPart.TEXTCOORD_1D
        });

        VBO vbo = new VBO(definition);

        /**
         * Can be used to set up a display list.
         */
        public Terrain() {
            perlin = new PerlinNoise(123332321, 4, 5.0);//create perlinNoise.
        }

        public void generateTrees() {
            trees = new ArrayList<Tree>();//list of trees
            for(int i = 0; i < treeCount ; i++){//for all trees
                float x = 0;
                float y = 0;
                float z = 0;
                while(z < 0.5f){//while no good position is found
                    x = (float)(gridSize*(1-Math.random()*2));//get x and y, randomly
                    y = (float)(gridSize*(1-Math.random()*2));

                    RaceTrack.TrackCollision collision = raceTrack.findCollision(x, y);//find out if the tree is on the track
                    if(!collision.isCollision) {//if not, set the height
                        z = heightAt(x, y);
                    }
                }
                trees.add(new Tree(x,y,z));//add the tree.
            }
        }

        
        void recomputeGeometry() {
            vbo.open();

            VBOBuilder builder = vbo.getVBOBuilder();

            for(float x = -gridSize;x<gridSize;x+=step)
            {//for every x in the range
                for(float y = -gridSize;y<gridSize;y+=step)
                {//for every y in the range
                    float lowerLeftCorner = heightAt(x,y);//get the 4 corners
                    float lowerRightCorner = heightAt(x+step,y);
                    float upperLeftCorner = heightAt(x,y+step);
                    float upperRightCorner = heightAt(x+step,y+step);

                    /* structure of this quad
                    * 
                    *             ulc - - - - - - urc
                    *              |             / |
                    *              |    diag  /    |
                    *     vertical |       /       |
                    *              |    /          |
                    *              | /             |
                    *             llc - - - - - - lrc
                    *                  horizontal
                    */

                    // Triangle 1

                    // Add lower left corner to VBO
                    builder.addPosition(x, y, lowerLeftCorner);//add the position
                    builder.addNormal(getNormal(x, y, step));//find the normal and add the normal
                    builder.addTexCoord(getColorAtHeight(lowerLeftCorner));//add the texture 
                    builder.endVertex();//close this vertex

                    // Add lower right corner to VBO
                    builder.addPosition(x + step, y, lowerRightCorner);
                    builder.addNormal(getNormal(x + step, y, step));
                    builder.addTexCoord(getColorAtHeight(lowerRightCorner));
                    builder.endVertex();

                    // Add upper right corner to VBO
                    builder.addPosition(x + step, y + step, upperRightCorner);
                    builder.addNormal(getNormal(x + step, y + step, step));
                    builder.addTexCoord(getColorAtHeight(upperRightCorner));
                    builder.endVertex();

                    // Triangle 2

                    // Add lower left corner to VBO
                    builder.addPosition(x, y, lowerLeftCorner);
                    builder.addNormal(getNormal(x, y, step));
                    builder.addTexCoord(getColorAtHeight(lowerLeftCorner));
                    builder.endVertex();

                    // Add upper left corner to VBO
                    builder.addPosition(x, y + step, upperLeftCorner);
                    builder.addNormal(getNormal(x, y + step, step));
                    builder.addTexCoord(getColorAtHeight(upperLeftCorner));
                    builder.endVertex();

                    // Add upper right corner to VBO
                    builder.addPosition(x + step, y + step, upperRightCorner);
                    builder.addNormal(getNormal(x + step, y + step, step));
                    builder.addTexCoord(getColorAtHeight(upperRightCorner));
                    builder.endVertex();
                }
            }
            
            vbo.upload(builder);//add it to the vbo.

            generateTrees();//generate all the trees.
        }

        /**
         * Draws the terrain.
         */
        public void draw() {
            OneDColorId = OneDColorId == -1 ? create1DTexture() : OneDColorId;
            RobotRace.Material.BLANK.set(gl);//set to blank material
            gl.glEnable(GL_TEXTURE_1D);
                gl.glBindTexture(GL_TEXTURE_1D, OneDColorId);

                if(!vbo.isOpened()) {//if no vbo
                    recomputeGeometry();//make it!
                }

                vbo.bind();
                vbo.enable();

                gl.glDrawArrays(gl.GL_TRIANGLES, 0, vbo.getTriangleCount());

                vbo.disable();
            gl.glDisable(GL_TEXTURE_1D);
            
            gl.glEnable(GL_BLEND);//draw the water surface, enable alpha
            gl.glBlendFunc(GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA);//enable alpha
                gl.glBegin(GL_QUADS);
                    RobotRace.Material.WATER.set(gl);//set material water
                    gl.glVertex3d(-gridSize,-gridSize,waterHeight);//all 4 corners of the terrain
                    gl.glVertex3d(gridSize,-gridSize,waterHeight);
                    gl.glVertex3d(gridSize,gridSize,waterHeight);
                    gl.glVertex3d(-gridSize,gridSize,waterHeight);
                gl.glEnd();
            gl.glDisable(GL_BLEND);
            
            if(trees != null) {
                for(Tree tree : trees) {
                    tree.draw();
                }
            }
        }

        private int create1DTexture() {
            int[] textureId = new int[1];

            gl.glEnable(GL_TEXTURE_1D);
                gl.glGenTextures(1 , textureId , 0);
            gl.glDisable(GL_TEXTURE_1D);

            uploadColors(textureId[0]);

            return textureId[0];
        }

        /**
         * Uploads the colors to a 1D texture.
         * @param textureId The OpenGL texture to upload the colors to
         */
        public void uploadColors(int textureId) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(colors.length*4).order(ByteOrder.nativeOrder());//create bytebuffer.
            for(Color color: colors){//for all colors
                int pixel = color.getRGB();//RGB value
                byteBuffer.put((byte)((pixel >>> 16) & 0xFF));//select Red component
                byteBuffer.put((byte)((pixel >>> 8) & 0xFF));//select Green component
                byteBuffer.put((byte)(pixel & 0xFF));//select Blue component
                byteBuffer.put((byte)(pixel >>> 24));//select Alpha component
            }
            byteBuffer.flip();

            gl.glEnable(GL_TEXTURE_1D);
                gl.glBindTexture(GL_TEXTURE_1D, textureId);//bind the textures
                gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, colors.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer);//set the 1d texture
                gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);//add filters
                gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);//add filters
            gl.glDisable(GL_TEXTURE_1D);
        }

        public void setColors(Color [] colors, boolean upload) {
            this.colors = colors;
            if(upload) {
                uploadColors(OneDColorId);
            }
        }

        public void setColors(Color [] colors) {
            setColors(colors, true);
        }

        public Color [] getColors() {
            return colors;
        }
        
        public float heightAt(float x, float y) {
            float height = (float)(perlin.noise2d(x,y) * terrainHeightLevel);
            return heightCorrection(x,y,height);
        }

        public Vector positionAt(float x, float y) {
            return new Vector(x, y, heightAt(x, y));
        }
        
        public float heightCorrection(float x, float y, float z){
            z=raceTrack.changeHeight(x, y, z);
            return z;
        }
        
        /**
         * 
         * @param z = height
         * @return 1D coordinates for 1D texture
         */
        public float getColorAtHeight(float z){
            float max = ((colors.length)/2.0f)-0.5f;//maximum
                
            if(z > max*2){//if above 2x max
                z = max;//make it the max
                z /= max*2+1f;//get a number between 0 and 1, to avoid repeating texture
            } else if(z < -0.5f){
                z = -0.45f;//of lower than -0.5, make it -0.45 to avoid textures fucking up.
                z += 0.5f;
                z /= max+0.5f;//get a number from 0 to 1, to avoid repeating texture
            } else{        
                z += 1f;
                z /= max*2+1f;//get a number between 0 and 1, to avoid repeating texture
            }

            return z;
        }
        
        /**
         * 
         * @param x_ = x coordinate
         * @param y_ = y coordinate
         * @param step = step till next point
         * @return the normal vector.
         */
        public Vector getNormal(double x_, double y_, float step) {
            float x = (float)x_, y = (float)y_;//set x and y
            /**
             *     /  | n /
             *   /    | /          a
             *  w ----p---- e     bcd
             *       /|   /        e
             *      / | s
             */
            Vector n = positionAt(x,        y + step);//find the vector position at specified x and y coordinates
            Vector ne= positionAt(x + step, y + step);
            Vector e = positionAt(x + step, y);
            Vector s = positionAt(x,        y - step);
            Vector sw= positionAt(x - step, y - step);
            Vector w = positionAt(x - step, y);

            Vector p = positionAt(x,        y);

            Vector pn = n.subtract(p);//subtract p from n to find vector pn
            Vector pe = e.subtract(p);
            Vector ps = s.subtract(p);
            Vector pw = w.subtract(p);

            Vector npw = pn.cross(pw);//find north west point by crossing pn with pw.
            Vector wps = pw.cross(ps);
            Vector spe = ps.cross(pe);
            Vector epn = pe.cross(pn);

            Vector normal = npw
                            .add(wps)
                            .add(spe)
                            .add(epn)
                            .normalized();//calculate the normal by adding all nw, ws, se and en.

            assert(normal.z() > 0);//assure the normal points in the correct direction.

//             System.out.println("Position: "+p+" Normal: "+normal);
            
            return normal;//return the normal
        }
    }
    
    private class Tree {
        int levels;//the amount of levels of leaves.
        float logHeight;//height of the stump
        float leafHeight;//height of the leaved part of the tree.
        float treeWidth;//width of the leaved part of the tree
        float logWidth;//width of the stump.
        float offset;//distance between each level.
        float x;//x y and z coordinates.
        float y;
        float z;
        int precision = 8;//precision of the tree objects, low on purpose.
        
        public Tree(float x, float y, float z){
            levels = 3+Math.round((float)Math.random()*3);//minimal levels = 3, max 6
            logHeight = 0.2f +(float)Math.random()*0.5f;//minimal height 0.2, max 0.7
            treeWidth = 1.5f + (0.10f+(float)Math.random()*0.10f)*levels;//minimal width 1.5
            logWidth = 0.2f + (float)Math.random()*0.2f;//minimal width 02f, max 0.4f.
            leafHeight = (0.75f + (float)Math.random()*0.4f)*levels;//minimal height 0.75
            offset = leafHeight/((levels*2)-1);//offset is the total leaf height devided by 2*levels, -1.
            this.x = x;//set the x y and z coordinates of the tree.
            this.y = y;
            this.z = z;
        }
        
        public void draw(){
            gl.glPushMatrix();
                gl.glTranslatef(x, y, 0);
                RobotRace.Material.WOOD.set(gl);//set the material to wood
                glut.glutSolidCylinder(logWidth/2, z+logHeight+0.1f, 50, 51);
                //draw the cylinder as the strump. To make sure the log is long enough, draw from z=0 to the height + logHeight
                gl.glTranslatef(0, 0, z+logHeight);//translate up for height + logHeight
                RobotRace.Material.LEAF.set(gl);//set the material to leaves.
                for(int i=0; i<levels; i++){//for each level of the tree
                    glut.glutSolidCone((1-(i*offset)/(logHeight+leafHeight))*(treeWidth/2), offset*2, precision, precision+1);//calculate the cylindrical size, and draw.
                    gl.glTranslatef(0, 0, offset);//transluate up with the offset.
                }          
            gl.glPopMatrix();
        }
    }

    /**
     * Main program execution body, delegates to an instance of the RobotRace
     * implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
        robotRace.run();
    }
    
    public RaceTrack getTrack(){
        return raceTrack;
    }
    
    
    
    
    
    
    
    
    
    private float[][] boxVertices;
    private float[][] boxNormals = {
      {-1.0f, 0.0f, 0.0f},
      {0.0f, 1.0f, 0.0f},
      {1.0f, 0.0f, 0.0f},
      {0.0f, -1.0f, 0.0f},
      {0.0f, 0.0f, 1.0f},
      {0.0f, 0.0f, -1.0f}
    };
    private int[][] boxFaces = {
      {0, 1, 2, 3},
      {3, 2, 6, 7},
      {7, 6, 5, 4},
      {4, 5, 1, 0},
      {5, 6, 2, 1},
      {7, 4, 0, 3}
    };
    
    /*
     * This code is based on the GLUT approach, but with textures.
     */
    public void drawBox(final GL2 gl, final float size, final int type, float textureX1, float textureX2, float textureY1, float textureY2) {
        gl.glEnable(gl.GL_TEXTURE_2D);
        torso.bind(gl);//bind the texture

        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );

        float deltaX = 720;//x size of texture file
        float deltaY = 2100;//y size of texture file

        textureX1 /= deltaX;//scale to 0 - 1 values
        textureX2 /= deltaX;
        textureY1 /= deltaY;
        textureY2 /= deltaY;

        if (boxVertices == null) {
          final float[][] v = new float[8][];
          for (int i = 0; i < 8; i++) {
            v[i] = new float[3];
          }
          v[0][0] = v[1][0] = v[2][0] = v[3][0] = -0.5f;
          v[4][0] = v[5][0] = v[6][0] = v[7][0] =  0.5f;
          v[0][1] = v[1][1] = v[4][1] = v[5][1] = -0.5f;
          v[2][1] = v[3][1] = v[6][1] = v[7][1] =  0.5f;
          v[0][2] = v[3][2] = v[4][2] = v[7][2] = -0.5f;
          v[1][2] = v[2][2] = v[5][2] = v[6][2] =  0.5f;
          boxVertices = v;
        }
        final float[][] v = boxVertices;
        final float[][] n = boxNormals;
        final int[][] faces = boxFaces;
        for (int i = 5; i >= 0; i--) {//for all 6 faces
          gl.glBegin(gl.GL_QUADS);
          gl.glNormal3fv(n[i], 0);//set the correct normals for this side of the cube.
          float[] vt = v[faces[i][0]];//choose the correct face
          gl.glTexCoord2f(textureX1, textureY1); //set the textures, clockwise
          gl.glVertex3f(vt[0] * size, vt[1] * size, vt[2] * size);//set coordinates
          vt = v[faces[i][1]];//choose the correct face
          gl.glTexCoord2f(textureX1, textureY2);//set the textures, clockwise 
          gl.glVertex3f(vt[0] * size, vt[1] * size, vt[2] * size);//set coordinates
          vt = v[faces[i][2]];//choose the correct face
          gl.glTexCoord2f(textureX2, textureY2);//set the textures, clockwise
          gl.glVertex3f(vt[0] * size, vt[1] * size, vt[2] * size);//set coordinates
          vt = v[faces[i][3]];//choose the correct face
          gl.glTexCoord2f(textureX2, textureY1);//set the textures, clockwise 
          gl.glVertex3f(vt[0] * size, vt[1] * size, vt[2] * size);//set coordinates
          gl.glEnd();
        }
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
    
    public void drawCylinder(float radius, float height, int steps, float textureX1, float textureX2, float textureY1, float textureY2){
        gl.glEnable(gl.GL_TEXTURE_2D);
        torso.bind(gl);
        //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, dimensions.w(), dimensions.h(), 0, GL_RGB, GL_UNSIGNED_BYTE, null);
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
        
        
        float deltaX = 720;//x size of texture file
        float deltaY = 2100;//y size of texture file    

        /*textureX1 /= deltaX;
        textureX2 /= deltaX;
        textureY1 /= deltaY;
        textureY2 /= deltaY;*/
        
        float delta = (textureX2 - textureX1) / (steps-1);//calculate size of texture step
        
        double step = (2* Math.PI) / steps;//calculate step sizes.
        gl.glBegin(gl.GL_QUADS);
        for(int i = 0; i<steps; i++){
            double x = Math.cos(step*i)*radius;//find x,y, next x, next y, and the points in middle.
            double y = Math.sin(step*i)*radius;
            double xn = Math.cos(step*(i+1))*radius;
            double yn = Math.sin(step*(i+1))*radius;
            double xnn = Math.cos(step*(i+0.5))*radius;
            double ynn = Math.sin(step*(i+0.5))*radius;
            
            Vector normal = new Vector(xnn,ynn,0).normalized();//normal found by subtracting 0,0,0 from point
            
            gl.glNormal3d(normal.x(), normal.y(), normal.z());//set the normal         
            gl.glTexCoord2f((textureX1+delta*i)/deltaX, textureY1/deltaY);//find the correct part of the texture (texture is devided into steps to spread it over cylinder face) 
            gl.glVertex3d(x,y,0);//set coordinates
            gl.glTexCoord2f((textureX1+delta*i)/deltaX, textureY2/deltaY); 
            gl.glVertex3d(x,y,height);
            gl.glTexCoord2f((textureX1+delta*(i+1))/deltaX, textureY2/deltaY); 
            gl.glVertex3d(xn,yn,height);
            gl.glTexCoord2f((textureX1+delta*(i+1))/deltaX, textureY1/deltaY); 
            gl.glVertex3d(xn,yn,0);
        }
        gl.glEnd();
        
        float dummyX = 650;//part of the texture without special stuff
        float dummyX2 = 700;//part of the texture without special stuff
        
        float diffX = (dummyX2 - dummyX);//calculate the difference
        float diffY = (textureY2 - textureY1);//calculate the y difference
        
        gl.glBegin(gl.GL_TRIANGLES);
        for(int i = 0; i<steps; i++){
            double x = Math.cos(step*i)*radius;//find x,y next x and next y coordinates.
            double y = Math.sin(step*i)*radius;
            double xn = Math.cos(step*(i+1))*radius;
            double yn = Math.sin(step*(i+1))*radius;
            gl.glNormal3d(0, 0, 1);//set the normal (midpoint to top, so positive)
            gl.glTexCoord2d((dummyX+0.5*diffX+0.5*diffX*Math.cos(step*i))/deltaX,(textureY1+0.5*diffY+0.5*diffY*Math.sin(step*i))/deltaY);//select correct part of texture  
            gl.glVertex3d(x,y,height);//set the coordinates
            gl.glTexCoord2d((dummyX+0.5*diffX+0.5*diffX*Math.cos(step*i+step))/deltaX,(textureY1+0.5*diffY+0.5*diffY*Math.sin(step*(i+1)))/deltaY); 
            gl.glVertex3d(xn,yn,height);
            gl.glTexCoord2d((dummyX+0.5*diffX)/deltaX,(textureY1+0.5*diffY)/deltaY); 
            gl.glVertex3d(0,0,height);
            
            gl.glNormal3d(0, 0, -1);//set the normal (midpoint to bottom, so negative).
            gl.glTexCoord2d((dummyX+0.5*diffX+0.5*diffX*Math.cos(step*i))/deltaX,(textureY1+0.5*diffY+0.5*diffY*Math.sin(step*i))/deltaY); 
            gl.glVertex3d(x,y,0);
            gl.glTexCoord2d((dummyX+0.5*diffX+0.5*diffX*Math.cos(step*i+step))/deltaX,(textureY1+0.5*diffY+0.5*diffY*Math.sin(step*(i+1)))/deltaY); 
            gl.glVertex3d(xn,yn,0);
            gl.glTexCoord2d((dummyX+0.5*diffX)/deltaX,(textureY1+0.5*diffY)/deltaY); 
            gl.glVertex3d(0,0,0);
        }
        gl.glEnd();
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
    
    /*
     * Different method for the torso, as it uses different order of textures (top and bottom have textures, instead of the curved part)
     */
    public void drawCylinderFront(float radius, float height, int steps, float textureX1, float textureX2, float textureY1, float textureY2){
        gl.glEnable(gl.GL_TEXTURE_2D);
        torso.bind(gl);
        //gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, dimensions.w(), dimensions.h(), 0, GL_RGB, GL_UNSIGNED_BYTE, null);
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
        
        
        float deltaX = 720;//x size of texture file
        float deltaY = 2100;//y size of texture file    

        float diffX = (textureX2 - textureX1);//difference
        float diffY = (textureY2 - textureY1);//difference
        
        double step = (2* Math.PI) / steps;//calculate the steps
        
        gl.glBegin(gl.GL_TRIANGLES);
        for(int i = 0; i<steps; i++){//for all steps
            double x = Math.cos(step*i)*radius;//calculate the x, y, next x and next y values
            double y = Math.sin(step*i)*radius;
            double xn = Math.cos(step*(i+1))*radius;
            double yn = Math.sin(step*(i+1))*radius;
            
            gl.glNormal3d(0, 0, 1);//set normal (midpoint -> top, +)
            gl.glTexCoord2d((textureX1+diffX*0.5+diffX*0.5*Math.cos(step*i))/deltaX, (textureY1 + diffY*0.5+ diffY*0.5*Math.sin(step*i))/deltaY);//find the position on the texture  
            gl.glVertex3d(x,y,height);//set the points
            gl.glTexCoord2d((textureX1+diffX*0.5+diffX*0.5*Math.cos(step*(i+1)))/deltaX, (textureY1 + diffY*0.5+ diffY*0.5*Math.sin(step*(i+1)))/deltaY);//find the position on the texture 
            gl.glVertex3d(xn,yn,height);
            gl.glTexCoord2d((textureX1+diffX*0.5)/deltaX, (textureY1 + diffY*0.5)/deltaY);//find the position on the texture
            gl.glVertex3d(0,0,height);//midpoint of top of cylinder
            
            gl.glNormal3d(0, 0, -1);//set normal (midpoin -> bottom, -)
            gl.glTexCoord2d((textureX1+diffX*0.5-diffX*0.5*Math.cos(step*i))/deltaX, (textureY1 + diffY*0.5+ diffY*0.5*Math.sin(step*i))/deltaY);//show texture mirrored  
            gl.glVertex3d(x,y,0);
            gl.glTexCoord2d((textureX1+diffX*0.5-diffX*0.5*Math.cos(step*(i+1)))/deltaX, (textureY1 + diffY*0.5+ diffY*0.5*Math.sin(step*(i+1)))/deltaY); 
            gl.glVertex3d(xn,yn,0);
            gl.glTexCoord2d((textureX1+diffX*0.5)/deltaX, (textureY1 + diffY*0.5)/deltaY);
            gl.glVertex3d(0,0,0);//midpoint of bottom of cylinder
        }
        gl.glEnd();
        
        float delta = (textureX2 - textureX1) / (steps-1);//texture step increase

        gl.glBegin(gl.GL_QUADS);
        for(int i = 0; i<steps; i++){
            double x = Math.cos(step*i)*radius;//calculate x,y, next x, next y, and the x and y in between.
            double y = Math.sin(step*i)*radius;
            double xn = Math.cos(step*i+step)*radius;
            double yn = Math.sin(step*i+step)*radius;
            double xnn = Math.cos(step*i+0.5*step)*radius;
            double ynn = Math.sin(step*i+0.5*step)*radius;
            
            Vector normal = new Vector(xnn,ynn,0).normalized();//calculate the normal vector, subtracting vector (0,0,0) from coordinates.
            
            gl.glNormal3d(normal.x(), normal.y(), normal.z());//set the normal         
            gl.glTexCoord2f((textureX1+delta*i+130)/deltaX, textureY1/deltaY);//get the location of the texture (only used for torso, so adding 130 gives us clean textures) 
            gl.glVertex3d(x,y,0);//set coordinates
            gl.glTexCoord2f((textureX1+delta*i+130)/deltaX, textureY2/deltaY);//get the location of the texture
            gl.glVertex3d(x,y,height);
            gl.glTexCoord2f((textureX1+delta*(i+1)+130)/deltaX, textureY2/deltaY);//get the location of the texture 
            gl.glVertex3d(xn,yn,height);
            gl.glTexCoord2f((textureX1+delta*(i+1)+130)/deltaX, textureY1/deltaY); //get the location of the texture
            gl.glVertex3d(xn,yn,0);
        }
        gl.glEnd();
       
        gl.glDisable(gl.GL_TEXTURE_2D);
    }
}
