package org.frias.avalon.domain.masterdata.domain.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MasterTree {

    private final Map<Long, MasterRoot> nodes;

    public MasterTree(List<MasterRoot> list) {
        this.nodes = list.stream()
                .collect(Collectors.toMap(MasterRoot::getId, m -> m));
    }

    public boolean isChildOf(MasterRoot node, String parentCode) {

        MasterRoot current = node;

        while (current != null) {

            if (current.getShortName().equals(parentCode)) {
                return true;
            }

            current = nodes.get(current.getParentId());
        }

        return false;
    }

    public MasterRoot getById(Long id) {
        return nodes.get(id);
    }

    public MasterRoot getByIdOrThrow(Long id) {
        MasterRoot node = nodes.get(id);
        if (node == null) {
            throw new IllegalStateException("MasterData no encontrado: " + id);
        }
        return node;
    }
    public boolean is(MasterRoot node, String code) {
        if (node == null || code == null) {
            return false;
        }
        return code.equalsIgnoreCase(node.getShortName().trim());
    }
}