package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;


@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
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
    @Column(columnDefinition = "jsonb")
    private String[][] tablero;

    @OneToOne(mappedBy = "tablero")
    private ReservasProgramadas reservasProgramadas;

}
