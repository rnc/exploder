package org.goots.exploder.types;

import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.gzip.GzipUtils;

import java.io.File;

public class GZIPFileType extends CompressedFileType
{
    @Override
    public String getUncompressedFilename( File source )
    {
        return GzipUtils.getUncompressedFilename( source.getPath() );
    }

    @Override
    public String getTypename()
    {
        return CompressorStreamFactory.GZIP;
    }
}
