 
create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists INSPECTION_HOOK (hook_id varchar(255) not null, inspected_port_id varchar(255), inspection_port_id varchar(255), tag bigint, hook_order bigint, enc_type varchar(255), failure_policy_type varchar(255), primary key (hook_id) );
create table if not exists INSPECTION_PORT (element_id varchar(255) not null, ingress_id varchar(255), egress_id varchar(255), primary key (element_id) );

create table if not exists NETWORK_ELEMENT (element_id varchar(255) not null, inspection_hook_id varchar(255), primary key (element_id) );

alter table INSPECTION_HOOK add constraint if not exists FK_INSPECTION_HOOK_NETWORK_ELEMENT foreign key (inspected_port_id) references NETWORK_ELEMENT;
alter table NETWORK_ELEMENT add constraint if not exists FK_NETWORK_ELEMENT_INSPECTION_HOOK foreign key (inspection_hook_id) references INSPECTION_HOOK;

alter table INSPECTION_PORT add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_INGR foreign key (ingress_id) references NETWORK_ELEMENT;
alter table INSPECTION_PORT add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_EGR foreign key (egress_id) references NETWORK_ELEMENT;

alter table INSPECTION_HOOK add constraint if not exists FK_INSPECTION_HOOK_INSPECTION_PORT foreign key (inspection_port_id) references INSPECTION_PORT;

create table if not exists NETWORK_ELEMENT_PORTIPS (NETWORK_ELEMENT_ELEMENTID varchar(255), PORTIPS varchar(255));
create table if not exists NETWORK_ELEMENT_MACADDRESSES (NETWORK_ELEMENT_ELEMENTID varchar(255), MACADDRESSES varchar(255));

alter table NETWORK_ELEMENT_PORTIPS add constraint if not exists NETWORK_ELEMENT_PORTIPS_NETWORK_ELEMENT foreign key (NETWORK_ELEMENT_ELEMENTID) references NETWORK_ELEMENT;
alter table NETWORK_ELEMENT_MACADDRESSES add constraint if not exists NETWORK_ELEMENT_MACADDRESSES_NETWORK_ELEMENT foreign key (NETWORK_ELEMENT_ELEMENTID) references NETWORK_ELEMENT;

