/**
 * RobotRace
 */
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
        robots = new Robot[] {
            /// Instantiate swag robot
            new Robot(Material.GOLD).setNeckModifier(2.f),

            // Instantiate bender, kiss my shiny metal ass
            new Robot(Material.SILVER),

            // Instantiate oldschool robot
            new Robot(Material.WOOD).setNeckModifier(0.5f),

            // Hey look at me, I'm an annoying orange robot!
            new Robot(Material.ORANGE)
        };

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

        float aspectRatio = (float) gs.w / (float) gs.h;

        // Calculate the view height from the aspect ratio
        float vHeight = gs.vWidth / aspectRatio;

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
        float fovY = (float)Math.atan(
            vHeight/(
                2.f * gs.vDist
            )
        ) * 2.f;

        // Initialize perspective from calculated values
        glu.gluPerspective(
            (float) Math.toDegrees(fovY),
            aspectRatio,
            0.1 * gs.vDist,
            100 * gs.vDist
        );

        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // Update the view according to the camera mode
        camera.update(gs.camMode);

        // Calculate the eye position from the center point and viewing angles
        Vector eyePosition = new Vector (
            gs.vDist * Math.cos(gs.phi) * Math.cos(gs.theta) + gs.cnt.x(),
            gs.vDist * Math.cos(gs.phi) * Math.sin(gs.theta) + gs.cnt.y(),
            gs.vDist * Math.sin(gs.phi) + gs.cnt.z());

        // Initializing the viewing matrix
        glu.gluLookAt(
            eyePosition.x(),    eyePosition.y(),    eyePosition.z(),
            gs.cnt.x(),         gs.cnt.y(),         gs.cnt.z(),
            camera.up.x(),      camera.up.y(),      camera.up.z());

        // Update the light
        {
            // Calculate the direction that is being looked at
            Vector viewDirection    = eyePosition.subtract(gs.cnt);

            // Calculate a vector to the left (relative to the eye)
            Vector leftDirection    = viewDirection.cross(camera.up).normalized();

            // Calculate a vector upwards (relative to the eye)
            Vector upDirection      = leftDirection.cross(viewDirection).normalized();

            // Calculate the direction the light is relative to the camera
            Vector leftUpDirection  = leftDirection.add(upDirection)
                                                   .normalized()
                                                   .scale(1.1f);

            // Calculate the position of the light
            Vector leftUp = eyePosition.add(leftUpDirection);

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
        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Draw the axis frame
        if (gs.showAxes) {
            // Axes should not be affected by light
            gl.glDisable(GL_LIGHTING);
            drawAxisFrame();
            gl.glEnable(GL_LIGHTING);
        }

        // Draw all robots
        int i = 0;
        for(Robot bob : robots) {
            gl.glPushMatrix();
                // Draw bob, all our robots are named bob
                float t = gs.tAnim/10;
                Vector position = raceTrack.getPoint(t);
                Vector tangent = raceTrack.getTangent(t);
                position = position.add(
                        tangent.cross(Vector.Z).normalized().scale(.5f+i++)
                );
                gl.glTranslated(position.x(), position.y(), position.z());

                gl.glRotatef(
                    (float)Math.toDegrees(
                        Math.atan(tangent.y() / tangent.x())
                    ) + (tangent.x() < 0 ? 90 : -90),
                    0.f, 0.f, 1.f
                );
                bob.draw(gs.showStick);
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

        /**
         * @return The shine shininess
         */
        public int getShine() {
            switch(this) {
                case GOLD:      return (int)Math.round(0.4*128);
                case SILVER:    return (int)Math.round(0.4*128);
                case WOOD:      return (int)Math.round(0.1*128);
                case ORANGE:    return (int)Math.round(0.25*128);
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

        /**
         * Draws this robot (as a {@code stickfigure} if specified).
         */
        public void draw(boolean stickFigure) {

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

                            // Mirror to the other side on the 2nd arm
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

        float x = 0.f;
        /**
         * Draws this track, based on the selected track number.
         */
        public void draw(int trackNr) {
            x+=0.2;
            
            // The test track is selected
            if (0 == trackNr) {
                gl.glBegin(gl.GL_TRIANGLE_STRIP);
                    final double STEP = 0.01;
                    for(int j = 0; j < 4; j++) {
                        for(double i = -3*STEP; i <= 1; i += STEP) {
                            Vector initialPoint = getPoint(i),
                                   point = initialPoint;
                            if(j == 1) {
                                point = getLower(point);
                            } else if (j == 2) {
                                point = getOuter(point);
                            }
                            gl.glColor3d(
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
                            gl.glVertex3d(outerPoint.x(), outerPoint.y(), outerPoint.z());
                        }
                    }
                gl.glEnd();
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
            float z = 0;
            Vector v;
            final float STEP = 0.2f;
            for(float x = -20;x<=20;x+=STEP)
            {
                gl.glBegin(GL_TRIANGLE_STRIP);
                
                for(float y = -20;y<=20;y+=STEP)
                {
                    z = heightAt(x, y);
                    setColorByHeight(z);
                    v = getTerrainTangent(x,y);
                    gl.glNormal3d(v.x(),v.y(),v.z());
                    gl.glVertex3f(x, y, z);
                    
                    
                    z = heightAt(x+STEP, y);
                    setColorByHeight(z);
                    v = getTerrainTangent(x+STEP,y);
                    gl.glNormal3d(v.x(),v.y(),v.z());
                    gl.glVertex3f(x+STEP, y, z);
                }
                gl.glEnd();
            }
        }
        
        public void setColorByHeight(float z){
            if(z>0){
                gl.glColor3d(0,1,0);
            }
            else{
                gl.glColor3d(0,0,1);
            }
        }

        /**
         * Computes the elevation of the terrain at ({@code x}, {@code y}).
         */
        public float heightAt(float x, float y) {
            return (float)(0.6*Math.cos(0.3*x+0.2*y)+0.4*Math.cos(x-0.5*y));
        }
        
        public Vector getTerrainTangent(float x, float y){
            Vector du = new Vector(1,0,-0.18*Math.sin(0.3*x+0.2*y)-0.4*Math.sin(x-0.5*y));
            Vector dv = new Vector(0,1,-0.12*Math.sin(0.3*x+0.2*y)-0.2*Math.sin(0.5*y-x));
            return du.cross(dv);
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
