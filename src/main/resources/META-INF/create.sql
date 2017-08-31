 
create sequence if not exists hibernate_sequence start with 1 increment by 1;

create table if not exists InspectionHookEntity (hookId varchar(255) not null, inspectedPortId varchar(255), inspectionPortId varchar(255), tag bigint, hookOrder bigint, encType varchar(255), failurePolicyType varchar(255), primary key (hookId) );
create table if not exists InspectionPortEntity (elementId varchar(255) not null, inspectionHookId varchar(255), ingressId varchar(255), egressId varchar(255), primary key (elementId) );

create table if not exists NetworkElementEntity (elementId varchar(255) not null, inspectionHookId varchar(255), ingressPortId varchar(255), egressPortId varchar(255), primary key (elementId) );
create table if not exists MacAddressEntity (id bigint not null, elementId varchar(255), macAddress varchar(128), primary key (id) );
create table if not exists PortIpEntity (id bigint not null, elementId varchar(255), portIp varchar(128), primary key (id) );

alter table MacAddressEntity add constraint if not exists FK_MAC_ADDRESS_NETWORK_ELEMENT foreign key (elementId) references NetworkElementEntity;
alter table PortIpEntity add constraint if not exists FK_PORT_NETWORK_ELEMENT foreign key (elementId) references NetworkElementEntity; 

alter table InspectionHookEntity add constraint if not exists FK_INSPECTION_HOOK_NETWORK_ELEMENT foreign key (inspectedPortId) references NetworkElementEntity;
alter table NetworkElementEntity add constraint if not exists FK_NETWORK_ELEMENT_INSPECTION_HOOK foreign key (inspectionHookId) references InspectionHookEntity;

alter table InspectionPortEntity add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_INGR foreign key (ingressId) references NetworkElementEntity;
alter table InspectionPortEntity add constraint if not exists FK_INSPECTION_PORT_NETWORK_ELEMENT_EGR foreign key (egressId) references NetworkElementEntity;
alter table NetworkElementEntity add constraint if not exists FK_NETWORK_ELEMENT_INGR foreign key (ingressPortId) references InspectionPortEntity;
alter table NetworkElementEntity add constraint if not exists FK_NETWORK_ELEMENT_EGR foreign key (egressPortId) references InspectionPortEntity;


alter table InspectionHookEntity add constraint if not exists FK_INSPECTION_HOOK_INSPECTION_PORT foreign key (inspectionPortId) references InspectionPortEntity;
alter table InspectionPortEntity add constraint if not exists FK_INSPECTION_PORT_INSPECTION_HOOK foreign key (inspectionHookId) references InspectionHookEntity;


