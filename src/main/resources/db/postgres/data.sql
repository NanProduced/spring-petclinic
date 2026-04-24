INSERT INTO clinics (name, address, city, telephone) SELECT 'Spring PetClinic Downtown', '123 Main Street', 'Madison', '6085550001' WHERE NOT EXISTS (SELECT * FROM clinics WHERE id=1);
INSERT INTO clinics (name, address, city, telephone) SELECT 'Spring PetClinic Westside', '456 Oak Avenue', 'Sun Prairie', '6085550002' WHERE NOT EXISTS (SELECT * FROM clinics WHERE id=2);

INSERT INTO users (username, password, clinic_id, role, enabled) SELECT 'admin1', '$2a$10$EqV8s7Q5eHb.JkfH9aJ3eOvq3Qx7Yb6Z5aW4eS3dR2cV1bN0m9K', 1, 'ROLE_ADMIN', true WHERE NOT EXISTS (SELECT * FROM users WHERE id=1);
INSERT INTO users (username, password, clinic_id, role, enabled) SELECT 'admin2', '$2a$10$EqV8s7Q5eHb.JkfH9aJ3eOvq3Qx7Yb6Z5aW4eS3dR2cV1bN0m9K', 2, 'ROLE_ADMIN', true WHERE NOT EXISTS (SELECT * FROM users WHERE id=2);

INSERT INTO vets (first_name, last_name, clinic_id) SELECT 'James', 'Carter', 1 WHERE NOT EXISTS (SELECT * FROM vets WHERE id=1);
INSERT INTO vets (first_name, last_name, clinic_id) SELECT 'Helen', 'Leary', 1 WHERE NOT EXISTS (SELECT * FROM vets WHERE id=2);
INSERT INTO vets (first_name, last_name, clinic_id) SELECT 'Linda', 'Douglas', 1 WHERE NOT EXISTS (SELECT * FROM vets WHERE id=3);
INSERT INTO vets (first_name, last_name, clinic_id) SELECT 'Rafael', 'Ortega', 2 WHERE NOT EXISTS (SELECT * FROM vets WHERE id=4);
INSERT INTO vets (first_name, last_name, clinic_id) SELECT 'Henry', 'Stevens', 2 WHERE NOT EXISTS (SELECT * FROM vets WHERE id=5);
INSERT INTO vets (first_name, last_name, clinic_id) SELECT 'Sharon', 'Jenkins', 2 WHERE NOT EXISTS (SELECT * FROM vets WHERE id=6);

INSERT INTO specialties (name) SELECT 'radiology' WHERE NOT EXISTS (SELECT * FROM specialties WHERE name='radiology');
INSERT INTO specialties (name) SELECT 'surgery' WHERE NOT EXISTS (SELECT * FROM specialties WHERE name='surgery'); 
INSERT INTO specialties (name) SELECT 'dentistry' WHERE NOT EXISTS (SELECT * FROM specialties WHERE name='dentistry');

INSERT INTO vet_specialties VALUES (2, 1) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (3, 2) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (3, 3) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (4, 2) ON CONFLICT (vet_id, specialty_id) DO NOTHING;
INSERT INTO vet_specialties VALUES (5, 1) ON CONFLICT (vet_id, specialty_id) DO NOTHING;

INSERT INTO types (name) SELECT 'cat' WHERE NOT EXISTS (SELECT * FROM types WHERE name='cat');
INSERT INTO types (name) SELECT 'dog' WHERE NOT EXISTS (SELECT * FROM types WHERE name='dog');
INSERT INTO types (name) SELECT 'lizard' WHERE NOT EXISTS (SELECT * FROM types WHERE name='lizard');
INSERT INTO types (name) SELECT 'snake' WHERE NOT EXISTS (SELECT * FROM types WHERE name='snake');
INSERT INTO types (name) SELECT 'bird' WHERE NOT EXISTS (SELECT * FROM types WHERE name='bird');
INSERT INTO types (name) SELECT 'hamster' WHERE NOT EXISTS (SELECT * FROM types WHERE name='hamster');

INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'George', 'Franklin', '110 W. Liberty St.', 'Madison', '6085551023', 1 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=1);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Betty', 'Davis', '638 Cardinal Ave.', 'Sun Prairie', '6085551749', 1 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=2);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Eduardo', 'Rodriquez', '2693 Commerce St.', 'McFarland', '6085558763', 1 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=3);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Harold', 'Davis', '563 Friendly St.', 'Windsor', '6085553198', 1 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=4);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Peter', 'McTavish', '2387 S. Fair Way', 'Madison', '6085552765', 1 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=5);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Jean', 'Coleman', '105 N. Lake St.', 'Monona', '6085552654', 2 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=6);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Jeff', 'Black', '1450 Oak Blvd.', 'Monona', '6085555387', 2 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=7);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Maria', 'Escobito', '345 Maple St.', 'Madison', '6085557683', 2 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=8);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'David', 'Schroeder', '2749 Blackhawk Trail', 'Madison', '6085559435', 2 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=9);
INSERT INTO owners (first_name, last_name, address, city, telephone, clinic_id) SELECT 'Carlos', 'Estaban', '2335 Independence La.', 'Waunakee', '6085555487', 2 WHERE NOT EXISTS (SELECT * FROM owners WHERE id=10);

INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Leo', '2000-09-07', 1, 1, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=1);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Basil', '2002-08-06', 6, 2, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=2);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Rosy', '2001-04-17', 2, 3, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=3);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Jewel', '2000-03-07', 2, 3, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=4);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Iggy', '2000-11-30', 3, 4, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=5);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'George', '2000-01-20', 4, 5, 1 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=6);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Samantha', '1995-09-04', 1, 6, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=7);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Max', '1995-09-04', 1, 6, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=8);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Lucky', '1999-08-06', 5, 7, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=9);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Mulligan', '1997-02-24', 2, 8, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=10);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Freddy', '2000-03-09', 5, 9, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=11);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Lucky', '2000-06-24', 2, 10, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=12);
INSERT INTO pets (name, birth_date, type_id, owner_id, clinic_id) SELECT 'Sly', '2002-06-08', 1, 10, 2 WHERE NOT EXISTS (SELECT * FROM pets WHERE id=13);

INSERT INTO visits (pet_id, visit_date, description, clinic_id) SELECT 7, '2010-03-04', 'rabies shot', 2 WHERE NOT EXISTS (SELECT * FROM visits WHERE id=1);
INSERT INTO visits (pet_id, visit_date, description, clinic_id) SELECT 8, '2011-03-04', 'rabies shot', 2 WHERE NOT EXISTS (SELECT * FROM visits WHERE id=2);
INSERT INTO visits (pet_id, visit_date, description, clinic_id) SELECT 8, '2009-06-04', 'neutered', 2 WHERE NOT EXISTS (SELECT * FROM visits WHERE id=3);
INSERT INTO visits (pet_id, visit_date, description, clinic_id) SELECT 7, '2008-09-04', 'spayed', 2 WHERE NOT EXISTS (SELECT * FROM visits WHERE id=4);
