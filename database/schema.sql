-- =============================================================================
-- DU AN: HE THONG QUAN LY CUA HANG BACH HOA KHU DAN CU (404-NOT-FOUND)
-- MON HOC: INT4211
-- FILE: database/schema.sql
-- HE QUAN TRI CSDL: PostgreSQL 15+ / Supabase
-- MO TA: Dinh nghia cau truc 13 bang nghiep vu, rang buoc PK/FK/UNIQUE/CHECK
--        va he thong INDEX toi uu cho giai thuat xuat kho FEFO
--        (First Expired, First Out) va tinh lai gop theo gia von thuc te.
--
-- LUU Y AN TOAN DU LIEU (DATA SAFETY):
-- File schema.sql su dung CREATE TABLE IF NOT EXISTS va CREATE INDEX IF NOT EXISTS
-- de dam bao an toan tuyet doi khi chay tren Supabase (khong tu dong xoa du lieu).
-- Neu can RESET toan bo cau truc bang tren moi truong DEV/TEST, chay thu cong
-- khoi lenh DROP TABLE duoi day truoc khi chay schema.sql:
-- /*
-- DROP TABLE IF EXISTS thong_bao, mua_thuong_xuyen, huy_hang, kiem_ke,
--     chi_tiet_don_lo, chi_tiet_don, don_hang, khuyen_mai, lo_hang,
--     san_pham, nha_cung_cap, danh_muc, nguoi_dung CASCADE;
-- */
-- =============================================================================

BEGIN;

-- =============================================================================
-- 1. BANG NGUOI_DUNG
-- Quan ly tai khoan va phan quyen: Chu cua hang, Nhan vien ban hang,
-- Nhan vien giao hang va Khach hang.
-- =============================================================================
CREATE TABLE IF NOT EXISTS nguoi_dung (
    id SERIAL PRIMARY KEY,
    ma_nguoi_dung VARCHAR(30) NOT NULL UNIQUE,
    ho_ten VARCHAR(100) NOT NULL,
    so_dien_thoai VARCHAR(15) NOT NULL UNIQUE
        CONSTRAINT chk_nguoi_dung_sdt
        CHECK (so_dien_thoai ~ '^[0-9]{10,11}$'),
    email VARCHAR(120) UNIQUE,
    mat_khau_hash VARCHAR(255) NOT NULL,
    vai_tro VARCHAR(30) NOT NULL
        CONSTRAINT chk_nguoi_dung_vai_tro
        CHECK (vai_tro IN ('CHU_CUA_HANG', 'NHAN_VIEN_BAN_HANG', 'NHAN_VIEN_GIAO_HANG', 'KHACH_HANG')),
    dia_chi TEXT,
    anh_dai_dien TEXT,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG'
        CONSTRAINT chk_nguoi_dung_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'KHOA')),
    ngay_tao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 2. BANG DANH_MUC
-- Phan nhom cac mat hang bach hoa trong cua hang.
-- =============================================================================
CREATE TABLE IF NOT EXISTS danh_muc (
    id SERIAL PRIMARY KEY,
    ma_danh_muc VARCHAR(30) NOT NULL UNIQUE,
    ten_danh_muc VARCHAR(100) NOT NULL UNIQUE,
    mo_ta TEXT,
    hinh_anh TEXT,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG'
        CONSTRAINT chk_danh_muc_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'NGUNG_HOAT_DONG')),
    ngay_tao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 3. BANG NHA_CUNG_CAP
