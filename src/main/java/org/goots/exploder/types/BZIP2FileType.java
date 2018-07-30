package org.goots.exploder.types;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2Utils;

import java.io.File;

public class BZIP2FileType
                extends CompressedFileType
{
    @Override
    public String getUncompressedFilename( File source )
    {
        return BZip2Utils.getUncompressedFilename( source.getPath() );
    }

    @Override
    public String getTypename()
    {
        return CompressorStreamFactory.BZIP2;
    }
}
