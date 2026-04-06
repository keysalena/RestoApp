CREATE DATABASE db_restoran;
USE db_restoran;

CREATE TABLE role (
    id_role INT AUTO_INCREMENT PRIMARY KEY,
    nama_role VARCHAR(50)
);

CREATE TABLE kategori (
    id_kategori INT AUTO_INCREMENT PRIMARY KEY,
    nama_kategori VARCHAR(100)
);

CREATE TABLE users (
    id_user INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(255),
    nama_user VARCHAR(100),
    id_role INT,
    FOREIGN KEY (id_role) REFERENCES role(id_role) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE menu (
    id_menu INT AUTO_INCREMENT PRIMARY KEY,
    nama_menu VARCHAR(255),
    harga INT,
    gambar VARCHAR(255),
    status ENUM('tersedia', 'habis'),
    id_kategori INT,
    FOREIGN KEY (id_kategori) REFERENCES kategori(id_kategori) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE orders (  
    id_order INT AUTO_INCREMENT PRIMARY KEY,
    no_meja INT,
    id_user INT,
    tanggal DATE,
    total_bayar INT,
    status ENUM('selesai', 'belum selesai'),
    FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE detail_order (
    id_detail_order INT AUTO_INCREMENT PRIMARY KEY,
    id_order INT,
    id_menu INT,
    qty INT,
    total INT,
    status ENUM('selesai', 'belum selesai'),
    FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (id_menu) REFERENCES menu(id_menu) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE transaksi (
    id_transaksi INT AUTO_INCREMENT PRIMARY KEY,
    id_order INT,
    id_user INT,
    tanggal DATE,
    total_bayar INT,
    status ENUM('lunas', 'belum lunas'),
    FOREIGN KEY (id_order) REFERENCES orders(id_order) ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (id_user) REFERENCES users(id_user) ON DELETE RESTRICT ON UPDATE CASCADE
);