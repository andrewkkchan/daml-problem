-- SQL Version: MariaDB 10.3

-- Setup default DB user and privileges
GRANT ALL ON contract.* TO 'sample_user'@'localhost' IDENTIFIED BY '19283746';

SET FOREIGN_KEY_CHECKS = 0;
SET GLOBAL FOREIGN_KEY_CHECKS = 0;

-- Create ledger schema and all tables
DROP SCHEMA IF EXISTS contract;
CREATE SCHEMA IF NOT EXISTS contract DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE contract;

CREATE TABLE IF NOT EXISTS contract.`iou`
(
  `id`         VARCHAR(36)    NOT NULL,
  `issuer`     VARCHAR(360)   NOT NULL,
  `owner`      VARCHAR(360)   NOT NULL,
  `currency`   VARCHAR(50)    NOT NULL,
  `amount`     DECIMAL(20, 5) NOT NULL,
  `contractId` VARCHAR(36)    NULL UNIQUE ,

  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS contract.`iou_transfer`
(
  `id`         VARCHAR(36)  NOT NULL,
  `newOwner`   VARCHAR(360) NOT NULL,
  `iouId`      VARCHAR(36)  NOT NULL,
  `contractId` VARCHAR(36)  NULL,

  PRIMARY KEY (`id`),
  CONSTRAINT `fk_iou_transfer_iou`
    FOREIGN KEY (`iouId`)
      REFERENCES `contract`.`iou` (`contractId`)
      ON DELETE NO ACTION
      ON UPDATE NO ACTION
)
  ENGINE = InnoDB;

