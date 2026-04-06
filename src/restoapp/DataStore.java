package restoapp;
import java.util.ArrayList;
import java.util.List;

// ── Masakan (Food Item) ────────────────────────────────────────────────────
class Masakan {
    public int id;
    public String nama, kategori, deskripsi, status;
    public double harga;

    public Masakan(int id, String nama, String kategori, String deskripsi, double harga, String status) {
        this.id = id; this.nama = nama; this.kategori = kategori;
        this.deskripsi = deskripsi; this.harga = harga; this.status = status;
    }
}

// ── Kategori ──────────────────────────────────────────────────────────────
class Kategori {
    public int id;
    public String nama, deskripsi;

    public Kategori(int id, String nama, String deskripsi) {
        this.id = id; this.nama = nama; this.deskripsi = deskripsi;
    }
}

// ── User ──────────────────────────────────────────────────────────────────
class User {
    public int id;
    public String nama, username, role, status;

    public User(int id, String nama, String username, String role, String status) {
        this.id = id; this.nama = nama; this.username = username;
        this.role = role; this.status = status;
    }
}

// ── DataStore (shared in-memory database) ─────────────────────────────────
public class DataStore {
    public static List<Masakan> masakanList = new ArrayList<>();
    public static List<Kategori> kategoriList = new ArrayList<>();
    public static List<User> userList = new ArrayList<>();
    public static int nextMasakanId = 6;
    public static int nextKategoriId = 6;
    public static int nextUserId = 4;
    public static String loggedUser = "Admin";

    static {
        // Seed masakan
        masakanList.add(new Masakan(1, "Nasi Goreng Spesial", "Nasi", "Nasi goreng dengan telur, ayam, dan sayuran segar", 35000, "Tersedia"));
        masakanList.add(new Masakan(2, "Soto Ayam", "Sup", "Soto ayam kuah bening dengan mie dan telur", 28000, "Tersedia"));
        masakanList.add(new Masakan(3, "Rendang Sapi", "Daging", "Rendang sapi empuk dengan bumbu rempah khas Padang", 55000, "Tersedia"));
        masakanList.add(new Masakan(4, "Gado-Gado", "Sayuran", "Sayur dengan saus kacang spesial", 22000, "Habis"));
        masakanList.add(new Masakan(5, "Es Teh Manis", "Minuman", "Teh manis dingin segar", 8000, "Tersedia"));

        // Seed kategori
        kategoriList.add(new Kategori(1, "Nasi", "Makanan berbasis nasi"));
        kategoriList.add(new Kategori(2, "Sup", "Aneka sup dan soto"));
        kategoriList.add(new Kategori(3, "Daging", "Olahan daging sapi dan ayam"));
        kategoriList.add(new Kategori(4, "Sayuran", "Makanan berbasis sayuran"));
        kategoriList.add(new Kategori(5, "Minuman", "Aneka minuman segar"));

        // Seed users
        userList.add(new User(1, "Administrator", "admin", "Admin", "Aktif"));
        userList.add(new User(2, "Budi Santoso", "kasir1", "Kasir", "Aktif"));
        userList.add(new User(3, "Siti Rahayu", "kasir2", "Kasir", "Nonaktif"));
    }

    public static String[] getKategoriNames() {
        return kategoriList.stream().map(k -> k.nama).toArray(String[]::new);
    }

    public static boolean validateLogin(String username, String password) {
        // Demo credentials
        if (username.equals("admin") && password.equals("admin123")) { loggedUser = "Admin"; return true; }
        if (username.equals("kasir1") && password.equals("kasir123")) { loggedUser = "Kasir"; return true; }
        return false;
    }
}
