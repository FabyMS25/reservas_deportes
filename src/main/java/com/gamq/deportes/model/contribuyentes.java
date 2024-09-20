package com.gamq.deportes.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "contribuyentes", indexes = @Index(name = "idx_cont_uuid", columnList = "uuid", unique = true))
@SQLDelete(sql = "UPDATE contribuyentes SET estado=true WHERE id_contribuyente=?")
@Where(clause = "estado = false")
public class contribuyentes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contribuyente")
    private Long idContribuyente;
    @Column(name = "uuid", updatable = false, unique = true, nullable = false, length = 50)
    private String uuid;
    @Column(name = "user_name", nullable = false, length = 20)
    private String userName;
    @Column(name = "password", nullable = false, length = 15)
    private String password;
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;
    @Column(name = "ap_paterno", nullable = false, length = 100)
    private String apPaterno;
    @Column(name = "ap_materno", nullable = false, length = 100)
    private String apMaterno;
    @Column(name = "nro_carnet", nullable = false, length = 10)
    private String nroCarnet;
    @Column(name = "nro_nit", nullable = false, length = 10)
    private String nroNit;
    @Column(name = "ci_expedido", nullable = false, length = 10)
    private String ciExpedido;
    @Column(name = "sexo", nullable = false, length = 10)
    private String sexo;
    @Column(name = "direccion", nullable = false, length = 100)
    private String direccion;
    @Column(name = "celular", nullable = false, length = 10)
    private String celular;
    @Column(name = "correo", nullable = false,  length = 30)
    private String correo;
    @Column(name = "tipo_contribuyente", nullable = false,  length = 30)
    private String tipoContribuyente;
    @Column(name = "codigo_contribuyente", nullable = false,  length = 30)
    private String codigoContribuyente;
    @Column(name = "tipo_documento", nullable = false,  length = 30)
    private String tipoDocumento;
    @Column(name = "estado_civil", nullable = false,  length = 30)
    private String estadoCivil;
    @Column(name = "fecha_nac", nullable = false)
    private String fechaNac;
    @Column(name = "estado_usuario", nullable = false,  length = 30)
    private String estadoUsuario;



    @Column(name = "estado", columnDefinition = "BOOLEAN NOT NULL DEFAULT '0'")
    private boolean estado;


    @OneToMany(mappedBy = "contribuyente", cascade = CascadeType.ALL)
    public List<reservas> reservasList = new ArrayList<>();

    public contribuyentes(String uuid) {
        this.uuid = uuid;
    }

    @PrePersist
    public void initializeUuid() {
        this.setUuid(UUID.randomUUID().toString());
    }
}
