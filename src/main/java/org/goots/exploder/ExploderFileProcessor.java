package org.goots.exploder;

import java.io.File;

public interface ExploderFileProcessor
{
    /**
     * Get any file from inside a zip/jar/ear/war and decide whether to process it or not
     *
     * Implement it to check whether a particular type of file has the right information For e.g check for every class file
     *
     * @param file file to process
     * @param virtualPath virtual path of the file relative to the zip/jar/ear/war it came from
     * @param baseDir the base temporary folder from which the zip/jar/ear/war was unzipped
     */
    void processFile( File file, String virtualPath, String baseDir);
}
