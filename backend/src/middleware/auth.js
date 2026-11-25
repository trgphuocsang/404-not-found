const jwt = require("jsonwebtoken");
const { httpError } = require("./errorHandler");

// Lớp 1: xác thực - "bạn là ai?"
// Client gửi header:  Authorization: Bearer <token>
function xacThuc(req, res, next) {
    const header = req.headers.authorization || "";
    const [kieu, token] = header.split(" ");

    if (kieu !== "Bearer" || !token) {
        return next(httpError(401, "Thiếu token đăng nhập"));
    }

    try {
        // verify kiểm tra chữ ký (đúng JWT_SECRET) và hạn dùng (exp) của token
        const payload = jwt.verify(token, process.env.JWT_SECRET);
        // Gắn thông tin người dùng vào req để các hàm phía sau dùng lại
        req.nguoiDung = { id: payload.id, vaiTro: payload.vaiTro };
        next();
    } catch (err) {
        next(httpError(401, "Token không hợp lệ hoặc đã hết hạn"));
    }
}

// Lớp 2: phân quyền - "bạn được làm gì?"
// Dùng sau xacThuc:  router.post("/", xacThuc, phanQuyen("CHU_CUA_HANG"), handler)
// Vai trò viết HOA, khớp CHECK trong schema: CHU_CUA_HANG, NHAN_VIEN_BAN_HANG,
// NHAN_VIEN_GIAO_HANG, KHACH_HANG
function phanQuyen(...vaiTroChoPhep) {
    return (req, res, next) => {
        if (!req.nguoiDung || !vaiTroChoPhep.includes(req.nguoiDung.vaiTro)) {
            return next(httpError(403, "Bạn không có quyền thực hiện thao tác này"));
        }
        next();
    };
}

module.exports = { xacThuc, phanQuyen };
