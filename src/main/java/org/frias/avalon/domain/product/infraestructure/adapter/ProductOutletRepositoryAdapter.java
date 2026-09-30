package org.frias.avalon.domain.product.infraestructure.adapter;

import lombok.RequiredArgsConstructor;
import org.frias.avalon.domain.product.application.port.ProductOutletRepositoryPort;
import org.frias.avalon.domain.product.domain.ProductDomain;
import org.frias.avalon.domain.product.infraestructure.entity.ProductOutlet;
import org.frias.avalon.domain.product.infraestructure.mapper.ProductOutletMapper;
import org.frias.avalon.domain.product.infraestructure.repository.JpaProductOutletRepository;
import org.frias.avalon.domain.product.infraestructure.specification.ProductSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Adapter for product persistence. Implements the output port from the application layer.
 */
@Component
@RequiredArgsConstructor
public class ProductOutletRepositoryAdapter implements ProductOutletRepositoryPort {

    private final JpaProductOutletRepository jpaProductOutletRepository;
    private final ProductOutletMapper productOutletMapper;
    private final DataSource dataSource;

    @Override
    public ProductDomain save(ProductDomain productDomain) {
        ProductOutlet entityToSave = productOutletMapper.toEntity(productDomain);
        ProductOutlet savedEntity = jpaProductOutletRepository.save(entityToSave);
        return productOutletMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<ProductDomain> findById(Long id) {
        Optional<ProductOutlet> entity = jpaProductOutletRepository.findById(id);
        return entity.map(productOutletMapper::toDomain);
    }

    @Override
    public Page<ProductDomain> findAll(String name, Long outletId, Pageable pageable) {
        return findAll(name, outletId, null, pageable);
    }

    @Override
    public Page<ProductDomain> findAll(String name, Long outletId, Long categoryId, Pageable pageable) {
        Specification<ProductOutlet> spec = ProductSpecification.hasName(name)
                .and(ProductSpecification.hasOutletId(outletId))
                .and(ProductSpecification.hasCategoryId(categoryId));

        Page<ProductOutlet> entityPage = jpaProductOutletRepository.findAll(spec, pageable);
        return entityPage.map(productOutletMapper::toDomain);
    }

    @Override
    public Page<ProductDomain> findAvailableByName(String name, Long outletId, Pageable pageable) {
        Specification<ProductOutlet> spec = ProductSpecification.hasName(name)
                .and(ProductSpecification.hasOutletId(outletId))
                .and(ProductSpecification.hasStockGreaterThanZero());

        Page<ProductOutlet> entityPage = jpaProductOutletRepository.findAll(spec, pageable);
        return entityPage.map(productOutletMapper::toDomain);
    }

    @Override
    public Map<Long, List<ProductDomain>> findAvailableByNameAcrossOutlets(String name, List<Long> outletIds, int limitPerOutlet) {
        if (outletIds == null || outletIds.isEmpty() || name == null || name.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<ProductDomain>> results = new HashMap<>();
        String searchPattern = "%" + name.trim().toLowerCase() + "%";

        try (Connection connection = dataSource.getConnection()) {
            for (Long outletId : outletIds) {
                if (outletId == null) continue;
                String schemaName = "store_" + outletId;
                String sql = "SELECT id, local_name, local_description, stock, unit_measure_id, local_price, outlet_id, status_id, created_at, updated_at, version " +
                        "FROM " + schemaName + ".product_outlet " +
                        "WHERE stock > 0 AND outlet_id = ? AND LOWER(local_name) LIKE ? " +
                        "ORDER BY id DESC LIMIT ?";

                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setLong(1, outletId);
                    ps.setString(2, searchPattern);
                    ps.setInt(3, limitPerOutlet);

                    try (ResultSet rs = ps.executeQuery()) {
                        List<ProductDomain> products = new ArrayList<>();
                        while (rs.next()) {
                            ProductDomain product = ProductDomain.fromPersistence(
                                    rs.getLong("id"),
                                    rs.getString("local_name"),
                                    rs.getString("local_description"),
                                    rs.getInt("stock"),
                                    rs.getObject("unit_measure_id", Long.class),
                                    null,
                                    rs.getBigDecimal("local_price"),
                                    rs.getLong("outlet_id"),
                                    rs.getObject("status_id", Long.class),
                                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null,
                                    rs.getObject("version", Long.class)
                            );
                            products.add(product);
                        }
                        if (!products.isEmpty()) {
                            results.put(outletId, products);
                        }
                    }
                } catch (SQLException e) {
                    // Silencioso si el esquema de una tienda puntual no existe o falla
                }
            }
        } catch (SQLException e) {
            // Silencioso en caso de error al obtener conexion
        }

        return results;
    }
}
