
import javax.media.opengl.GL;
import static javax.media.opengl.GL2.*;
import static javax.media.opengl.fixedfunc.GLLightingFunc.GL_LIGHT0;
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
    private final Camera camera;
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
        robots = new Robot[4];

        // Initialize robot 0
        robots[0] = new Robot(Material.GOLD /* add other parameters that characterize this robot */);

        // Initialize robot 1
        robots[1] = new Robot(Material.SILVER /* add other parameters that characterize this robot */);

        // Initialize robot 2
        robots[2] = new Robot(Material.WOOD /* add other parameters that characterize this robot */);

        // Initialize robot 3
        robots[3] = new Robot(Material.ORANGE /* add other parameters that characterize this robot */);

        // Initialize the camera
        camera = new Camera();

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
        gl.glEnable(GL_LIGHT0);
        gl.glEnable(GL_LIGHT1);
        gl.glEnable(GL_NORMALIZE);

        // Try to load four textures, add more if you like.
        track = loadTexture("track.jpg");
        brick = loadTexture("brick.jpg");
        head = loadTexture("head.jpg");
        torso = loadTexture("torso.jpg");
    }

    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        // Select part of window.
        gl.glViewport(0, 0, gs.w, gs.h);

        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        // Set the perspective.
        // Modify this to meet the requirements in the assignment.
        float vHeight = gs.vWidth / ((float) gs.w / (float) gs.h);

        float fovY = (float) Math.atan2((0.5f * vHeight), gs.vDist) * 2f;
        fovY = (float) Math.toDegrees(fovY);

        glu.gluPerspective(fovY, (float) gs.w / (float) gs.h, 0.1 * gs.vDist, 100 * gs.vDist);

        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // Update the view according to the camera mode
        camera.update(gs.camMode);
    }

    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        /*gl.glBegin(gl.GL_LINES);
         gl.glColor4f(0f,0f,0f,1f);
        
         final double LENGTH_CONSTANT = gs.vDist / (2*Math.cos(gs.phi)*gs.vDist);
         System.out.println(LENGTH_CONSTANT);
         gl.glVertex3d(gs.cnt.x(),gs.cnt.y(),gs.cnt.z());
         gl.glVertex3d(-LENGTH_CONSTANT*Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y(),-LENGTH_CONSTANT*Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x(),0);
         gl.glVertex3d(LENGTH_CONSTANT*Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y(),LENGTH_CONSTANT*Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x(),0);
        
         /*gl.glVertex3d(-Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y(),-Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x(),0);
         gl.glVertex3d(Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y(),Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x(),0);
         System.out.println(Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y()+";"+Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x());
         /*gl.glVertex3d(-Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x(),-Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y(),0);
         gl.glVertex3d(Math.cos(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.x(),Math.sin(gs.theta)*Math.cos(gs.phi)*gs.vDist+gs.cnt.y(),0);
         gl.glEnd();*/

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Draw the axis frame
        if (gs.showAxes) {
            drawAxisFrame();
        }

        // Set color to black.
        gl.glColor3f(0f, 0f, 0f);
         
        // Draw the first robot
        robots[0].draw(gs.showStick);
        
        gl.glTranslatef(1f, 0f, 0f);
        
        robots[1].draw(gs.showStick);
        
        gl.glTranslatef(1f, 0f, 0f);
        
        robots[2].draw(gs.showStick);
        
        gl.glTranslatef(1f, 0f, 0f);
        
        robots[3].draw(gs.showStick);

        // Draw race track
        raceTrack.draw(gs.trackNr);

        // Draw terrain
        terrain.draw();
