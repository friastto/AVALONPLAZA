package org.frias.avalon.domain.masterdata.domain.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.frias.avalon.domain.masterdata.domain.model.MasterRoot;
import org.frias.avalon.domain.masterdata.domain.model.MasterTree;
import org.frias.avalon.domain.masterdata.domain.repository.MasterDataRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MasterTreeProvider {

    private final MasterDataRepositoryPort masterPort;

    @Getter
    private volatile MasterTree tree;

    public MasterTreeProvider(MasterDataRepositoryPort masterPort) {
        this.masterPort = masterPort;
    }


    @PostConstruct
    public void init() {
        System.out.println("\n *************\n" +
                "MasterTree sincronizando \n" +
                "******************\n");
        refresh();
        System.out.println("\n *************\n" +
                "sincronizacion del tree finalizada con exito \n" +
                "******************\n");
    }

    public synchronized void refresh() {
        List<MasterRoot> all = masterPort.findAll();
        this.tree = new MasterTree(all);
    }
}