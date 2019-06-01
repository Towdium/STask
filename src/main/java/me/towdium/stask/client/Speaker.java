package me.towdium.stask.client;

import me.towdium.stask.utils.*;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Author: Towdium
 * Date: 13/04/19
 */
@ParametersAreNonnullByDefault
public class Speaker extends Closeable implements Tickable {
    static int count = 0;
    static long device;
    long context;
    Cache<String, Audio> audios = new Cache<>(Audio::new);
    Map<Reference<? extends Source>, Runnable> sources = new HashMap<>();
    ReferenceQueue<Source> queue = new ReferenceQueue<>();

    public Speaker() {
        if (count == 0) {
            String name = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
            device = ALC10.alcOpenDevice(name);
        }
        count++;
        context = ALC10.alcCreateContext(device, new int[]{0});
        ALC10.alcMakeContextCurrent(context);
        AL.createCapabilities(ALC.createCapabilities(device));
    }

    @Override
    public void close() {
        super.close();
        ALC10.alcDestroyContext(context);
        audios.foreach((k, v) -> v.delete());
        sources.forEach((k, v) -> v.run());
        count--;
        if (count == 0) ALC10.alcCloseDevice(device);
    }

    @Override
    public void tick() {
        Reference<? extends Source> s;
        while ((s = queue.poll()) != null) sources.get(s).run();
    }

    public Source source() {
        Source ret = new Source();
        PhantomReference<Source> ref = new PhantomReference<>(ret, queue);
        sources.put(ref, ret.cleaner());
        return ret;
    }

    static class Audio {
        int id;

        public Audio(String s) {
            ByteBuffer audio = Utilities.readBytes("/audio/" + s);
            if (audio == null) throw new RuntimeException("Failed to load audio: " + s);
            ShortBuffer raw;

            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer channel = stack.mallocInt(1);
                IntBuffer rate = stack.mallocInt(1);
                raw = STBVorbis.stb_vorbis_decode_memory(audio, channel, rate);
                Objects.requireNonNull(raw);
                int format = -1;
                if (channel.get(0) == 1) format = AL10.AL_FORMAT_MONO16;
                else if (channel.get(0) == 2) format = AL10.AL_FORMAT_STEREO16;
                id = AL10.alGenBuffers();
                AL10.alBufferData(id, format, raw, rate.get(0));
                LibCStdlib.free(raw);
            }
        }

        public void delete() {
            AL10.alDeleteBuffers(id);
        }
    }

    public class Source {
        int id;

        public Source() {
            id = AL10.alGenSources();
        }

        public void play(String s) {
            AL10.alSourcei(id, AL10.AL_BUFFER, audios.get(s).id);
            AL10.alSourcePlay(id);
        }

        public Runnable cleaner() {
            final int i = id;
            return () -> {
                AL10.alDeleteSources(i);
                Log.client.debug("Removing source " + i);
            };
        }
    }
}