/*
        // Unit box around origin.
        glut.glutWireCube(1f);

        // Move in x-direction.
        gl.glTranslatef(2f, 0f, 0f);

        // Rotate 30 degrees, around z-axis.
        gl.glRotatef(30f, 0f, 0f, 1f);

        // Scale in z-direction.
        gl.glScalef(1f, 1f, 2f);

        // Translated, rotated, scaled box.
        glut.glutWireCube(1f);*/
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue), and origin
     * (yellow).
     */
    public void drawAxisFrame() {
        final float CONE_LENGTH = 0.25f;
        final float AXIS_LENGTH = 1f;
        final float LINE_LENGTH = AXIS_LENGTH - CONE_LENGTH;
        int[][] directionVectors = {{1, 0, 0}, {0, 1, 0}, {0, 0, 1}};

        final float LINE_WIDTH = 0.015f;

        for (int[] direction : directionVectors) {
            gl.glPushMatrix();
            gl.glColor4f(direction[0] * 255, direction[1] * 255, direction[2] * 255, 1f);
            gl.glScalef(LINE_LENGTH * direction[0] + LINE_WIDTH, LINE_LENGTH * direction[1] + LINE_WIDTH, LINE_LENGTH * direction[2] + LINE_WIDTH);
            gl.glTranslatef(0.5f * direction[0], 0.5f * direction[1], 0.5f * direction[2]);
            glut.glutSolidCube(1f);
            gl.glPopMatrix();

            gl.glPushMatrix();
            gl.glTranslated(LINE_LENGTH * direction[0], LINE_LENGTH * direction[1], LINE_LENGTH * direction[2]);
            gl.glRotatef(90f, 0f - direction[1], direction[0], direction[2]);
            glut.glutSolidCone(0.2 * CONE_LENGTH, CONE_LENGTH, 50, 51);
            gl.glPopMatrix();
        }

        gl.glPushMatrix();
        gl.glColor4f(255, 255, 0, 1);
        glut.glutSolidSphere(0.05f, 50, 51);
        gl.glPopMatrix();
    }

    /**
     * Materials that can be used for the robots.
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
        new float[]{1f, 0.5f, 0f, 1.0f});
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
    }

    /**
     * Represents a Robot, to be implemented according to the Assignments.
     */
    private class Robot {
        class GlColor {
            float r, g, b, a;
            GlColor(float r, float g, float b, float a) {
                this.r = r;
                this.g = g;
                this.b = b;
                this.a = a;
            }

            GlColor(float r, float g, float b) {
                this(r, g, b, 1.f);
            }

            void set(GL2 gl) {
                gl.glColor4f(r, g, b, a);
            }
        }

        /**
         * The material from which this robot is built.
         */
        private final Material material;

        GlColor baseColor       = new GlColor(1.0f, 0.2f, 0.3f);
        GlColor headColor       = new GlColor(0.4f, 0.4f, 0.4f);
        GlColor neckColor       = new GlColor(0.5f, 0.4f, 0.4f);
        GlColor shoulderColor   = new GlColor(0.4f, 0.4f, 0.4f);
        GlColor chestColor      = new GlColor(0.4f, 0.4f, 0.4f);
        GlColor hipColor        = new GlColor(0.0f, 0.4f, 0.4f);

        GlColor legColor        = new GlColor(0.f, 0.f, 1.f);

        GlColor [] singleLegColor = new GlColor[] {
            legColor,
            legColor
        };
        GlColor [][] legPartColor = new GlColor[][] {
            singleLegColor,
            singleLegColor,
        };

        GlColor [][] legJointColor = new GlColor[][] {
            singleLegColor,
            singleLegColor,
        };

        GlColor [] footColor = new GlColor[] {
            legColor,
            legColor,
        };
        /**
         * Constructs the robot with initial parameters.
         */
        public Robot(Material material /* add other parameters that characterize this robot */) {
            this.material = material;

            // code goes here ...
        }

        /**
         * Draws this robot (as a {@code stickfigure} if specified).
         */
        public void draw(boolean stickFigure) {

            final float VAKJE                   = 0.1f;
            final float SHOULDER_OVERLAP_MAGIC  = 1.f;

            final float TORSO_HEIGHT            = 5     *VAKJE;
            final float TORSO_THICKNESS         = 1.5f  *VAKJE;
            final float SHOULDER_HEIGHT         = 2     *VAKJE;
            final float SHOULDER_WIDTH          = TORSO_HEIGHT;
            final float NECK_HEIGHT             = 1     *VAKJE;
            final float NECK_WIDTH              = 0.5f  *VAKJE;
            final float HEAD_HEIGHT             = 3     *VAKJE;
            final float HEAD_WIDTH              = 2     *VAKJE;
            final float SHOUlDER_JOINT_HEIGHT   = 1     *VAKJE;
            final float SHOULDER_JOINT_WIDTH    = 1.3f  *VAKJE;
            final float ARM_PART_LENGTH         = 4     *VAKJE;
            final float LEG_PART_LENGTH         = 5     *VAKJE;
            final float TORSO_BOTTOM_HEIGHT     = 1.5f  *VAKJE;
            final float TORSO_BOTTOM_WIDTH      = 0.95f *TORSO_HEIGHT;
            final float FEET_LENGTH             = 2.f   *VAKJE; // TODO look up
            
            final int PRECISION               = 40;
            final int PRECISION2               = PRECISION+1;
            
            
            final float ARM_WIDTH               = SHOULDER_JOINT_WIDTH * 0.8f;
            final float ELBOW_JOINT_WIDTH       = SHOULDER_JOINT_WIDTH;
            final float ARM_HEIGHT             = ARM_WIDTH * 0.8f;
            
            final float LEG_WIDTH               = ARM_WIDTH;
            final float KNEE_JOINT_WIDTH        = ELBOW_JOINT_WIDTH;
            final float KNEE_JOINT_HEIGHT       = SHOUlDER_JOINT_HEIGHT;
            final float LEG_HEIGHT              = LEG_WIDTH * 0.8f;
            final float FEET_HEIGHT             = KNEE_JOINT_HEIGHT;
            final float FEET_WIDTH              = LEG_WIDTH;
            
            final float TORSO_RELATIVE_HEIGHT = 2*LEG_PART_LENGTH+TORSO_HEIGHT/2+TORSO_BOTTOM_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC)+KNEE_JOINT_HEIGHT/2; 
            
//             gl.glPolygonMode( gl.GL_FRONT_AND_BACK, gl.GL_LINE );
            
            gl.glPushMatrix();
                gl.glTranslatef(0.f, 0.f, TORSO_RELATIVE_HEIGHT);
                gl.glPushMatrix();//chest
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

                    baseColor.set(gl);
                    gl.glTranslatef(0.f, 0.f, -TORSO_BOTTOM_HEIGHT/(2+SHOULDER_OVERLAP_MAGIC));
                    for(int i = 0; i < 2; i++)
                    {
                        gl.glPushMatrix();
                            gl.glTranslatef(0.f, 0.f, -TORSO_HEIGHT/2);
                            if(i == 1) gl.glScalef(-1.f, 1.f, 1.f);
                            gl.glTranslatef(TORSO_BOTTOM_WIDTH/2-3*LEG_WIDTH/4,0.f,0.f);
                            for(int j = 0; j < 2; j++)
                            {
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
                            }
                            //TODO gl.glRotatef(45, 1.f, 0.f, 0.f);
                            gl.glRotatef(90, 0.f, 0.f, 1.f);
                            gl.glTranslatef(0.f, -KNEE_JOINT_WIDTH/2, -KNEE_JOINT_HEIGHT/2);
                            gl.glScalef(FEET_LENGTH, FEET_WIDTH, FEET_HEIGHT);
                            /* z=1  __ ==\
                             *  \==--      \
                             *  | \   ---    \
                             *  |   \    ---   \
                             *  |     \      --==\
                             *  |       \   __
                             *  =========/==
                             *  O        x=1
                             */
                            footColor[i].set(gl);
                            gl.glBegin(stickFigure ? gl.GL_LINE_STRIP : gl.GL_TRIANGLE_STRIP);
                                // Left side
                                gl.glVertex3f(0.f, 0.f, 0.f);
                                gl.glVertex3f(1.f, 0.f, 0.f);
                                gl.glVertex3f(0.f, 0.f, 1.f);

                                //Front quad
                                gl.glVertex3f(1.f, 1.f, 0.f);
                                gl.glVertex3f(0.f, 1.f, 1.f);

                                //Right side
                                gl.glVertex3f(0.f, 1.f, 0.f);

                                //Back side
                                gl.glVertex3f(0.f, 0.f, 1.f);
                                gl.glVertex3f(0.f, 0.f, 0.f);
                                gl.glVertex3f(0.f, 1.f, 0.f);

                                //Bottom side
                                gl.glVertex3f(1.f, 0.f, 0.f);
                                gl.glVertex3f(1.f, 1.f, 0.f);

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
                
                for(int i = 0; i < 2; i++)
                {
                    gl.glPushMatrix();
                        if(i == 1) gl.glScalef(-1.f, 1.f, 1.f);
                        gl.glTranslatef(SHOULDER_WIDTH/2, 0.f, 0.f);
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
                        if(stickFigure) {
                            gl.glBegin(gl.GL_LINES);
                                gl.glVertex3f(0.f, 0.f, -SHOUlDER_JOINT_HEIGHT/3);
                                gl.glVertex3f(0.f, 0.f, SHOUlDER_JOINT_HEIGHT/3);
                            gl.glEnd();
                        } else {
                            glut.glutSolidSphere(SHOUlDER_JOINT_HEIGHT/3, PRECISION, PRECISION2); //TODO: give seperate variable
                        }
                        gl.glTranslatef(-SHOULDER_JOINT_WIDTH/2, 0.f, 0.f);
                        for(int j = 0; j < 2; j++)
                        {
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
                            gl.glTranslatef(0.f, 0.f, -ARM_PART_LENGTH);
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
            
//             gl.glPolygonMode( gl.GL_FRONT_AND_BACK, gl.GL_FILL );
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

        /**
         * Updates the camera viewpoint and direction based on the selected
         * camera mode.
         */
        public void update(int mode) {
            robots[0].toString();

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
                // code goes here...
                // Default mode
            } else {
                setDefaultMode();
            }
            
            double X_EYE_COR = gs.vDist * Math.cos(gs.phi) * Math.sin(gs.theta) + gs.cnt.x();
            double Y_EYE_COR = gs.vDist * Math.cos(gs.phi) * Math.cos(gs.theta) + gs.cnt.y();
            double Z_EYE_COR = gs.vDist * Math.sin(gs.phi) + gs.cnt.z();
            glu.gluLookAt(X_EYE_COR, Y_EYE_COR, Z_EYE_COR, gs.cnt.x(), gs.cnt.y(), gs.cnt.z(), 0, 0, 1);
            
            
            /** -- WARNING: the credit for the below code for the lighting goes to rene zaal -- **/
            /* rene zaal has given us credit to make use of this code */
            
            // draw a light above and to the left of the camera
            // calculate the direction in which the camera looks in the xy plane 
            Vector xyCameraDir = (new Vector(eye.subtract(center).x(), eye.subtract(center).y(), 0)).normalized();

            // take the cross product of that vector with the up vector to get a vector orthogonal to the direction vector in the xyplane
            Vector light1 = xyCameraDir.cross(up).normalized();

            // now we can look correct the vector if it points to the right instead of to the left
            // this part is only easy to explain when you visualize it on paper
            // basically we compare the x coordinates to find out whether light1 is pointing to the left or right
            // a switch happens at y=-x
            if (xyCameraDir.y() > -xyCameraDir.x()) {
                if (light1.x() > xyCameraDir.x()) {
                    light1 = Vector.O.subtract(light1);
                }
            } else if (xyCameraDir.y() < -xyCameraDir.x()) {
                if (light1.x() < xyCameraDir.x()) {
                    light1 = Vector.O.subtract(light1);
                }
            } else if (light1.x() != xyCameraDir.x()) {
                light1 = Vector.O.subtract(light1);
            }

            light1 = light1.add(eye);

            float light1co[] = new float[]{(float) light1.x(), (float) light1.y(), (float) light1.z(), 1.0f};

            // activate the spot
            gl.glLightfv(GL_LIGHT1, GL_POSITION, light1co, 0);
            if (mode != 0) {
                glu.gluLookAt(eye.x(), eye.y(), eye.z(), center.x(), center.y(), center.z(), up.x(), up.y(), up.z());
            }
            /** --------------------------------------------------- **/
        }   

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * camera's default mode.
         */
        private void setDefaultMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * helicopter mode.
         */
        private void setHelicopterMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * motorcycle mode.
         */
        private void setMotorCycleMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based on the
         * first person mode.
         */
        private void setFirstPersonMode() {
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

        /**
         * Constructs the race track, sets up display lists.
         */
        public RaceTrack() {
            // code goes here ...
        }

        /**
         * Draws this track, based on the selected track number.
         */
        public void draw(int trackNr) {

            // The test track is selected
            if (0 == trackNr) {
                // code goes here ...
                // The O-track is selected
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

        /**
         * Returns the position of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getPoint(double t) {
            return Vector.O; // <- code goes here
        }

        /**
         * Returns the tangent of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getTangent(double t) {
            return Vector.O; // <- code goes here
        }
    }

    /**
     * Implementation of the terrain.
     */
    private class Terrain {

        /**
         * Can be used to set up a display list.
         */
        public Terrain() {
            // code goes here ...
        }

        /**
         * Draws the terrain.
         */
        public void draw() {
            // code goes here ...
        }

        /**
         * Computes the elevation of the terrain at ({@code x}, {@code y}).
         */
        public float heightAt(float x, float y) {
            return 0; // <- code goes here
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
}
