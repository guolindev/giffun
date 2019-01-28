package com.bumptech.glide.load.model;

import android.os.ParcelFileDescriptor;

import com.bumptech.glide.load.Encoder;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * A source encoder that writes a {@link ImageVideoWrapper} to disk by preferentially
 * writing data from the wrapper's {@link InputStream} and falling back to the wrapper's
 * {@link ParcelFileDescriptor} if the {@link InputStream} isn't available.
 */
public class ImageVideoWrapperEncoder implements Encoder<ImageVideoWrapper> {
    private final Encoder<InputStream> streamEncoder;
    private final Encoder<ParcelFileDescriptor> fileDescriptorEncoder;
    private String id;

    public ImageVideoWrapperEncoder(Encoder<InputStream> streamEncoder,
            Encoder<ParcelFileDescriptor> fileDescriptorEncoder) {
        this.streamEncoder = streamEncoder;
        this.fileDescriptorEncoder = fileDescriptorEncoder;
    }

    @Override
    public boolean encode(ImageVideoWrapper data, OutputStream os) {
        if (data.getStream() != null) {
            return streamEncoder.encode(data.getStream(), os);
        } else {
            return fileDescriptorEncoder.encode(data.getFileDescriptor(), os);
        }
    }

    @Override
    public String getId() {
        if (id == null) {
            id = streamEncoder.getId() + fileDescriptorEncoder.getId();
        }
        return id;
    }
}
