package org.frias.avalon.ventas.entities;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.frias.avalon.maestra.entities.MasterData;
import org.frias.avalon.person.entity.Person;
import org.frias.avalon.user.entities.User;
import org.hibernate.annotations.UuidGenerator;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( nullable = false, unique = true, updatable = false, columnDefinition = "uuid not null default gen_random_uuid()")
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    private UUID saleCode;

    @Column(nullable = false)
    private BigDecimal total;

    @Column(nullable = false)
    private BigDecimal amountReceived;

    @Column(nullable = false)
    private BigDecimal amountReturned;

    @Column(nullable = false)
    private LocalDateTime saleDateAt;

    @ManyToOne // cada venta pertenece a un cliente
    @JoinColumn
    private Person customerId;

    @ManyToOne(optional = false) // cada venta la realiza un empleado(cajero admin etc)
    @JoinColumn( nullable = false, foreignKey = @ForeignKey(name = "fk_sale_enployeeId"))
    private User enployeeId;

//    @ManyToOne
//    @JoinColumn(name = "pedi_id", foreignKey = @ForeignKey(name = "fk_venta_pedido"))
//    private Pedido pedido;

    @ManyToOne(optional = false)
    @JoinColumn(name = "paymentmethod_id", foreignKey = @ForeignKey(name = "fk_sales_paimentmethodId"))
    private MasterData paymentMethodId;

    @ManyToOne()
    @JoinColumn(name = "status_id", foreignKey = @ForeignKey(name = "fk_sales_statusId"))
    MasterData statusId;

    /*
        @ManyToOne(optional = false)
        @JoinColumn(name = "empr_id")
        private Empresa empresa;
     */

    /// nota: en el repository Cargarlas desde el repositorio con JOIN FETCH o @EntityGraph.
    @JsonManagedReference
    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private List<SaleDetail> details = new ArrayList<>();


}


