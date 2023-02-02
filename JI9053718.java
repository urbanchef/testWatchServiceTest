import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.UUID;

public class JI9053718 {

	public static void main(String[] args) throws Exception {
		//create a directory to watch in the temp directory and an unique subdirectory 
		//subdir is going to be created and events should be received for that 
		Path dir = Paths.get(System.getProperty("java.io.tmpdir")); 
		Path watchdir = dir.resolve(UUID.randomUUID().toString()); 
		Path subdir = watchdir.resolve(UUID.randomUUID().toString()); 

		//create the watched directory 
		Files.createDirectories(watchdir); 
		try { 
			WatchService watcher = FileSystems.getDefault().newWatchService(); 
			WatchKey key = watchdir.register(watcher, new WatchEvent.Kind<?>[] { StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, 
				StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.OVERFLOW }); 

			//1. Set a breakpoint on the next line, 
			//2. Pause the FileSystemWatchService thread manually 
			//3. Run the rest of this method while WatchService is paused 
			Files.createDirectories(subdir); 
			if (key.pollEvents().isEmpty()) { 
				throw new RuntimeException("Test failed, no events received."); 
			} 
			System.out.println("Successful test."); 

		} finally { 
			//clean up the directories 
			Files.delete(subdir); 
			Files.delete(watchdir); 
		} 

	}

}
