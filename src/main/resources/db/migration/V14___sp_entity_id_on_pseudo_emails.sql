DELETE FROM pseudo_emails;

ALTER TABLE pseudo_emails
  ADD sp_entity_id TEXT NOT NULL;

ALTER TABLE pseudo_emails
  ADD INDEX pseudo_emails_sp_entity_id_index (sp_entity_id(500));

ALTER TABLE pseudo_emails
  ADD updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

