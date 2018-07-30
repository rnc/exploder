package org.goots.exploder.types;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.xz.XZUtils;

import java.io.File;

public class XZFileType
                extends CompressedFileType
{
    @Override
    public String getUncompressedFilename( File source )
    {
        return XZUtils.getUncompressedFilename( source.getPath() );
    }

    @Override
    public String getTypename()
    {
        return CompressorStreamFactory.XZ;
    }
}
