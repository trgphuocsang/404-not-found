const express = require("express");
const bcrypt = require("bcrypt");
const jwt = require("jsonwebtoken");
const pool = require("../db");
const { httpError } = require("../middleware/errorHandler");

const router = express.Router();

// Số vòng băm của bcrypt: càng cao càng an toàn nhưng càng chậm. 10 khớp với seed của TV2.
const SALT_ROUNDS = 10;

// Khớp ràng buộc chk_nguoi_dung_sdt trong schema: 10-11 chữ số
const REGEX_SDT = /^[0-9]{10,11}$/;
const REGEX_EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

// ---------- ĐĂNG KÝ: POST /auth/dang-ky ----------
// Chỉ khách hàng được tự đăng ký => vai trò luôn là "KHACH_HANG", KHÔNG đọc từ body.
router.post("/dang-ky", async (req, res, next) => {
    try {
        const { soDienThoai, matKhau, hoTen } = req.body;
        // email là tuỳ chọn: chuỗi rỗng hoặc không gửi thì lưu NULL
        const email = req.body.email ? String(req.body.email).trim() : null;

        // 1) Kiểm tra đầu vào, sai thì trả 400
        if (!soDienThoai || !matKhau || !hoTen) {
            throw httpError(400, "Cần nhập đủ soDienThoai, matKhau, hoTen");
        }
        if (!REGEX_SDT.test(soDienThoai)) {
            throw httpError(400, "Số điện thoại phải gồm 10-11 chữ số");
        }
        if (matKhau.length < 6) {
            throw httpError(400, "Mật khẩu phải có ít nhất 6 ký tự");
        }
        if (email && !REGEX_EMAIL.test(email)) {
            throw httpError(400, "Email không hợp lệ");
        }

        // 2) Băm mật khẩu: DB chỉ lưu bản băm, không bao giờ lưu mật khẩu gốc
        const matKhauHash = await bcrypt.hash(matKhau, SALT_ROUNDS);

        // 3) Lưu vào DB bằng SQL tham số ($1, $2...) để chống SQL injection.
        // Lấy số id kế tiếp từ bộ đếm của DB (nextval luôn cho số duy nhất, kể cả khi
        // hai người đăng ký cùng lúc), rồi ghép thành mã ND + 3 chữ số, ví dụ ND010.
        const ketQua = await pool.query(
            `WITH moi AS (
                 SELECT nextval(pg_get_serial_sequence('nguoi_dung', 'id')) AS id
             )
             INSERT INTO nguoi_dung
                 (id, ma_nguoi_dung, ho_ten, so_dien_thoai, email, mat_khau_hash, vai_tro)
             SELECT id, 'ND' || LPAD(id::text, 3, '0'), $1, $2, $3, $4, 'KHACH_HANG'
             FROM moi
             RETURNING id, ma_nguoi_dung, ho_ten, so_dien_thoai, email, vai_tro`,
            [hoTen.trim(), soDienThoai, email, matKhauHash]
        );

        res.status(201).json(ketQua.rows[0]);
    } catch (err) {
        // 23505 = vi phạm UNIQUE của PostgreSQL => 409. err.constraint cho biết cột nào trùng.
        if (err.code === "23505") {
            const trungEmail = String(err.constraint || "").includes("email");
            return next(
                httpError(409, trungEmail ? "Email đã được sử dụng" : "Số điện thoại đã được đăng ký")
            );
        }
        next(err);
    }
});

// ---------- ĐĂNG NHẬP: POST /auth/dang-nhap ----------
router.post("/dang-nhap", async (req, res, next) => {
    try {
        const { soDienThoai, matKhau } = req.body;

        if (!soDienThoai || !matKhau) {
            throw httpError(400, "Cần nhập soDienThoai và matKhau");
        }

        const ketQua = await pool.query(
            `SELECT id, ma_nguoi_dung, ho_ten, so_dien_thoai, email, mat_khau_hash, vai_tro, trang_thai
             FROM nguoi_dung WHERE so_dien_thoai = $1`,
            [soDienThoai]
        );
        const nguoiDung = ketQua.rows[0];

        // So khớp mật khẩu với bản băm. Không có user hoặc sai mật khẩu đều trả CÙNG
        // một thông báo, để kẻ xấu không dò được số điện thoại nào tồn tại.
        const dung = nguoiDung && (await bcrypt.compare(matKhau, nguoiDung.mat_khau_hash));
        if (!dung) {
            throw httpError(401, "Sai số điện thoại hoặc mật khẩu");
        }

        // Mật khẩu đúng nhưng tài khoản bị khóa thì không cho vào (403: biết bạn là ai nhưng cấm)
        if (nguoiDung.trang_thai !== "HOAT_DONG") {
            throw httpError(403, "Tài khoản đã bị khóa");
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
                maNguoiDung: nguoiDung.ma_nguoi_dung,
                hoTen: nguoiDung.ho_ten,
                soDienThoai: nguoiDung.so_dien_thoai,
                email: nguoiDung.email,
                vaiTro: nguoiDung.vai_tro
            }
        });
    } catch (err) {
        next(err);
    }
});

module.exports = router;
