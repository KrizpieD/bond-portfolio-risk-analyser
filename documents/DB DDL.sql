create table bpra.bond_portfolio (
	id integer generated always as identity primary key,
	portfolio_name varchar
);

create table bpra.bond (
	id integer generated always as identity primary key,
	portfolio_id integer references bpra.bond_portfolio (id),
	isin varchar,
	maturity_date timestamp,
	coupon_rate decimal,
	face_value decimal,
	market_price decimal
);

create table bpra.bond_coupon_date (
	bond_id integer references bpra.bond (id),
	coupon_date timestamp
);