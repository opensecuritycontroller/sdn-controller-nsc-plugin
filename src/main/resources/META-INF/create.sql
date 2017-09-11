 
create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists INSPECTION_HOOK (hook_id varchar(255) not null, inspected_port_fk varchar(255), inspection_port_fk varchar(255), tag bigint, hook_order bigint, enc_type varchar(255), failure_policy_type varchar(255), primary key (hook_id) );
create table if not exists INSPECTION_PORT (element_id varchar(255) not null, ingress_fk varchar(255), egress_fk varchar(255), primary key (element_id) );

create table if not exists NETWORK_ELEMENT (element_id varchar(255) not null, inspection_hook_fk varchar(255), primary key (element_id) );

alter table INSPECTION_HOOK add constraint if not exists FK_INSPECTION_HOOK_NETWORK_ELEMENT foreign key (inspected_port_fk) references NETWORK_ELEMENT;
alter table NETWORK_ELEMENT add constraint if not exists FK_NETWORK_ELEMENT_INSPECTION_HOOK foreign key (inspection_hook_fk) references INSPECTION_HOOK;

alter table INSPECTION_PORT add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_INGR foreign key (ingress_fk) references NETWORK_ELEMENT;
alter table INSPECTION_PORT add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_EGR foreign key (egress_fk) references NETWORK_ELEMENT;

alter table INSPECTION_HOOK add constraint if not exists FK_INSPECTION_HOOK_INSPECTION_PORT foreign key (inspection_port_fk) references INSPECTION_PORT;

create table if not exists NETWORK_ELEMENT_PORTIPS (network_element_fk varchar(255), PORTIPS varchar(255));
create table if not exists NETWORK_ELEMENT_MACADDRESSES (network_element_fk varchar(255), MACADDRESSES varchar(255));

alter table NETWORK_ELEMENT_PORTIPS add constraint if not exists FK_NETWORK_ELEMENT_PORTIPS_NETWORK_ELEMENT foreign key (network_element_fk) references NETWORK_ELEMENT;
alter table NETWORK_ELEMENT_MACADDRESSES add constraint if not exists FK_NETWORK_ELEMENT_MACADDRESSES_NETWORK_ELEMENT foreign key (network_element_fk) references NETWORK_ELEMENT;

