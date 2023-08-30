package me.hashemalayan;


import btree4j.BTreeException;
import btree4j.BTreeIndex;
import btree4j.BTreeIndexDup;
import btree4j.utils.io.FileUtils;
import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.Provides;
import me.hashemalayan.db.DBManager;
import me.hashemalayan.server.LocalNodeManager;
import me.hashemalayan.server.RemoteNodesManager;
import me.hashemalayan.signaling.SignalingClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

public class NodeModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LocalNodeManager.class).asEagerSingleton();
        bind(RemoteNodesManager.class).asEagerSingleton();
        bind(BTreeIndexDup.class).toProvider(BTreeProvider.class).asEagerSingleton();
        bind(DBManager.class).asEagerSingleton();
        bind(NodeProperties.class).asEagerSingleton();
        bind(SignalingClient.class).asEagerSingleton();
    }

    @Provides
    Logger loggerProvider() {
        return LoggerFactory.getLogger("NodeLogger");
    }

    private static class BTreeProvider implements Provider<BTreeIndexDup> {

        private static final Random random = new Random();
        @Override
        public BTreeIndexDup get() {

            File tmpDir = new File("/home/mythi/development");
            assert tmpDir.exists();

            File tmpFile = new File(tmpDir, "db-"+ 1 + ".idx");
            System.out.println(tmpFile.getAbsolutePath());
//            tmpFile.deleteOnExit();
//            assert !tmpFile.exists() || tmpFile.delete();

            var tree =  new BTreeIndexDup(tmpFile);
            try {
                tree.init(false);
            } catch (BTreeException e) {
                throw new RuntimeException(e);
            }
            return tree;
        }
    }
}
