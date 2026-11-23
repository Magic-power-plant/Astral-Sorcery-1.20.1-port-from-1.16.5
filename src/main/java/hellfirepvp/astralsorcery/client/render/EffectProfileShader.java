package hellfirepvp.astralsorcery.client.render;

import hellfirepvp.astralsorcery.client.util.Blending;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;

import java.io.IOException;
import java.util.Arrays;
import java.util.regex.Pattern;

final class EffectProfileShader extends EffectMaterialShader {
    static final int PAGE_BYTES = 65536;
    private static final int PAGE_WORDS = PAGE_BYTES / Integer.BYTES;
    private static final Pattern WORD = Pattern.compile("0x([0-9a-f]{8})u");
    private final int[] buffers;
    private final int[] previous;
    private final long[] offsets;
    private final long[] sizes;

    EffectProfileShader(ResourceProvider resources, ResourceLocation program, ResourceLocation coefficients, Blending blending) throws IOException {
        super(resources, program, blending);
        int bound = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_BINDING);
        try (var reader = resources.openAsReader(coefficients)) {
            int[] words = reader.lines().flatMapToInt(line -> WORD.matcher(line).results()
                    .mapToInt(match -> Integer.parseUnsignedInt(match.group(1), 16))).toArray();
            buffers = new int[(words.length + PAGE_WORDS - 1) / PAGE_WORDS];
            if (buffers.length == 0 || buffers.length > 12) throw new IOException("Invalid effect profile size " + coefficients);
            previous = new int[buffers.length];
            offsets = new long[buffers.length];
            sizes = new long[buffers.length];
            for (int page = 0; page < buffers.length; page++) {
                int block = GL31.glGetUniformBlockIndex(getId(), "Profiles" + page);
                if (block == GL31.GL_INVALID_INDEX) throw new IOException("Missing profile block " + page + " in " + program);
                int bytes = GL31.glGetActiveUniformBlocki(getId(), block, GL31.GL_UNIFORM_BLOCK_DATA_SIZE);
                int from = page * PAGE_WORDS;
                int expected = Math.min(words.length - from, PAGE_WORDS);
                if (bytes != (expected + 3) / 4 * 16) throw new IOException("Mismatched profile block " + coefficients);
                buffers[page] = GL15.glGenBuffers();
                GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, buffers[page]);
                GL15.glBufferData(GL31.GL_UNIFORM_BUFFER, Arrays.copyOfRange(words, from, from + bytes / Integer.BYTES), GL15.GL_STATIC_DRAW);
                GL31.glUniformBlockBinding(getId(), block, page);
            }
        } catch (IOException | RuntimeException failure) {
            close();
            throw failure;
        } finally {
            GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bound);
        }
    }

    @Override
    public void apply() {
        super.apply();
        int bound = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_BINDING);
        for (int page = 0; page < buffers.length; page++) {
            previous[page] = GL30.glGetIntegeri(GL31.GL_UNIFORM_BUFFER_BINDING, page);
            if (previous[page] != 0) {
                offsets[page] = GL32.glGetInteger64i(GL31.GL_UNIFORM_BUFFER_START, page);
                sizes[page] = GL32.glGetInteger64i(GL31.GL_UNIFORM_BUFFER_SIZE, page);
            }
            GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, page, buffers[page]);
        }
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bound);
    }

    @Override
    public void clear() {
        int bound = GL11.glGetInteger(GL31.GL_UNIFORM_BUFFER_BINDING);
        for (int page = 0; page < buffers.length; page++) {
            if (previous[page] != 0 && sizes[page] > 0) {
                GL30.glBindBufferRange(GL31.GL_UNIFORM_BUFFER, page, previous[page], offsets[page], sizes[page]);
            } else {
                GL30.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, page, previous[page]);
            }
        }
        GL15.glBindBuffer(GL31.GL_UNIFORM_BUFFER, bound);
        super.clear();
    }

    @Override
    public void close() {
        if (buffers != null) GL15.glDeleteBuffers(buffers);
        super.close();
    }
}
