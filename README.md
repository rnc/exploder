

# Exploder

This utility will recursively decompress and unpack a given file into its current directory
 or a specified working directory. It utilitises Apache Commons Compress to provide the
 archive / compression handling and therefore can potentially support all that that library
 supports. Currently it only supports a subset of it.


### File Analysis

If a calling function implements `ExploderFileProcessor` interface then the
```
    void processFile( File baseDir, File file ) throws InternalException;
```
will be called on each unpacked non-archive/non-compressed file (i.e. terminal unit). This interface
also provides a convenient method to obtain the virtual path of the processed file:
```
 String getVirtualPath (File baseDir, File target)
 ```

For example `example.war / example.jar --> folder/Explorer.class` returns `folder/Explorer.class` as a virtual path for the class.

### API

Exploder supports the following API:

#### Configuration

###### `public Exploder excludeSuffix ( String suffix ) throws InternalException`

Register suffix to ignore when exploding the archive(s).

###### `public Exploder useTemporaryDirectory () throws InternalException`

This will configure the current instance to use a temporary directory to copy the target File to prior to unpacking. This is useful if running the `ExploderFileProcessor` on an archive. It WILL delete the temporary directory on completion.

###### `public Exploder disableRecursion () throws InternalException`

This will disable recursive unpack. By default Exploder will recursively unpack all supported types.

###### `public Exploder useTargetDirectory( File workingDirectory )`

This will configure the current instance to use the specified target directory. If the parameter to unpack is a directory it will be copied to the target directory initially. It will NOT delete the working directory on completion.

#### Unpack Options

###### `public void unpack ( String path ) throws InternalException`

Automatically detects whether a URL (`http`, `https` or `file`) has been passed in and calls the appropriate method.

###### `public void unpack ( File root ) throws InternalException`

Unpacks the contents of the file/directory, decompressing and unarchiving recursively.

If a temporary directory has been configured it will be cleaned up at the end.

###### `public void unpack( ExploderFileProcessor processor, File root ) throws InternalException`

Unpacks the contents of the file/directory, decompressing and unarchiving recursively. It will use the specified ExploderFileProcessor on each target file.

If a temporary directory has been configured it will be cleaned up at the end.

###### `public void unpack( ExploderFileProcessor processor, URL source ) throws InternalException`

Unpacks the contents of the remote file, decompressing and unarchiving recursively. It will use the specified ExploderFileProcessor on each target file.

If a working/temporary directory has not been configured then this will implicitly create and use a temporary directory which WILL be cleaned up at the end.


### Use Cases

* Any operation should be able to run a FileProcessor on it.
* Any unpack operation should be able to ignore pre-configured set of suffixes.
* Any unpack operation should be configurable to recurse or not.
* Any unpack operation can utilise a temporary directory instead of target directory for unpacking
 * FileProcessor can run over this.
 * Automatically cleaned up at the end.


Take a directory structure and copy it and unpack it to a destination directory.
 * Optionally recursively unpack all nested structures ( e.g. jar within a war )
 * Is NOT cleaned up at the end
 * If no target has been supplied use a temporary
Take a file and unpack it to a destination directory.
 * Optionally recursively unpack all nested structures ( e.g. jar within a war )
 * Is NOT cleaned up at the end
 * If no target has been supplied use a temporary
Take a URL and unpack to a destination directory.
 * Optionally recursively unpack all nested structures ( e.g. jar within a war )
 * Is NOT cleaned up at the end
 * URL should be downloaded to a temporary directory prior to commencing unpack.
 * If no target has been supplied use a temporary

### TODO:

1. Multi-threading fork/join execution when decomposing the unpack/decompress.
