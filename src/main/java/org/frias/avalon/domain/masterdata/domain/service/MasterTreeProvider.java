package org.frias.avalon.domain.masterdata.domain.service;

import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;

import java.util.List;

/**
 * Pure Java Domain service providing memory mirror cache of MasterTree.
 * Free of Spring Framework / Jakarta / Lombok annotations.
 */
public class MasterTreeProvider {

    private final MasterDataRepositoryPort masterPort;
    private volatile MasterTree tree;

    public MasterTreeProvider(MasterDataRepositoryPort masterPort) {
        this.masterPort = masterPort;
    }

    public void init() {
        refresh();
    }

    public synchronized void refresh() {
        List<MasterRoot> all = masterPort.findAll();
        this.tree = new MasterTree(all);
    }

    public MasterTree getTree() {
        return tree;
    }
}