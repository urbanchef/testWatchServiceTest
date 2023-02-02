import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class WatchService8202759 {
    private static final int COUNT = 5;
    private static final long DELAY = 60000L;

    private static final List<String> POSIX_PERMISSIONS = Arrays.asList("rwxrwxrwx", "rwxrwx---", "rwx------");


    public static void main(String[] args) throws Exception {
        int count = args.length > 0 ? Integer.valueOf(args[0]) : COUNT;
        long delay = args.length > 1 ? Long.valueOf(args[1]) : DELAY;

        //create a directory to watch in the temp directory and a unique
        //subdirectory 
        //subdir is going to be created and events should be received for that 
        Path dir = Paths.get(System.getProperty("java.io.tmpdir")); 
        Path watchdir = dir.resolve(UUID.randomUUID().toString());
        Path subdir = watchdir.resolve(UUID.randomUUID().toString());



        //create the watched directory 
        Files.createDirectories(watchdir);
        System.out.println("created dir " + watchdir);
        Path filePath = Files.createTempFile(watchdir, UUID.randomUUID().toString(), ".tmp");
        System.out.println("created temp file " + filePath.toString());
        Thread.currentThread().sleep(1000);
        try { 
            WatchService watcher = FileSystems.getDefault().newWatchService(); 
            WatchKey key = watchdir.register(watcher, new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, 
                StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW }); 

            int successes = 0;
            int failures = 0;
            for (int i = 0; i < count; i++) {
                Collections.shuffle(POSIX_PERMISSIONS);
//                Files.createDirectories(subdir);
                for (String posixPermission : POSIX_PERMISSIONS) {
                    Files.setPosixFilePermissions(filePath, PosixFilePermissions.fromString(posixPermission));
                    System.out.println("set permission " + posixPermission + " to file " + filePath.toString());
                    Thread.currentThread().sleep(1000);
                }
                if (delay > 0) {
                    Thread.currentThread().sleep(delay);
                }
                if (key.pollEvents().isEmpty()) { 
                    System.out.println("Test failed, no events received.");
                    failures++;
                }  else {
                    System.out.println("Successful test.");
                    successes++;
                }
                Files.deleteIfExists(subdir);
                key.reset();
            }
 
            System.out.format("%d out of %d tests (%f percent) failed%n",
                failures, count, 100.0*failures/count);

        } finally {
            Files.deleteIfExists(filePath);
            Files.delete(watchdir);
        } 
    }

}
