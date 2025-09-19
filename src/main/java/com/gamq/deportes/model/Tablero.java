package com.gamq.deportes.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import com.gamq.deportes.Converter.StringArray2DConverter;
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
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@Entity
@Table(name = "tablero", indexes = @Index(name = "idx_tab_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE tablero SET estado=true WHERE id_tablero=?")
@Where(clause = "estado = false")
public class Tablero {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tablero")
    private Long idTablero;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Type(type = "jsonb")
    @Column(name = "horario", columnDefinition = "jsonb")
    //@Convert(converter = StringArray2DConverter.class)
    private String[][] horario;
    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;
    @OneToOne(mappedBy = "tablero")
    private ReservasProgramadas reservasProgramadas;

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }

}
