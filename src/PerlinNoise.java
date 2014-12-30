/*
The MIT License (MIT)
Copyright (c) 2013 - 2014, Steven Wokke
Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/**
* A 2D noise which is based on the perlin noise.
* Currently it is not complete as it does not use gradients.
* @author wokste
*/
class PerlinNoise {
    int seed;
    int octaves;
    double scale;

    /**
     * Simple constructor
     * @param newSeed A number to base the terrain on. This should be random
     * @param newOctaves How much details the terrain should have. Take 5.
     * @param newScale The scale. Use the maximum size of mountains you want to have.
     */
    public PerlinNoise(int newSeed, int newOctaves, double newScale) {
        seed = newSeed;
        octaves = newOctaves;
        scale = newScale;
    }

    /**
     * Interpolates between two values. It will use a continious function.
     * @param v1 the return value for factor = 0
     * @param v2 the return value for factor = 1
     * @param factor a value within the range [0, 1]
     * @return a value in the range [v1, v2]
     */
    private double interpolate(double v1, double v2, double factor) {
        // Use a higher order polynomial interpolation factor.
        // This stops the code from generating sharp corners.
        factor = factor * factor * factor * (factor * (factor * 6 - 15) + 10);
        return v1 * (1 - factor) + v2 * factor;
    }

    /**
     * This function generates a pseudo-random number based on: x, y and the seed.
     * It is based on prime magic.
     * @return a value in the range [-1,1]
     */
    private double random2d(int x, int y) {
        int n = x + y * 57;
        n *= seed;
        n ^= n << 13;
        int nn = (n * (n * n * 60493 + 19990303) + 1376312589) & 0x7fffffff;
        return 1.0 - ((double) nn / 1073741824.0);
    }

    /**
     * Calculates 1 octave of the noise2d function
     * @return a continious value in the range [-1,1]
     */
    private double noiseIteration2d(double x, double y) {
        // Get random values for the four corners
        int xFloored = (int) (Math.floor(x));
        int yFloored = (int) (Math.floor(y));
        double topLeft = random2d(xFloored, yFloored);
        double topRight = random2d(xFloored + 1, yFloored);
        double bottomLeft = random2d(xFloored, yFloored + 1);
        double bottomRight = random2d(xFloored + 1, yFloored + 1);

        // Interpolate the four corners.
        double top = interpolate(topLeft, topRight, x - xFloored);
        double bottom = interpolate(bottomLeft, bottomRight, x - xFloored);
        return interpolate(top, bottom, y - yFloored);
    }

    /**
     * Get the noise value of a given x and y coordinates.
     * @return the noise value.
     */
    public double noise2d(double x, double y) {
        x /= scale;
        y /= scale;
        double sum = 0;
        for (int i = 0; i < octaves; i++) {
            double frequency = Math.pow(2, i);
            double amplitude = Math.pow(0.4, i);
            sum += noiseIteration2d(x * frequency, y * frequency) * amplitude;
        }
        return sum;
    }
}
