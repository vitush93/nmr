package cz.vithabada.nmr_gui.model;

import java.io.File;
import java.util.Map;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Storage wrapper for MapDB.
 *
 * <example>
 * <code>
 * Storage storage = new Storage("database", "password"); // login to database (existing or create a new one)
 *
 * Map<Integer, String> map = storage.map("something"); // retrieve map
 * (existing or create a new one)
 *
 * map.put(1, "item 1"); map.put(2, "item 2");
 *
 * storage.flush(); // flush changes to the filesystem
 *
 * for(Integer key : map.keySet()) { System.out.println(map.get(key)); }
 * </code>
 * </example>
 *
 * @author vitush
 */
public class Storage {

    private DB db;

    public Storage(String filename, String password) {
        db = DBMaker.fileDB(new File(filename))
                .encryptionEnable(password)
                .closeOnJvmShutdown()
                .make();
    }

    /**
     * Retrieve data from the file storage.
     *
     * @param name
     * @param <K>
     * @param <V>
     * @return
     */
    public <K, V> Map<K, V> map(String name) {
        return db.treeMap(name);
    }

    /**
     * Flush data to the file storage.
     */
    public void flush() {
        db.commit();
    }
}
