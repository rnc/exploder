

# Exploder

This utility will recursively decompress and unpack a given file into its current directory
 or a specified working directory. It utilitises Apache Commons Compress to provide the
 archive / compression handling and therefore can potentially support all that that library
 supports. Currently it only supports a subset of it.

If a calling function implements `ExploderFileProcessor` interface then the
```
    void processFile( File file, String virtualPath, String baseDir);
```
will be called on each unpacked non-archive/non-compressed file (i.e. terminal unit).


TODO:

1. Multi-threading fork/join execution when decomposing the unpack/decompress.
