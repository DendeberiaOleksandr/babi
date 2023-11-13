alter table place add column if not exists street_number varchar(50);
alter table place add column if not exists route varchar(255);
alter table place add column if not exists locality varchar(255);
alter table place add column if not exists administrative_area_level_2 varchar(255);
alter table place add column if not exists administrative_area_level_1 varchar(255);
alter table place add column if not exists country varchar(255);
alter table place add column if not exists postal_code varchar(20);