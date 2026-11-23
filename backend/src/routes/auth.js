const express = require("express");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const pool = require("../db");
const { httpError } = require("../middleware/errorHandler");

const router = express.Router();

// Số vòng băm của bcrypt: càng cao càng an toàn nhưng càng chậm. 10 là mức phổ biến.
const SALT_ROUNDS = 10;

// ---------- ĐĂNG KÝ: POST /auth/dang-ky ----------
// Chỉ khách hàng được tự đăng ký => vai trò luôn là "khach_hang", KHÔNG đọc từ body.
router.post("/dang-ky", async (req, res, next) => {
    try {
        const { tenDangNhap, matKhau, hoTen } = req.body;

        // 1) Kiểm tra đầu vào, sai thì trả 400
        if (!tenDangNhap || !matKhau || !hoTen) {
            throw httpError(400, "Cần nhập đủ tenDangNhap, matKhau, hoTen");
        }
        if (matKhau.length < 6) {
            throw httpError(400, "Mật khẩu phải có ít nhất 6 ký tự");
        }

        // 2) Băm mật khẩu: DB chỉ lưu bản băm, không bao giờ lưu mật khẩu gốc
        const matKhauHash = await bcrypt.hash(matKhau, SALT_ROUNDS);

        // 3) Lưu vào DB bằng SQL tham số ($1, $2...) để chống SQL injection
        const ketQua = await pool.query(
            `INSERT INTO nguoi_dung (ten_dang_nhap, mat_khau_hash, ho_ten, vai_tro)
             VALUES ($1, $2, $3, 'khach_hang')
             RETURNING id, ten_dang_nhap, ho_ten, vai_tro`,
            [tenDangNhap, matKhauHash, hoTen]
        );

        res.status(201).json(ketQua.rows[0]);
    } catch (err) {
        // 23505 = lỗi vi phạm UNIQUE của PostgreSQL => tên đăng nhập đã có => 409
        if (err.code === "23505") {
            return next(httpError(409, "Tên đăng nhập đã tồn tại"));
        }
        next(err);
    }
});

// ---------- ĐĂNG NHẬP: POST /auth/dang-nhap ----------
router.post("/dang-nhap", async (req, res, next) => {
    try {
        const { tenDangNhap, matKhau } = req.body;

        if (!tenDangNhap || !matKhau) {
            throw httpError(400, "Cần nhập tenDangNhap và matKhau");
        }

        const ketQua = await pool.query(
            `SELECT id, ten_dang_nhap, mat_khau_hash, ho_ten, vai_tro
             FROM nguoi_dung WHERE ten_dang_nhap = $1`,
            [tenDangNhap]
        );
        const nguoiDung = ketQua.rows[0];

        // So khớp mật khẩu với bản băm. Không có user hoặc sai mật khẩu đều trả CÙNG
        // một thông báo, để kẻ xấu không dò được tên đăng nhập nào tồn tại.
        const dung = nguoiDung && (await bcrypt.compare(matKhau, nguoiDung.mat_khau_hash));
        if (!dung) {
            throw httpError(401, "Sai tên đăng nhập hoặc mật khẩu");
        }

        // Tạo JWT: payload chứa id và vai trò, ký bằng JWT_SECRET, có hạn dùng
        const token = jwt.sign(
            { id: nguoiDung.id, vaiTro: nguoiDung.vai_tro },
            process.env.JWT_SECRET,
            { expiresIn: process.env.JWT_EXPIRES_IN || "1d" }
        );

        res.status(200).json({
            token,
            nguoiDung: {
                id: nguoiDung.id,
                tenDangNhap: nguoiDung.ten_dang_nhap,
                hoTen: nguoiDung.ho_ten,
                vaiTro: nguoiDung.vai_tro
            }
        });
    } catch (err) {
        next(err);
    }
});

module.exports = router;
