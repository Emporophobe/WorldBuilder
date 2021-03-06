package model;

import model.noise.Cylindrical2DNoise;
import model.noise.I2dNoiseGenerator;
import model.noise.OpenSimplexNoise;
import model.settings.ImmutableWorldSettings;

public class RandomHeightmapGenerator implements IHeightmapGenerator {
    private final ImmutableWorldSettings worldSettings;
    private final I2dNoiseGenerator noiseGenerator;

    public RandomHeightmapGenerator(ImmutableWorldSettings worldSettings) {
        this.worldSettings = worldSettings;
        this.noiseGenerator = worldSettings.xWrapping() ?
                new Cylindrical2DNoise(
                        worldSettings.seed(),
                        (double) worldSettings.width() / worldSettings.mapGenerationSettings().scale())
                : new OpenSimplexNoise(worldSettings.seed());
    }

    public IHeightmap generate() {
        // based on https://cmaher.github.io/posts/working-with-simplex-noise/
        int[][] heights = new int[worldSettings.width()][worldSettings.height()];
        for (int x = 0; x < worldSettings.width(); x++) {
            for (int y = 0; y < worldSettings.height(); y++) {
                heights[x][y] = heightAtPoint(x, y);
            }
        }
        return new Heightmap(heights);
    }

    private int heightAtPoint(int x, int y) {
        int iterations = 16;
        double maxAmplitude = 0;
        double amplitude = 1;
        double frequency = 1.0 / worldSettings.mapGenerationSettings().scale();
        double noise = 0;

        for (int i = 0; i < iterations; i++) {
            noise += noiseGenerator.eval(x * frequency, y * frequency) * amplitude;
            maxAmplitude += amplitude;
            amplitude *= 0.5;
            frequency *= 2;
        }
        noise /= maxAmplitude;
        return (int) ((noise + 1) / 2 * worldSettings.mapGenerationSettings().max_height());
    }
}
