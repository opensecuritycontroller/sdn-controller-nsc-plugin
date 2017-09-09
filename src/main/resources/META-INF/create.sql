 
create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists InspectionHookEntity (hookId varchar(255) not null, inspectedPortId varchar(255), inspectionPortId varchar(255), tag bigint, hookOrder bigint, encType varchar(255), failurePolicyType varchar(255), primary key (hookId) );
create table if not exists InspectionPortEntity (elementId varchar(255) not null, ingressId varchar(255), egressId varchar(255), primary key (elementId) );

create table if not exists NetworkElementEntity (elementId varchar(255) not null, inspectionHookId varchar(255), primary key (elementId) );

alter table InspectionHookEntity add constraint if not exists FK_INSPECTION_HOOK_NETWORK_ELEMENT foreign key (inspectedPortId) references NetworkElementEntity;
alter table NetworkElementEntity add constraint if not exists FK_NETWORK_ELEMENT_INSPECTION_HOOK foreign key (inspectionHookId) references InspectionHookEntity;

alter table InspectionPortEntity add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_INGR foreign key (ingressId) references NetworkElementEntity;
alter table InspectionPortEntity add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_EGR foreign key (egressId) references NetworkElementEntity;

alter table InspectionHookEntity add constraint if not exists FK_INSPECTION_HOOK_INSPECTION_PORT foreign key (inspectionPortId) references InspectionPortEntity;

create table if not exists NETWORKELEMENTENTITY_PORTIPS (NETWORKELEMENTENTITY_ELEMENTID varchar(255), PORTIPS varchar(255));
create table if not exists NETWORKELEMENTENTITY_MACADDRESSES (NETWORKELEMENTENTITY_ELEMENTID varchar(255), MACADDRESSES varchar(255));

alter table NETWORKELEMENTENTITY_PORTIPS add constraint if not exists NETWORKELEMENTENTITY_PORTIPS_NETWORKELEMENTENTITY foreign key (NETWORKELEMENTENTITY_ELEMENTID) references NetworkElementEntity;
alter table NETWORKELEMENTENTITY_MACADDRESSES add constraint if not exists NETWORKELEMENTENTITY_MACADDRESSES_NETWORKELEMENTENTITY foreign key (NETWORKELEMENTENTITY_ELEMENTID) references NetworkElementEntity;

