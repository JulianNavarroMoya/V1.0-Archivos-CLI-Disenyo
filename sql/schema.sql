-- Esquema de JobTrackr (v2.0).
-- Requiere que la base de datos tracker exista (la crea root una sola vez).

USE tracker;

DROP TABLE IF EXISTS ENTREVISTA;
DROP TABLE IF EXISTS OFERTA;
DROP TABLE IF EXISTS EMPRESA;

CREATE TABLE EMPRESA (
                         id            INT          NOT NULL AUTO_INCREMENT,
                         nombre        VARCHAR(120) NOT NULL,
                         sector        VARCHAR(100),
                         web           VARCHAR(255),
                         fechaRegistro DATE         NOT NULL DEFAULT (CURRENT_DATE),
                         PRIMARY KEY (id),
                         CONSTRAINT uq_empresa_nombre UNIQUE (nombre)
) ENGINE = InnoDB;

CREATE TABLE OFERTA (
                        id               INT           NOT NULL AUTO_INCREMENT,
                        id_empresa       INT           NOT NULL,
                        titulo           VARCHAR(150)  NOT NULL,
                        descripcion      TEXT,
                        estado           ENUM('pendiente', 'entrevista', 'rechazado', 'aceptado') NOT NULL DEFAULT 'pendiente',
                        fechaPostulacion DATE          NOT NULL,
                        enlace           VARCHAR(500),
                        salario          DECIMAL(10,2),
                        notas            TEXT,
                        PRIMARY KEY (id),
                        CONSTRAINT fk_oferta_empresa FOREIGN KEY (id_empresa) REFERENCES EMPRESA (id)
                            ON DELETE RESTRICT ON UPDATE CASCADE,
                        INDEX idx_oferta_estado (estado),
                        INDEX idx_oferta_fecha (fechaPostulacion)
) ENGINE = InnoDB;

CREATE TABLE ENTREVISTA (
                            id        INT  NOT NULL AUTO_INCREMENT,
                            id_oferta INT  NOT NULL,
                            fecha     DATE NOT NULL,
                            tipo      ENUM('telefonica', 'tecnica', 'presencial', 'online') NOT NULL,
                            notas     TEXT,
                            resultado ENUM('pendiente', 'positivo', 'negativo') NOT NULL DEFAULT 'pendiente',
                            PRIMARY KEY (id),
                            CONSTRAINT fk_entrevista_oferta FOREIGN KEY (id_oferta) REFERENCES OFERTA (id)
                                ON DELETE CASCADE ON UPDATE CASCADE,
                            INDEX idx_entrevista_fecha (fecha)
) ENGINE = InnoDB;