package org.frias.avalon.domain.masterdata.domain.model;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MasterTree {

    private final Map<Long, MasterRoot> nodes;
    private final Map<String, MasterRoot> byCode;

    public MasterTree(List<MasterRoot> list) {
        this.nodes = list.stream()
                .collect(Collectors.toMap(MasterRoot::getId, m -> m));

        this.byCode = list.stream()
                .collect(Collectors.toMap(
                        m -> m.getShortName().trim().toUpperCase(),
                        m -> m,
                        (b, a) -> a // evita crash si hay duplicados
                ));
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

    public MasterRoot getByCode(String code) {
        if (code == null) return null;
        return byCode.get(code.toUpperCase());
    }

    public boolean isAny(MasterRoot node, String... codes) {
        if (node == null || codes == null) return false;

        for (String code : codes) {
            if (is(node, code)) return true;
        }
        return false;
    }
}