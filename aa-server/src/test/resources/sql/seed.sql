INSERT INTO `accounts` (`id`, `urn`,`account_type`, `linked_id`, `created`, `schac_home`)
VALUES
  (1, 'saml2_user.com', 'ORCID', 'http://orcid.org/0000-0002-4926-2859', '2017-06-06 09:51:09', 'http://mock-sp');

INSERT INTO `pseudo_emails` (`id`, `email`,`pseudo_email`,`sp_entity_id`,`created`)
VALUES
  (1, 'john.doe@example.com', '6799299b-66ba-32f0-82ad-71e159a8fd40@openconext.org', 'http://localhost','2017-06-06 09:51:09');
