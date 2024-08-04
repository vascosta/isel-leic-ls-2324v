begin transaction;

create table player(
    id serial primary key,
    name varchar(25) unique not null ,
    email varchar(40) unique not null,
    passwordhash varchar(20) not null,
    tokenhash varchar(80) unique
);

create table game(
    id serial primary key,
    name varchar(64) unique not null,
    developer varchar(32) not null,
    genres integer[5] not null check(array_length(genres, 1) > 0)
);

create table session(
    id serial primary key,
    capacity int default 1 not null,
    date timestamp with time zone not null,
    game int not null,
    players integer[] not null check (array_length(players, 1) <= capacity),
    host int not null check (host = players[1]),
    foreign key (game) references game(id)
);

commit;