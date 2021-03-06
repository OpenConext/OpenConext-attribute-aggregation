CREATE TABLE accounts (
  id           MEDIUMINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
  urn          VARCHAR(255) NOT NULL UNIQUE,
  name         VARCHAR(255) NOT NULL,
  email        VARCHAR(255),
  account_type VARCHAR(255) NOT NULL,
  linked_id    VARCHAR(255) NOT NULL,
  created      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
)
  ENGINE = InnoDB;

