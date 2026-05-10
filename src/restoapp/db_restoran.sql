-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: May 04, 2026 at 11:59 AM
-- Server version: 8.0.30
-- PHP Version: 8.3.13

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `db_restoran`
--

DELIMITER $$
--
-- Procedures
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `tambah_order` (IN `p_no_meja` INT, IN `p_total` INT, OUT `p_id_order` INT)   BEGIN
    INSERT INTO orders(no_meja, tanggal, total_bayar, status_pesan, status_bayar)
    VALUES (p_no_meja, CURDATE(), p_total, 'proses', 'belum lunas');

    SET p_id_order = LAST_INSERT_ID();
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `detail_order`
--

CREATE TABLE `detail_order` (
  `id_detail_order` int NOT NULL,
  `id_order` int DEFAULT NULL,
  `id_menu` int DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `total` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `detail_order`
--

INSERT INTO `detail_order` (`id_detail_order`, `id_order`, `id_menu`, `qty`, `total`) VALUES
(13, 10, 4, 1, 30000),
(14, 10, 5, 1, 21000),
(15, 10, 7, 2, 12000),
(16, 11, 6, 3, 12000),
(17, 11, 4, 1, 30000),
(18, 11, 5, 2, 42000),
(19, 12, 7, 1, 6000),
(20, 12, 6, 2, 8000),
(21, 12, 4, 3, 90000);

-- --------------------------------------------------------

--
-- Table structure for table `kategori`
--

CREATE TABLE `kategori` (
  `id_kategori` int NOT NULL,
  `nama_kategori` varchar(100) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `kategori`
--

INSERT INTO `kategori` (`id_kategori`, `nama_kategori`) VALUES
(1, 'Makanan'),
(3, 'Minuman');

-- --------------------------------------------------------

--
-- Table structure for table `menu`
--

CREATE TABLE `menu` (
  `id_menu` int NOT NULL,
  `nama_menu` varchar(255) DEFAULT NULL,
  `harga` int DEFAULT NULL,
  `gambar` varchar(255) DEFAULT NULL,
  `status` enum('tersedia','habis') DEFAULT NULL,
  `id_kategori` int DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `menu`
--

INSERT INTO `menu` (`id_menu`, `nama_menu`, `harga`, `gambar`, `status`, `id_kategori`) VALUES
(4, 'Sop Iga', 30000, 'images\\1776578488360_sop iga.jpg', 'tersedia', 1),
(5, 'Soto', 21000, 'images\\1776578520465_soto.jpg', 'tersedia', 1),
(6, 'Es Teh', 4000, 'images\\1776578553814_es teh.jpg', 'tersedia', 3),
(7, 'Kopi', 6000, 'images\\1776578587032_kopi.jpg', 'tersedia', 3),
(8, 'Jeruk Hangat', 5000, 'images\\1776578628245_jeruk anget.jpg', 'habis', 3);

-- --------------------------------------------------------

--
-- Table structure for table `orders`
--

CREATE TABLE `orders` (
  `id_order` int NOT NULL,
  `no_meja` int DEFAULT NULL,
  `id_user` int DEFAULT NULL,
  `tanggal` date DEFAULT NULL,
  `total_bayar` int DEFAULT NULL,
  `status_pesan` enum('selesai','proses') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
  `status_bayar` enum('lunas','belum lunas') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `orders`
--

INSERT INTO `orders` (`id_order`, `no_meja`, `id_user`, `tanggal`, `total_bayar`, `status_pesan`, `status_bayar`) VALUES
(10, 2, NULL, '2026-04-19', 63000, 'selesai', 'lunas'),
(11, 0, NULL, '2026-04-19', 86000, 'selesai', 'lunas'),
(12, 8, NULL, '2026-04-19', 104000, 'selesai', 'lunas');

--
-- Triggers `orders`
--
DELIMITER $$
CREATE TRIGGER `before_delete_order` BEFORE DELETE ON `orders` FOR EACH ROW BEGIN
    DELETE FROM detail_order WHERE id_order = OLD.id_order;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `id_user` int NOT NULL,
  `username` varchar(50) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `nama_user` varchar(100) DEFAULT NULL,
  `role` enum('Admin','Kasir') NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`id_user`, `username`, `password`, `nama_user`, `role`) VALUES
(1, 'admin', 'admin', 'Administrator', 'Admin'),
(2, 'kasir', 'kasir', 'Kasir', 'Kasir');

-- --------------------------------------------------------

--
-- Stand-in structure for view `view_menu`
-- (See below for the actual view)
--
CREATE TABLE `view_menu` (
`gambar` varchar(255)
,`harga` int
,`id_menu` int
,`nama_kategori` varchar(100)
,`nama_menu` varchar(255)
,`status` enum('tersedia','habis')
);

-- --------------------------------------------------------

--
-- Structure for view `view_menu`
--
DROP TABLE IF EXISTS `view_menu`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `view_menu`  AS SELECT `m`.`id_menu` AS `id_menu`, `m`.`nama_menu` AS `nama_menu`, `m`.`harga` AS `harga`, `m`.`gambar` AS `gambar`, `m`.`status` AS `status`, `k`.`nama_kategori` AS `nama_kategori` FROM (`menu` `m` join `kategori` `k` on((`m`.`id_kategori` = `k`.`id_kategori`))) ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `detail_order`
--
ALTER TABLE `detail_order`
  ADD PRIMARY KEY (`id_detail_order`),
  ADD KEY `id_order` (`id_order`),
  ADD KEY `id_menu` (`id_menu`);

--
-- Indexes for table `kategori`
--
ALTER TABLE `kategori`
  ADD PRIMARY KEY (`id_kategori`);

--
-- Indexes for table `menu`
--
ALTER TABLE `menu`
  ADD PRIMARY KEY (`id_menu`),
  ADD KEY `id_kategori` (`id_kategori`);

--
-- Indexes for table `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`id_order`),
  ADD KEY `id_user` (`id_user`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id_user`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `detail_order`
--
ALTER TABLE `detail_order`
  MODIFY `id_detail_order` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=22;

--
-- AUTO_INCREMENT for table `kategori`
--
ALTER TABLE `kategori`
  MODIFY `id_kategori` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT for table `menu`
--
ALTER TABLE `menu`
  MODIFY `id_menu` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=9;

--
-- AUTO_INCREMENT for table `orders`
--
ALTER TABLE `orders`
  MODIFY `id_order` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `id_user` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `detail_order`
--
ALTER TABLE `detail_order`
  ADD CONSTRAINT `detail_order_ibfk_1` FOREIGN KEY (`id_order`) REFERENCES `orders` (`id_order`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `detail_order_ibfk_2` FOREIGN KEY (`id_menu`) REFERENCES `menu` (`id_menu`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `menu`
--
ALTER TABLE `menu`
  ADD CONSTRAINT `menu_ibfk_1` FOREIGN KEY (`id_kategori`) REFERENCES `kategori` (`id_kategori`) ON DELETE RESTRICT ON UPDATE CASCADE;

--
-- Constraints for table `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `users` (`id_user`) ON DELETE RESTRICT ON UPDATE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
