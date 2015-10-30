package model;

import java.io.File;
import java.util.concurrent.ConcurrentNavigableMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

public class Storage {

    DB db;

    public Storage(String filename, String password) {
        db = DBMaker.fileDB(new File(filename))
                .encryptionEnable(password)
                .make();
    }

    @Deprecated
    public void fetch() {

        // TODO remove example method
        ConcurrentNavigableMap<Integer, String> map = db.treeMap("collectionName");

        map.put(1, "one");
        map.put(2, "two");

        db.commit();

        ConcurrentNavigableMap<Integer, String> map2 = db.treeMap("collectionName");

        for (Integer key : map2.keySet()) {
            System.out.println(map2.get(key));
        }

        db.close();
    }

}
