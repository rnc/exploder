package org.goots.exploder.types;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.lzma.LZMAUtils;

import java.io.File;

public class LZMAFileType
                extends CompressedFileType
{
    @Override
    public String getUncompressedFilename( File source )
    {
        return LZMAUtils.getUncompressedFilename( source.getPath() );
    }

    @Override
    public String getTypename()
    {
        return CompressorStreamFactory.LZMA;
    }
}
