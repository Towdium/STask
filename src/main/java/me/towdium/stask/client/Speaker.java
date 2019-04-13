package me.towdium.stask.client;

import me.towdium.stask.utils.*;
import me.towdium.stask.utils.time.Timer;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Author: Towdium
 * Date: 13/04/19
 */
public class Speaker extends Closeable implements Tickable {
    static int count = 0;
    static long device;
    long context;
    Cache<String, Audio> audios = new Cache<>(Audio::new);
    List<Integer> sources = new ArrayList<>();
    Timer timer = new Timer(1d, i -> this.recollect());

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

    public void play(String s) {
        int id = audios.get(s).id;
        int source = AL10.alGenSources();
        AL10.alSourcei(source, AL10.AL_BUFFER, id);
        AL10.alSourcePlay(source);
        sources.add(source);
    }

    @Override
    public void close() {
        super.close();
        ALC10.alcDestroyContext(context);
        audios.foreach((k, v) -> v.delete());
        sources.forEach(AL10::alDeleteSources);
        count--;
        if (count == 0) ALC10.alcCloseDevice(device);
    }

    @Override
    public void tick() {
        timer.tick();
    }

    private void recollect() {
        sources = sources.stream().filter(i -> {
            int state = AL10.alGetSourcei(i, AL10.AL_SOURCE_STATE);
            if (state == AL10.AL_STOPPED) {
                AL10.alDeleteSources(i);
                Log.client.trace("Removed source " + i);
                return false;
            } else return true;
        }).collect(Collectors.toList());
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
}