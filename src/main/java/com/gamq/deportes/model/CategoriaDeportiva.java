package com.gamq.deportes.model;


import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "categorias_deportivas", indexes = {
        @Index(name = "idx_cate_uuid", columnList = "uuid", unique = true),
        @Index(name = "idx_cate_nombre", columnList = "nombre", unique = true)
})
@SQLDelete(sql = "UPDATE categorias_deportivas SET estado = true WHERE id_categoria = ?")
@Where(clause = "estado = false")
public class CategoriaDeportiva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_categoria")
    private Long idCategoria;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT false")
    private boolean estado = false;
    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;
    @Column(name = "nombre", unique = true, nullable = false, length = 255)
    private String nombre;
    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;
    @Column(name = "activo", columnDefinition = "BOOLEAN DEFAULT true")
    private boolean activo = true;

            @Column(name = "for_asociation")
            private boolean forAsociacion;
            @Column(name = "tipo", length = 50)
            private String tipo;


    @PrePersist
    public void prePersist() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.fechaCreacion == null) {
            this.fechaCreacion = LocalDateTime.now();
        }
    }

}