/**
 * RobotRace
 */
import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
            new Robot(Material.GOLD).setNeckModifier(2.f).setSpeed(50f),

            // Instantiate bender, kiss my shiny metal ass
            new Robot(Material.SILVER).setSpeed(56f),

            // Instantiate oldschool robot
            new Robot(Material.WOOD).setNeckModifier(0.5f).setSpeed(51f),

            // Hey look at me, I'm an annoying orange robot!
            new Robot(Material.ORANGE).setSpeed(53f)
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
            torso = loadTexture("torso.jpg");
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
                    Vector screenPosition = raceTrack.getPoint(0).add(new Vector(0, 0, 2));
                    Vector outerScreenPosition = raceTrack.getOuter(screenPosition);
                    Vector screenDelta = outerScreenPosition.subtract(screenPosition);
                    gl.glTranslated(screenPosition.x(), screenPosition.y(), screenPosition.z());
                    gl.glScaled(
                        screenDelta.x(),
                        textureDimensions.w()/textureDimensions.w()*screenDelta.x(),
                        1.f
                    );
                    gl.glBegin(GL_QUADS);
                        gl.glTexCoord2d(0.f, 0.f); gl.glVertex3f(0.f, 0.f, 0.f);
                        gl.glTexCoord2d(1.f, 0.f); gl.glVertex3f(1.f, 0.f, 0.f);
                        gl.glTexCoord2d(1.f, 1.f); gl.glVertex3f(1.f, 0.f, 1.f);
                        gl.glTexCoord2d(0.f, 1.f); gl.glVertex3f(0.f, 0.f, 1.f);
                    gl.glEnd();
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
        public Robot(Material material) {
            setDefaultMaterial(material);

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
                        glut.glutSolidCylinder(TORSO_HEIGHT/2, TORSO_THICKNESS, PRECISION, PRECISION2);
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
                            glut.glutSolidCylinder(TORSO_BOTTOM_HEIGHT/2, TORSO_BOTTOM_WIDTH, PRECISION, PRECISION2);
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
                                        glut.glutSolidCube(1.f);
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
                                        glut.glutSolidCylinder(KNEE_JOINT_HEIGHT/2, KNEE_JOINT_WIDTH, PRECISION, PRECISION2);
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
                             gl.glBegin(stickFigure ? gl.GL_LINE_STRIP : gl.GL_TRIANGLE_STRIP);
                                // Left side
                                gl.glNormal3f(0.f, -1.f, 0.f); gl.glVertex3f(0.f, 0.f, 0.f);
                                gl.glNormal3f((float)Math.sqrt(2), -1.f, (float)Math.sqrt(2)); gl.glVertex3f(1.f, 0.f, 0.f);
                                gl.glNormal3f((float)Math.sqrt(2), -1.f, (float)Math.sqrt(2)); gl.glVertex3f(0.f, 0.f, 1.f);

                                //Front quad
                                gl.glNormal3f((float)Math.sqrt(2), 1.f, (float)Math.sqrt(2)); gl.glVertex3f(1.f, 1.f, 0.f);
                                gl.glNormal3f((float)Math.sqrt(2)-1.f, 1.f, (float)Math.sqrt(2)); gl.glVertex3f(0.f, 1.f, 1.f);

                                //Right side
                                gl.glNormal3f(-1.f, 1.f, 0.f); gl.glVertex3f(0.f, 1.f, 0.f);

                                //Back side
                                gl.glNormal3f(-1.f, 0.f, 0.f); gl.glVertex3f(0.f, 0.f, 1.f);
                                gl.glNormal3f(-1.f, 0.f, -1.f); gl.glVertex3f(0.f, 0.f, 0.f);
                                gl.glNormal3f(-1.f, 0.f, -1.f); gl.glVertex3f(0.f, 1.f, 0.f);

                                //Bottom side
                                gl.glNormal3f(0.f, 0.f, -1.f); gl.glVertex3f(1.f, 0.f, 0.f);
                                gl.glNormal3f(0.f, 0.f, -1.f); gl.glVertex3f(1.f, 1.f, 0.f);

                                // Stick figure needs additional vertexes
                                if(stickFigure) {
                                    gl.glVertex3f(1.f, 0.f, 0.f);
                                    gl.glVertex3f(1.f, 1.f, 0.f);
                                    gl.glVertex3f(0.f, 1.f, 0.f);
                                    gl.glVertex3f(0.f, 1.f, 1.f);
                                    gl.glVertex3f(0.f, 0.f, 1.f);
                                }
                            gl.glEnd();
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
                        glut.glutSolidCylinder(SHOULDER_HEIGHT/2, SHOULDER_WIDTH, PRECISION, PRECISION2);
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
                        glut.glutSolidCylinder(NECK_WIDTH/2 ,NECK_HEIGHT+SHOULDER_HEIGHT/2, PRECISION, PRECISION2);
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
                        glut.glutSolidCylinder(HEAD_WIDTH/2, HEAD_HEIGHT, PRECISION, PRECISION2);
                    }
                gl.glPopMatrix();

                // Draw the arms
                for(int i = 0; i < 2; i++)
                {
                    gl.glPushMatrix();
                        shoulderColor.set(gl);

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
                                glut.glutSolidCylinder(SHOUlDER_JOINT_HEIGHT/2, SHOULDER_JOINT_WIDTH, PRECISION, PRECISION2);
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
                                    glut.glutSolidCube(1.f);
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
                                    glut.glutSolidCylinder(SHOUlDER_JOINT_HEIGHT/2, ELBOW_JOINT_WIDTH, PRECISION, PRECISION2);
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
        public Vector eye = new Vector(3f, 6f, 5f);
        /**
         * The point to which the camera is looking.
         */
        public Vector center = Vector.O;
        /**
         * The up vector.
         */
        public Vector up = Vector.Z;

        private int mode = 0;
        private int robot = 0;

        public float fov = (float)Math.PI/2.f;

        public FrameBuffer frameBuffer = null;
        private float viewingDistance = 12;

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
                setViewingDistance(10);
                if(this == mainCamera) {
                    gs.vWidth = 10+(int)(Math.random()*10);
                }

            // Motor cycle mode
            } else if (2 == mode) {
                setViewingDistance(5);

                if(this == mainCamera) {
                    gs.vWidth = 10+(int)(Math.random()*10);
                }

            // First person mode
            } else if (3 == mode) {
                if(this == mainCamera) {
                    gs.vWidth = 10+(int)(Math.random()*10);
                }

                for(int i = 0; i < robots.length ; i++) {
                    float speed = robots[i].getSpeed();
                    if(speed>=robots[robotFPV].getSpeed()){
                        robotFPV = i;
                    }
                }

            // Auto mode
            } else if (4 == mode) {
                setViewingDistance(12);
                startTime = System.currentTimeMillis();
                for(int i = 0; i < robots.length ; i++) {
                    float speed = robots[i].getSpeed();

                    if(speed>=robots[robotFPV].getSpeed()) {
                        robotFPV = i;
                    }
                }
            // Default mode
            } else {
                setViewingDistance(25);
            }

            robot = (int)(Math.random()*robots.length);
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
                if(System.currentTimeMillis() - startTime < interval){
                    setHelicopterMode();//number from 0-3, to choose a robot.
                }
                else if(System.currentTimeMillis() - startTime < 2*interval){
                    setMotorCycleMode();
                }
                else if(System.currentTimeMillis() - startTime < 3*interval){
                    setFirstPersonMode();
                }
                else{
                    startTime = System.currentTimeMillis();
                    setHelicopterMode();//number from 0-3, to choose a robot.
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
            float t = gs.tAnim/robots[robot].getSpeed();
            Vector position = raceTrack.getPoint(t);
            Vector tangent = raceTrack.getTangent(t);
            position = position.add(tangent.cross(Vector.Z).normalized().scale(0.5f+robot));
            
            center = position;
            //eye = robots[0].getOrientation().normalized().add(up);
            eye = center.add(up.scale(10)).add(Vector.X.normalized());
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * motorcycle mode.
         */
        private void setMotorCycleMode() {
            float t = gs.tAnim/robots[robot].getSpeed();
            Vector position = raceTrack.getPoint(t);      
            Vector tangent = raceTrack.getTangent(t);
            position = position.add(tangent.cross(Vector.Z).normalized().scale(0.5f+robot));
                    
            center = position.add(Vector.Z.scale(1.5));       
            eye = center.add(tangent.cross(Vector.Z).normalized().scale(-1.75*(robot+1)));
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * first person mode.
         */
        private void setFirstPersonMode() {
            float t = gs.tAnim/robots[robotFPV].getSpeed();
            Vector position = raceTrack.getPoint(t);
            Vector tangent = raceTrack.getTangent(t);
            position = position.add(tangent.cross(Vector.Z).normalized().scale(0.5f+robotFPV));
            
            eye = position.add(Vector.Z.scale(2));        
            center = eye.add(tangent.normalized().scale(0.5));
            // code goes here ...
        }
    }

    /**
     * Implementation of a race track that is made from Bezier segments.
     */
    private class RaceTrack {

        /**
         * Array with control points for the O-track.
         */
        private Vector[] controlPointsOTrack;
        /**
         * Array with control points for the L-track.
         */
        private Vector[] controlPointsLTrack;
        /**
         * Array with control points for the C-track.
         */
        private Vector[] controlPointsCTrack;
        /**
         * Array with control points for the custom track.
         */
        private Vector[] controlPointsCustomTrack;

        private int trackNr = 0;
        /**
         * Constructs the race track, sets up display lists.
         */
        public RaceTrack() {
            // code goes here ...
        }

        float x = 0.f;
        /**
         * Draws this track, based on the selected track number.
         */
        
        public void draw(int trackNr) {
            this.trackNr = trackNr;
            x+=0.2;
            
            // The test track is selected
            if (0 == trackNr) {
                final double STEP = 0.01;
                for(int j = 0; j < 4; j++) {
                    if(j == 0) {
                        gl.glEnable(gl.GL_TEXTURE_2D);

                        track.bind(gl);

                        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
                        gl.glTexParameteri( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
                    }

                    gl.glBegin(gl.GL_TRIANGLE_STRIP);
                        for(double i = -3*STEP; i <= 1; i += STEP) {
                            Vector initialPoint = getPoint(i),
                                   point = initialPoint;
                            if(j == 1) {
                                point = getLower(point);
                            } else if (j == 2) {
                                point = getOuter(point);
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

                            Vector outerPoint = (j == 2 || j == 3) ? getLower(point) : getOuter(point);
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
            } else if (1 == trackNr) {
                // code goes here ...
                // The L-track is selected
            } else if (2 == trackNr) {
                // code goes here ...
                // The C-track is selected
            } else if (3 == trackNr) {
                // code goes here ...
                // The custom track is selected
            } else if (4 == trackNr) {
                // code goes here ...
            }
        }
        
        Vector getLower(Vector initialPosition) {
            return initialPosition.add(new Vector(0, 0, -2));
        }
        
        Vector getOuter(Vector initialPosition) {
            Vector directionVector = initialPosition.normalized().scale(4);
            return initialPosition.add(new Vector(directionVector.x(), directionVector.y(), 0));
        }

        /**
         * Returns the position of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getPoint(double t) {
            return new Vector(10*Math.cos(2* Math.PI * t),14*Math.sin(2* Math.PI * t),1);
        }

        /**
         * Returns the tangent of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getTangent(double t) {
            return new Vector(-20*Math.PI*Math.sin(2*Math.PI * t),28*Math.PI*Math.cos(2*Math.PI * t),0);
        }
        
        public boolean isOnTrack(float x, float y){
            if(trackNr==0){
                float f = pointFunctionalValue(x, y);
                if(f<2.1 && f>0.8){
                    return true;
                }
                return false;
            }
            else{
                return false;
            }    
        }
        
        public float pointFunctionalValue(float x, float y){
            if(trackNr==0){
                return (float)(Math.pow(x,2)/100+Math.pow(y,2)/196);
            }
            else{
                return 1;
            }
        }
        
        public float changeHeight(float x, float y, float height){
            if(height > 0.5){
                if(!raceTrack.isOnTrack(x,y)){
                    float f = pointFunctionalValue(x,y);
                    if(f<0.8 && f>0.2){
                         height = height*0.5f;      
                    } 
                    else if (f>2.1 && f<3){
                         height = height*0.5f; 
                    }
                }
                else{
                    height = 0.5f;
                }
            }
            return height;
        }
    }

    /**
     * Implementation of the terrain.
     */
    private class Terrain {
        float gridSize = 25;
        float step = 0.5f;
        float waterHeight = 0f;
        int treeCount = 15;
        PerlinNoise perlin;    
        int OneDColorId = -1;
        ArrayList<Tree> trees;
        float terrainHeightLevel = 5.0f;
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
        
        /**
         * Can be used to set up a display list.
         */
        public Terrain() {
            perlin = new PerlinNoise(123332321, 4, 5.0);
            generateTrees();
                   
        }
        
        public void generateTrees(){
           trees = new ArrayList<Tree>();
            for(int i = 0; i < treeCount ; i++){
                float x = 0;
                float y = 0;
                float z = 0;
                while(z < 0.5f){
                    x = (float)(gridSize*(1-Math.random()*2));
                    y = (float)(gridSize*(1-Math.random()*2));
                    if(!raceTrack.isOnTrack(x,y)){
                       z = heightAt(x, y); 
                    }
                }
                trees.add(new Tree(x,y,z));//random positions.
            }   
        }

        /**
         * Draws the terrain.
         */
        public void draw() {
            OneDColorId = OneDColorId == -1 ? create1DTexture() : OneDColorId;
            RobotRace.Material.BLANK.set(gl);
            gl.glEnable(GL_TEXTURE_1D);
                gl.glBindTexture(GL_TEXTURE_1D, OneDColorId);
                for(float x = -gridSize;x<=gridSize;x+=step)
                {
                    for(float y = -gridSize;y<=gridSize;y+=step)
                    {
                        float lowerLeftCorner = heightAt(x,y);
                        float lowerRightCorner = heightAt(x+step,y);
                        float upperLeftCorner = heightAt(x,y+step);
                        float upperRightCorner = heightAt(x+step,y+step);

                        /*
                        *             ulc - - - - - - urc
                        *              |             / |
                        *              |    diag  /    |
                        *     vertical |       /       |
                        *              |    /          |
                        *              | /             |
                        *             llc - - - - - - lrc
                        *                  horizontal
                        */

                        Vector diagonal = new Vector(step, step, upperRightCorner-lowerLeftCorner);
                        Vector horizontal = new Vector(step,0, lowerRightCorner-lowerLeftCorner);
                        Vector vertical = new Vector(0, step, upperLeftCorner-lowerLeftCorner);

                        Vector normal = getNormal(diagonal,horizontal);

                        gl.glBegin(GL_TRIANGLES);
                            gl.glNormal3d(normal.x(), normal.y(), normal.z());//set the normal for this triangle

                            setColorAtHeight(lowerLeftCorner);
                            gl.glVertex3d(x, y, lowerLeftCorner);

                            setColorAtHeight(lowerRightCorner);
                            gl.glVertex3d(x + step, y, lowerRightCorner);

                            setColorAtHeight(upperRightCorner);
                            gl.glVertex3d(x + step, y + step, upperRightCorner);
                        gl.glEnd();

                        normal = getNormal(diagonal,vertical);

                        gl.glBegin(GL_TRIANGLES);
                            gl.glNormal3d(normal.x(), normal.y(), normal.z());//set the normal for this triangle

                            setColorAtHeight(lowerLeftCorner);
                            gl.glVertex3d(x, y, lowerLeftCorner);

                            setColorAtHeight(upperLeftCorner);
                            gl.glVertex3d(x, y + step, upperLeftCorner);

                            setColorAtHeight(upperRightCorner);
                            gl.glVertex3d(x + step, y + step, upperRightCorner);
                        gl.glEnd();
                    }
                }
            gl.glDisable(GL_TEXTURE_1D);
            
            gl.glEnable(GL_BLEND);
            gl.glBlendFunc(GL_ONE_MINUS_SRC_ALPHA, GL_SRC_ALPHA);
                gl.glBegin(GL_QUADS);
                    RobotRace.Material.WATER.set(gl);
                    gl.glVertex3d(-gridSize,-gridSize,waterHeight);
                    gl.glVertex3d(gridSize,-gridSize,waterHeight);
                    gl.glVertex3d(gridSize,gridSize,waterHeight);
                    gl.glVertex3d(-gridSize,gridSize,waterHeight);
                gl.glEnd();
            gl.glDisable(GL_BLEND);
            
            for(int i = 0; i < treeCount; i++){
                trees.get(i).draw();
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
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(colors.length*4).order(ByteOrder.nativeOrder());
            for(Color color: colors){
                int pixel = color.getRGB();
                byteBuffer.put((byte)((pixel >>> 16) & 0xFF));//Red component
                byteBuffer.put((byte)((pixel >>> 8) & 0xFF));//Green component
                byteBuffer.put((byte)(pixel & 0xFF));//Blue component
                byteBuffer.put((byte)(pixel >>> 24));//Alpha component
            }
            byteBuffer.flip();

            gl.glEnable(GL_TEXTURE_1D);
                gl.glBindTexture(GL_TEXTURE_1D, textureId);
                gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA, colors.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, byteBuffer);
                gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
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
        
        public float heightCorrection(float x, float y, float z){
            z=raceTrack.changeHeight(x, y, z);
            return z;
        }
        
        public void setColorAtHeight(float z){
            float max = ((colors.length)/2.0f)-0.5f;
                
            if(z > max*2){
                z = max-1f;
                z += 1f;
                z /= max*2+1f;//get a number between 0 and 1, to avoid repeating texture
                gl.glTexCoord1d(z);
            }
            else if(z < -0.5f){
                z = -0.45f;
                z += 0.5f;
                z /= max+0.5f;//get a number from 0 to 1, to avoid repeating texture
                gl.glTexCoord1d(z);
            }
            else{        
                z += 1f;
                z /= max*2+1f;//get a number between 0 and 1, to avoid repeating texture
                gl.glTexCoord1d(z);
            }
        }
        
        public Vector getNormal(Vector a, Vector b){
            Vector n = a.cross(b);//cross product of the two vectors is the normal of the plane the two vectors represent.
            if(n.z()<0){
                n = n.scale(-1);//wrong direction, make it face the other way
            }
            return n.normalized();
        }
    }
    
    private class Tree {
        //Types: pine, oak?
        int levels;
        float logHeight;
        float leafHeight;
        float treeWidth;
        float logWidth;
        float offset;
        float x;
        float y;
        float z;
        int precision = 20;
        
        public Tree(float x, float y, float z){
            levels = 3+Math.round((float)Math.random()*3);
            logHeight = 0.2f +(float)Math.random()*0.5f;
            treeWidth = 1.5f + (0.10f+(float)Math.random()*0.10f)*levels;
            logWidth = 0.2f + (float)Math.random()*0.2f;
            leafHeight = (0.75f + (float)Math.random()*0.4f)*levels;
            offset = leafHeight/((levels*2)-1);
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public void draw(){
            gl.glPushMatrix();
                gl.glTranslatef(x, y, 0);
                RobotRace.Material.WOOD.set(gl);
                glut.glutSolidCylinder(logWidth/2, z+logHeight+0.1f, 50, 51);
                gl.glTranslatef(0, 0, z+logHeight);
                RobotRace.Material.LEAF.set(gl);
                for(int i=0; i<levels; i++){
                    glut.glutSolidCone((1-(i*offset)/(logHeight+leafHeight))*(treeWidth/2), offset*2, precision, precision+1);
                    gl.glTranslatef(0, 0, offset);
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
}