-- Quan ly thong tin cac don vi cung ung hang hoa cho cua hang.
-- =============================================================================
CREATE TABLE IF NOT EXISTS nha_cung_cap (
    id SERIAL PRIMARY KEY,
    ma_ncc VARCHAR(30) NOT NULL UNIQUE,
    ten_ncc VARCHAR(150) NOT NULL,
    nguoi_lien_he VARCHAR(100),
    so_dien_thoai VARCHAR(15) NOT NULL UNIQUE
        CONSTRAINT chk_ncc_sdt
        CHECK (so_dien_thoai ~ '^[0-9]{10,11}$'),
    email VARCHAR(120),
    dia_chi TEXT NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'HOAT_DONG'
        CONSTRAINT chk_ncc_trang_thai
        CHECK (trang_thai IN ('HOAT_DONG', 'NGUNG_HOP_TAC')),
    ngay_tao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 4. BANG SAN_PHAM
-- Thong tin danh muc san pham, ma vach (barcode), don vi tinh, gia ban niem yet
-- va dinh muc ton kho toi thieu de canh bao nhap hang.
-- =============================================================================
CREATE TABLE IF NOT EXISTS san_pham (
    id SERIAL PRIMARY KEY,
    ma_san_pham VARCHAR(30) NOT NULL UNIQUE,
    ma_vach VARCHAR(50) NOT NULL UNIQUE,
    danh_muc_id INTEGER NOT NULL
        CONSTRAINT fk_san_pham_danh_muc
        REFERENCES danh_muc(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    ten_san_pham VARCHAR(180) NOT NULL,
    don_vi_tinh VARCHAR(30) NOT NULL
        CONSTRAINT chk_san_pham_don_vi_tinh
        CHECK (length(trim(don_vi_tinh)) > 0),
    gia_ban NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_san_pham_gia_ban
        CHECK (gia_ban >= 0),
    ton_toi_thieu INTEGER NOT NULL DEFAULT 10
        CONSTRAINT chk_san_pham_ton_toi_thieu
        CHECK (ton_toi_thieu >= 0),
    hinh_anh TEXT,
    mo_ta TEXT,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_BAN'
        CONSTRAINT chk_san_pham_trang_thai
        CHECK (trang_thai IN ('DANG_BAN', 'NGUNG_BAN')),
    ngay_tao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_cap_nhat TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =============================================================================
-- 5. BANG LO_HANG
-- Quan ly ton kho theo tung lo nhap: so luong nhap, so luong con, gia nhap,
-- ngay nhap, han su dung va trang thai lo. Day la bang cot loi cho FEFO.
-- =============================================================================
CREATE TABLE IF NOT EXISTS lo_hang (
    id SERIAL PRIMARY KEY,
    ma_lo VARCHAR(40) NOT NULL UNIQUE,
    san_pham_id INTEGER NOT NULL
        CONSTRAINT fk_lo_hang_san_pham
        REFERENCES san_pham(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nha_cung_cap_id INTEGER NOT NULL
        CONSTRAINT fk_lo_hang_nha_cung_cap
        REFERENCES nha_cung_cap(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nguoi_nhap_id INTEGER
        CONSTRAINT fk_lo_hang_nguoi_nhap
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    so_luong_nhap INTEGER NOT NULL
        CONSTRAINT chk_lo_hang_so_luong_nhap
        CHECK (so_luong_nhap > 0),
    so_luong_con INTEGER NOT NULL
        CONSTRAINT chk_lo_hang_so_luong_con
        CHECK (so_luong_con >= 0 AND so_luong_con <= so_luong_nhap),
    gia_nhap NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_lo_hang_gia_nhap
        CHECK (gia_nhap >= 0),
    ngay_san_xuat DATE,
    ngay_nhap TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    han_su_dung DATE NOT NULL,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'CON_HANG'
        CONSTRAINT chk_lo_hang_trang_thai
        CHECK (trang_thai IN ('CON_HANG', 'HET_HANG', 'HET_HAN', 'DA_HUY')),
    ghi_chu TEXT,
    CONSTRAINT uq_lo_hang_id_san_pham
        UNIQUE (id, san_pham_id),
    CONSTRAINT chk_lo_hang_ngay_sx_hsd
        CHECK (ngay_san_xuat IS NULL OR ngay_san_xuat <= han_su_dung),
    CONSTRAINT chk_lo_hang_logic_ton_kho
        CHECK (
            (trang_thai IN ('HET_HANG', 'DA_HUY') AND so_luong_con = 0)
            OR (trang_thai IN ('CON_HANG', 'HET_HAN') AND so_luong_con > 0)
        )
);

-- =============================================================================
-- 6. BANG KHUYEN_MAI
-- Quan ly chuong trinh khuyen mai: xa hang lo can han, giam gia san pham
-- hoac giam gia tren tong don hang.
-- =============================================================================
CREATE TABLE IF NOT EXISTS khuyen_mai (
    id SERIAL PRIMARY KEY,
    ma_khuyen_mai VARCHAR(40) NOT NULL UNIQUE,
    ten_khuyen_mai VARCHAR(150) NOT NULL,
    mo_ta TEXT,
    loai_khuyen_mai VARCHAR(20) NOT NULL
        CONSTRAINT chk_khuyen_mai_loai
        CHECK (loai_khuyen_mai IN ('PHAN_TRAM', 'SO_TIEN')),
    pham_vi_ap_dung VARCHAR(20) NOT NULL DEFAULT 'LO_CAN_HAN'
        CONSTRAINT chk_khuyen_mai_pham_vi
        CHECK (pham_vi_ap_dung IN ('SAN_PHAM', 'LO_CAN_HAN', 'DON_HANG')),
    san_pham_id INTEGER
        CONSTRAINT fk_khuyen_mai_san_pham
        REFERENCES san_pham(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    lo_hang_id INTEGER
        CONSTRAINT fk_khuyen_mai_lo_hang
        REFERENCES lo_hang(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    gia_tri_giam NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_khuyen_mai_gia_tri
        CHECK (gia_tri_giam > 0),
    giam_toi_da NUMERIC(12, 2)
        CONSTRAINT chk_khuyen_mai_giam_toi_da
        CHECK (giam_toi_da IS NULL OR giam_toi_da >= 0),
    don_hang_toi_thieu NUMERIC(12, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_khuyen_mai_don_toi_thieu
        CHECK (don_hang_toi_thieu >= 0),
    ngay_bat_dau TIMESTAMPTZ NOT NULL,
    ngay_ket_thuc TIMESTAMPTZ NOT NULL,
    so_luong_gioi_han INTEGER
        CONSTRAINT chk_khuyen_mai_gioi_han
        CHECK (so_luong_gioi_han IS NULL OR so_luong_gioi_han > 0),
    so_luong_da_dung INTEGER NOT NULL DEFAULT 0
        CONSTRAINT chk_khuyen_mai_da_dung
        CHECK (so_luong_da_dung >= 0),
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DANG_AP_DUNG'
        CONSTRAINT chk_khuyen_mai_trang_thai
        CHECK (trang_thai IN ('CHUA_BAT_DAU', 'DANG_AP_DUNG', 'KET_THUC', 'TAM_DUNG')),
    nguoi_tao_id INTEGER
        CONSTRAINT fk_khuyen_mai_nguoi_tao
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_khuyen_mai_lo_khop_san_pham
        FOREIGN KEY (lo_hang_id, san_pham_id)
        REFERENCES lo_hang(id, san_pham_id) ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_khuyen_mai_logic_pham_vi
        CHECK (
            (pham_vi_ap_dung = 'LO_CAN_HAN' AND lo_hang_id IS NOT NULL AND san_pham_id IS NOT NULL)
            OR (pham_vi_ap_dung = 'SAN_PHAM' AND san_pham_id IS NOT NULL AND lo_hang_id IS NULL)
            OR (pham_vi_ap_dung = 'DON_HANG' AND san_pham_id IS NULL AND lo_hang_id IS NULL)
        ),
    CONSTRAINT chk_khuyen_mai_thoi_gian
        CHECK (ngay_ket_thuc >= ngay_bat_dau),
    CONSTRAINT chk_khuyen_mai_ty_le_phan_tram
        CHECK (loai_khuyen_mai <> 'PHAN_TRAM' OR (gia_tri_giam > 0 AND gia_tri_giam <= 100)),
    CONSTRAINT chk_khuyen_mai_han_muc_su_dung
        CHECK (so_luong_gioi_han IS NULL OR so_luong_da_dung <= so_luong_gioi_han)
);

-- =============================================================================
-- 7. BANG DON_HANG
-- Quan ly don ban hang tai quay (POS) va don dat giao hang qua ung dung Client.
-- =============================================================================
CREATE TABLE IF NOT EXISTS don_hang (
    id SERIAL PRIMARY KEY,
    ma_don_hang VARCHAR(40) NOT NULL UNIQUE,
    khach_hang_id INTEGER
        CONSTRAINT fk_don_hang_khach_hang
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nhan_vien_ban_id INTEGER
        CONSTRAINT fk_don_hang_nhan_vien_ban
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nhan_vien_giao_id INTEGER
        CONSTRAINT fk_don_hang_nhan_vien_giao
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    khuyen_mai_id INTEGER
        CONSTRAINT fk_don_hang_khuyen_mai
        REFERENCES khuyen_mai(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    loai_don VARCHAR(20) NOT NULL
        CONSTRAINT chk_don_hang_loai_don
        CHECK (loai_don IN ('TAI_QUAY', 'GIAO_HANG')),
    trang_thai VARCHAR(30) NOT NULL
        CONSTRAINT chk_don_hang_trang_thai
        CHECK (trang_thai IN ('CHO_XAC_NHAN', 'DA_XAC_NHAN', 'DANG_GIAO', 'HOAN_THANH', 'DA_HUY')),
    phuong_thuc_thanh_toan VARCHAR(30) NOT NULL DEFAULT 'TIEN_MAT'
        CONSTRAINT chk_don_hang_pt_thanh_toan
        CHECK (phuong_thuc_thanh_toan IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'VI_DIEN_TU')),
    trang_thai_thanh_toan VARCHAR(20) NOT NULL DEFAULT 'DA_THANH_TOAN'
        CONSTRAINT chk_don_hang_tt_thanh_toan
        CHECK (trang_thai_thanh_toan IN ('CHUA_THANH_TOAN', 'DA_THANH_TOAN', 'HOAN_TIEN')),
    nguoi_nhan VARCHAR(100),
    so_dien_thoai_nhan VARCHAR(15),
    dia_chi_giao TEXT,
    khung_gio_giao VARCHAR(50),
    tong_tien_hang NUMERIC(12, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_don_hang_tong_tien_hang
        CHECK (tong_tien_hang >= 0),
    tien_giam_gia NUMERIC(12, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_don_hang_tien_giam_gia
        CHECK (tien_giam_gia >= 0),
    phi_giao_hang NUMERIC(12, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_don_hang_phi_giao_hang
        CHECK (phi_giao_hang >= 0),
    tong_thanh_toan NUMERIC(12, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_don_hang_tong_thanh_toan
        CHECK (tong_thanh_toan >= 0),
    ghi_chu TEXT,
    ngay_dat TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_giao_thanh_cong TIMESTAMPTZ,
    ngay_cap_nhat TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_don_hang_giam_gia_hop_le
        CHECK (tien_giam_gia <= tong_tien_hang),
    CONSTRAINT chk_don_hang_phi_giao_tai_quay
        CHECK (loai_don <> 'TAI_QUAY' OR phi_giao_hang = 0),
    CONSTRAINT chk_don_hang_cong_thuc_tien
        CHECK (tong_thanh_toan = tong_tien_hang - tien_giam_gia + phi_giao_hang)
);

-- =============================================================================
-- 8. BANG CHI_TIET_DON
-- Chi tiet tung mat hang trong don hang (so luong mua, don gia ban, giam gia).
-- =============================================================================
CREATE TABLE IF NOT EXISTS chi_tiet_don (
    id SERIAL PRIMARY KEY,
    don_hang_id INTEGER NOT NULL
        CONSTRAINT fk_chi_tiet_don_don_hang
        REFERENCES don_hang(id) ON UPDATE CASCADE ON DELETE CASCADE,
    san_pham_id INTEGER NOT NULL
        CONSTRAINT fk_chi_tiet_don_san_pham
        REFERENCES san_pham(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    khuyen_mai_id INTEGER
        CONSTRAINT fk_chi_tiet_don_khuyen_mai
        REFERENCES khuyen_mai(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    so_luong INTEGER NOT NULL
        CONSTRAINT chk_chi_tiet_don_so_luong
        CHECK (so_luong > 0),
    don_gia_ban NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_chi_tiet_don_gia_ban
        CHECK (don_gia_ban >= 0),
    tien_giam NUMERIC(12, 2) NOT NULL DEFAULT 0
        CONSTRAINT chk_chi_tiet_don_tien_giam
        CHECK (tien_giam >= 0),
    thanh_tien NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_chi_tiet_don_thanh_tien
        CHECK (thanh_tien >= 0),
    CONSTRAINT uq_chi_tiet_don_don_sp
        UNIQUE (don_hang_id, san_pham_id),
    CONSTRAINT chk_chi_tiet_don_cong_thuc
        CHECK (thanh_tien = (so_luong * don_gia_ban) - tien_giam)
);

-- =============================================================================
-- 9. BANG CHI_TIET_DON_LO
-- Bang lien ket giua dong chi tiet don hang va cac lo hang da xuat theo FEFO.
-- Luu lai gia_nhap_tai_thoi_diem_ban de tinh chinh xac gia von va lai gop.
-- =============================================================================
CREATE TABLE IF NOT EXISTS chi_tiet_don_lo (
    id SERIAL PRIMARY KEY,
    chi_tiet_don_id INTEGER NOT NULL
        CONSTRAINT fk_ctdl_chi_tiet_don
        REFERENCES chi_tiet_don(id) ON UPDATE CASCADE ON DELETE CASCADE,
    lo_hang_id INTEGER NOT NULL
        CONSTRAINT fk_ctdl_lo_hang
        REFERENCES lo_hang(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    so_luong_xuat INTEGER NOT NULL
        CONSTRAINT chk_ctdl_so_luong_xuat
        CHECK (so_luong_xuat > 0),
    gia_nhap_tai_thoi_diem_ban NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_ctdl_gia_nhap
        CHECK (gia_nhap_tai_thoi_diem_ban >= 0),
    thanh_tien_gia_von NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_ctdl_thanh_tien_gia_von
        CHECK (thanh_tien_gia_von >= 0),
    ngay_xuat TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_chi_tiet_don_lo
        UNIQUE (chi_tiet_don_id, lo_hang_id),
    CONSTRAINT chk_ctdl_cong_thuc_gia_von
        CHECK (thanh_tien_gia_von = so_luong_xuat * gia_nhap_tai_thoi_diem_ban)
);

-- =============================================================================
-- 10. BANG KIEM_KE
-- Ghi nhan dot kiem ke ton kho thuc te theo tung lo hang va chenh lech.
-- =============================================================================
CREATE TABLE IF NOT EXISTS kiem_ke (
    id SERIAL PRIMARY KEY,
    ma_kiem_ke VARCHAR(40) NOT NULL UNIQUE,
    lo_hang_id INTEGER NOT NULL
        CONSTRAINT fk_kiem_ke_lo_hang
        REFERENCES lo_hang(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nguoi_kiem_ke_id INTEGER NOT NULL
        CONSTRAINT fk_kiem_ke_nguoi_dung
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    so_luong_he_thong INTEGER NOT NULL
        CONSTRAINT chk_kiem_ke_sl_he_thong
        CHECK (so_luong_he_thong >= 0),
    so_luong_thuc_te INTEGER NOT NULL
        CONSTRAINT chk_kiem_ke_sl_thuc_te
        CHECK (so_luong_thuc_te >= 0),
    chenh_lech INTEGER NOT NULL,
    ly_do TEXT,
    trang_thai VARCHAR(20) NOT NULL DEFAULT 'DA_CAN_BANG'
        CONSTRAINT chk_kiem_ke_trang_thai
        CHECK (trang_thai IN ('CHO_DUYET', 'DA_CAN_BANG')),
    ngay_kiem_ke TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_kiem_ke_cong_thuc_chenh_lech
        CHECK (chenh_lech = so_luong_thuc_te - so_luong_he_thong)
);

-- =============================================================================
-- 11. BANG HUY_HANG
-- Ghi nhan lich su tieu huy cac lo hang het han su dung hoac hu hong.
-- =============================================================================
CREATE TABLE IF NOT EXISTS huy_hang (
    id SERIAL PRIMARY KEY,
    ma_phieu_huy VARCHAR(40) NOT NULL UNIQUE,
    lo_hang_id INTEGER NOT NULL
        CONSTRAINT fk_huy_hang_lo_hang
        REFERENCES lo_hang(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nguoi_huy_id INTEGER NOT NULL
        CONSTRAINT fk_huy_hang_nguoi_dung
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    so_luong_huy INTEGER NOT NULL
        CONSTRAINT chk_huy_hang_so_luong
        CHECK (so_luong_huy > 0),
    don_gia_von NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_huy_hang_don_gia_von
        CHECK (don_gia_von >= 0),
    tong_gia_tri_huy NUMERIC(12, 2) NOT NULL
        CONSTRAINT chk_huy_hang_tong_gia_tri
        CHECK (tong_gia_tri_huy >= 0),
    nguyen_nhan VARCHAR(30) NOT NULL DEFAULT 'HET_HAN'
        CONSTRAINT chk_huy_hang_nguyen_nhan
        CHECK (nguyen_nhan IN ('HET_HAN', 'HU_HONG', 'KHAC')),
    ghi_chu TEXT,
    ngay_huy TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_huy_hang_cong_thuc_gia_tri
        CHECK (tong_gia_tri_huy = so_luong_huy * don_gia_von)
);

-- =============================================================================
-- 12. BANG MUA_THUONG_XUYEN
-- Danh sach san pham khach hang thuong xuyen mua de goi y dat nhanh va
-- dang ky nhan thong bao khuyen mai khi co lo hang can han giam gia.
-- =============================================================================
CREATE TABLE IF NOT EXISTS mua_thuong_xuyen (
    id SERIAL PRIMARY KEY,
    khach_hang_id INTEGER NOT NULL
        CONSTRAINT fk_mtx_khach_hang
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE CASCADE,
    san_pham_id INTEGER NOT NULL
        CONSTRAINT fk_mtx_san_pham
        REFERENCES san_pham(id) ON UPDATE CASCADE ON DELETE CASCADE,
    so_luong_mac_dinh INTEGER NOT NULL DEFAULT 1
        CONSTRAINT chk_mtx_so_luong_mac_dinh
        CHECK (so_luong_mac_dinh > 0),
    so_lan_mua INTEGER NOT NULL DEFAULT 1
        CONSTRAINT chk_mtx_so_lan_mua
        CHECK (so_lan_mua > 0),
    lan_mua_cuoi TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nhan_thong_bao_km BOOLEAN NOT NULL DEFAULT TRUE,
    ngay_tao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_mua_thuong_xuyen_kh_sp
        UNIQUE (khach_hang_id, san_pham_id)
);

-- =============================================================================
-- 13. BANG THONG_BAO
-- Luu tru thong bao gui cho khach hang (khuyen mai can han, trang thai don giao)
-- va thong bao canh bao kho cho chu cua hang / nhan vien.
-- =============================================================================
CREATE TABLE IF NOT EXISTS thong_bao (
    id SERIAL PRIMARY KEY,
    nguoi_nhan_id INTEGER NOT NULL
        CONSTRAINT fk_thong_bao_nguoi_nhan
        REFERENCES nguoi_dung(id) ON UPDATE CASCADE ON DELETE CASCADE,
    loai_thong_bao VARCHAR(30) NOT NULL
        CONSTRAINT chk_thong_bao_loai
        CHECK (loai_thong_bao IN ('KHUYEN_MAI', 'DON_HANG', 'CANH_BAO_KHO', 'HE_THONG')),
    tieu_de VARCHAR(200) NOT NULL,
    noi_dung TEXT NOT NULL,
    don_hang_id INTEGER
        CONSTRAINT fk_thong_bao_don_hang
        REFERENCES don_hang(id) ON UPDATE CASCADE ON DELETE SET NULL,
    khuyen_mai_id INTEGER
        CONSTRAINT fk_thong_bao_khuyen_mai
        REFERENCES khuyen_mai(id) ON UPDATE CASCADE ON DELETE SET NULL,
    lo_hang_id INTEGER
        CONSTRAINT fk_thong_bao_lo_hang
        REFERENCES lo_hang(id) ON UPDATE CASCADE ON DELETE SET NULL,
    da_doc BOOLEAN NOT NULL DEFAULT FALSE,
    ngay_tao TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ngay_doc TIMESTAMPTZ
);

-- =============================================================================
-- HE THONG CHI MUC (INDEXES) TOI UU TRUY VAN VA GIAI THUAT FEFO
-- =============================================================================

-- Index cho nguoi_dung
CREATE INDEX IF NOT EXISTS idx_nguoi_dung_vai_tro ON nguoi_dung(vai_tro);
CREATE INDEX IF NOT EXISTS idx_nguoi_dung_trang_thai ON nguoi_dung(trang_thai);

-- Index cho san_pham
CREATE INDEX IF NOT EXISTS idx_san_pham_danh_muc ON san_pham(danh_muc_id);
CREATE INDEX IF NOT EXISTS idx_san_pham_ten ON san_pham(ten_san_pham);
CREATE INDEX IF NOT EXISTS idx_san_pham_trang_thai ON san_pham(trang_thai);

-- Index cot loi phuc vu truy van va khoa dong (SELECT ... FOR UPDATE) theo FEFO:
-- Uu tien lo CON_HANG co so_luong_con > 0 va han_su_dung gan nhat truoc
CREATE INDEX IF NOT EXISTS idx_lo_hang_fefo
    ON lo_hang(san_pham_id, han_su_dung ASC, ngay_nhap ASC, id ASC)
    WHERE trang_thai = 'CON_HANG' AND so_luong_con > 0;

CREATE INDEX IF NOT EXISTS idx_lo_hang_han_su_dung ON lo_hang(han_su_dung);
CREATE INDEX IF NOT EXISTS idx_lo_hang_nha_cung_cap ON lo_hang(nha_cung_cap_id);
CREATE INDEX IF NOT EXISTS idx_lo_hang_trang_thai ON lo_hang(trang_thai);

-- Index cho khuyen_mai
CREATE INDEX IF NOT EXISTS idx_khuyen_mai_san_pham ON khuyen_mai(san_pham_id);
CREATE INDEX IF NOT EXISTS idx_khuyen_mai_lo_hang ON khuyen_mai(lo_hang_id);
CREATE INDEX IF NOT EXISTS idx_khuyen_mai_hieu_luc ON khuyen_mai(trang_thai, ngay_bat_dau, ngay_ket_thuc);

-- Index cho don_hang
CREATE INDEX IF NOT EXISTS idx_don_hang_khach_hang ON don_hang(khach_hang_id, ngay_dat DESC);
CREATE INDEX IF NOT EXISTS idx_don_hang_nhan_vien_ban ON don_hang(nhan_vien_ban_id);
CREATE INDEX IF NOT EXISTS idx_don_hang_nhan_vien_giao ON don_hang(nhan_vien_giao_id, trang_thai);
CREATE INDEX IF NOT EXISTS idx_don_hang_ngay_dat ON don_hang(ngay_dat DESC);
CREATE INDEX IF NOT EXISTS idx_don_hang_loai_trang_thai ON don_hang(loai_don, trang_thai);

-- Index cho chi_tiet_don va chi_tiet_don_lo (bao cao doanh thu & lai gop)
CREATE INDEX IF NOT EXISTS idx_chi_tiet_don_don_hang ON chi_tiet_don(don_hang_id);
CREATE INDEX IF NOT EXISTS idx_chi_tiet_don_san_pham ON chi_tiet_don(san_pham_id);
CREATE INDEX IF NOT EXISTS idx_ctdl_chi_tiet_don ON chi_tiet_don_lo(chi_tiet_don_id);
CREATE INDEX IF NOT EXISTS idx_ctdl_lo_hang ON chi_tiet_don_lo(lo_hang_id);

-- Index cho kiem_ke, huy_hang, mua_thuong_xuyen, thong_bao
CREATE INDEX IF NOT EXISTS idx_kiem_ke_lo_hang ON kiem_ke(lo_hang_id, ngay_kiem_ke DESC);
CREATE INDEX IF NOT EXISTS idx_huy_hang_lo_hang ON huy_hang(lo_hang_id, ngay_huy DESC);
CREATE INDEX IF NOT EXISTS idx_mtx_khach_hang ON mua_thuong_xuyen(khach_hang_id);
CREATE INDEX IF NOT EXISTS idx_mtx_san_pham ON mua_thuong_xuyen(san_pham_id);
CREATE INDEX IF NOT EXISTS idx_thong_bao_nguoi_nhan ON thong_bao(nguoi_nhan_id, da_doc, ngay_tao DESC);

COMMIT;
