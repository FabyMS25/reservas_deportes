package com.gamq.deportes.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "categoria", indexes = @Index(name = "idx_cate_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE categoria SET estado=true WHERE id_categoria=?")
@Where(clause = "estado = false")

public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;
    @Column(name = "tipo", nullable = false, length = 50)
    private String tipo;
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
