# FE API Spec - Profile & Account

Tài liệu này mô tả các API cần dùng để làm khu vực profile và quản lý tài khoản ở FE, không cần mở source BE mỗi lần tích hợp.

## 1. Tổng quan

Base URL mặc định:

- `http://localhost:8080`

Nhóm API chính:

- `POST /api/auth/login` để đăng nhập
- `POST /api/auth/google-login` để đăng nhập bằng Google
- `POST /api/auth/register` để đăng ký tài khoản thường
- `POST /api/auth/send-otp` và `POST /api/auth/verify-otp` để kích hoạt tài khoản
- `GET /api/auth/refresh` để refresh access token
- `POST /api/auth/logout` để đăng xuất
- `GET /api/users/me` hoặc `GET /api/users` để lấy thông tin account hiện tại
- `GET /api/users/profile` để lấy profile chi tiết
- `PUT /api/users/profile` để cập nhật profile
- `PUT /api/users/update-info` để cập nhật tên cơ bản
- `PUT /api/users/change-password` để đổi mật khẩu
- `POST /api/users/forgot-password` để lấy mật khẩu mới qua email
- `POST /api/users/ping` để cập nhật trạng thái online/activity

## 2. Cách auth hoạt động

### Access token

- Sau khi login, BE trả về `accessToken` trong response body.
- FE phải lưu token này và gửi ở header:

```http
Authorization: Bearer <accessToken>
```

### Refresh token

- BE set refresh token vào cookie `refreshToken`.
- Cookie này là `HttpOnly` và `Secure`.
- FE không đọc cookie bằng JavaScript, trình duyệt sẽ tự gửi cookie khi gọi refresh/logout nếu cùng domain và đúng điều kiện cookie.

### Device ID

- Login response trả thêm header `X-Device-Id`.
- FE nên lưu giá trị này, rồi gửi lại ở các request:
  - `GET /api/auth/refresh`
  - `POST /api/auth/logout`
- Nếu không có `X-Device-Id`, BE sẽ tự sinh mới trong login.

### Lưu ý quan trọng khi chạy local

- Cookie refresh đang được set `secure=true`.
- Nếu FE chạy trên `http://localhost`, trình duyệt có thể không lưu cookie refresh.
- Khi test local, nên dùng HTTPS hoặc đổi cấu hình BE cho môi trường dev nếu cần.

## 3. Chuẩn response

Phần lớn API trả về dạng envelope:

```json
{
  "status": 200,
  "message": "Success",
  "data": { ... }
}
```

Một số endpoint trả object trực tiếp qua `ResponseEntity`, nhưng vẫn bị wrapper thành `ApiResponse` nếu không phải `ApiResponse` sẵn.

### ApiResponse fields

- `status`: số nguyên, thường là `200` khi thành công
- `message`: chuỗi thông báo
- `data`: payload thực

## 4. Auth API

### 4.1 Login

`POST /api/auth/login`

Request body:

```json
{
  "email": "user@example.com",
  "password": "123456",
  "roleName": "ROLE_USER"
}
```

Header tùy chọn:

```http
X-Device-Id: device-001
```

Response:

- Header `Set-Cookie`: refresh token
- Header `X-Device-Id`: device id dùng cho lần sau
- Body:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "accessToken": "<jwt-access-token>"
  }
}
```

FE flow sau login:

1. Lưu `accessToken`.
2. Lưu `X-Device-Id` từ response header.
3. Dùng `Authorization: Bearer <accessToken>` cho các API cần đăng nhập.

### 4.2 Refresh access token

`GET /api/auth/refresh`

Headers:

```http
X-Device-Id: device-001
```

Cookie bắt buộc:

- `refreshToken`

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "accessToken": "<new-jwt-access-token>"
  }
}
```

FE flow:

- Gọi khi access token hết hạn.
- Nếu refresh thành công thì thay access token cũ bằng token mới.

### 4.3 Google login

`POST /api/auth/google-login`

Mục đích:

- FE lấy Google ID token từ Google Identity Services.
- Gửi ID token này lên BE để BE xác thực với Google.
- Nếu hợp lệ, BE phát hành `accessToken` và refresh cookie giống login thường.

Request body:

```json
{
  "idToken": "<google-id-token>"
}
```

Headers tùy chọn:

```http
X-Device-Id: device-001
```

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "accessToken": "<jwt-access-token>"
  }
}
```

FE flow:

1. Người dùng bấm Sign in with Google.
2. FE lấy `credential` hoặc `id_token` từ Google SDK.
3. Gửi `idToken` lên `/api/auth/google-login`.
4. Lưu `accessToken` và `X-Device-Id` như login thường.
5. Nếu muốn lấy profile đầy đủ, gọi thêm `GET /api/users/profile` hoặc `GET /api/users/me`.

Lưu ý:

- Google OAuth client id phải được cấu hình ở BE.
- BE chỉ chấp nhận token có `aud` đúng client id của ứng dụng.
- Email Google phải được xác minh.

### 4.4 Logout

`POST /api/auth/logout`

Headers:

```http
X-Device-Id: device-001
```

Cookie:

- `refreshToken`

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": "Đăng xuất thành công"
}
```

FE flow:

- Xóa access token local.
- Clear state người dùng.
- Điều hướng về màn login.

### 4.5 Verify token

`GET /api/auth/verify`

Mục đích:

- Kiểm tra token hiện tại còn hợp lệ hay không.
- Trả về trạng thái active của user.

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "email": "user@example.com",
    "roles": [],
    "active": true
  }
}
```

### 4.6 Register user thường

`POST /api/auth/register`

Request body:

```json
{
  "name": "Nguyen Van A",
  "email": "user@example.com",
  "password": "123456",
  "roleName": "ROLE_USER"
}
```

Lưu ý:

- BE tự set `roleName = ROLE_USER` trong controller.
- Tài khoản mới được tạo với `active = false`.
- FE nên bắt người dùng xác thực OTP sau khi đăng ký.

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "...",
    "name": "...",
    "email": "...",
    "role": "ROLE_USER",
    "avatar": null,
    "lastLogin": null,
    "previousLogin": null,
    "createdAt": "...",
    "passwordUpdatedAt": null,
    "online": false,
    "active": false
  }
}
```

### 4.7 Gửi OTP kích hoạt tài khoản

`POST /api/auth/send-otp`

Request body:

```json
{
  "email": "user@example.com"
}
```

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": "OTP đã gửi tới email"
}
```

Lưu ý:

- Endpoint này tìm user theo email đã đăng ký.
- Dùng sau khi register.

### 4.8 Xác thực OTP

`POST /api/auth/verify-otp`

Request body:

```json
{
  "userId": "user-id",
  "otp": "123456"
}
```

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": true
}
```

Nếu đúng OTP và còn hạn, user sẽ được set `active = true`.

## 5. Account API

### 5.1 Lấy thông tin user hiện tại

`GET /api/users/me`

Hoặc:

`GET /api/users`

Response `UserResponse`:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "...",
    "name": "Nguyen Van A",
    "email": "user@example.com",
    "role": "ROLE_USER",
    "avatar": "https://...",
    "lastLogin": "2026-04-11T10:00:00",
    "previousLogin": "2026-04-10T09:00:00",
    "createdAt": "2026-04-01T08:00:00",
    "passwordUpdatedAt": "2026-04-05T12:00:00",
    "online": true,
    "active": true
  }
}
```

FE dùng cho:

- profile menu
- header user info
- auth guard
- hiển thị trạng thái active/online

### 5.2 Lấy profile chi tiết

`GET /api/users/profile`

Response `UserProfileResponse`:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "...",
    "name": "Nguyen Van A",
    "email": "user@example.com",
    "avatar": "https://...",
    "phone": "0987654321",
    "gender": "MALE",
    "birthday": "1999-01-01",
    "bio": "...",
    "address": "Ha Noi",
    "createdAt": "2026-04-01T08:00:00"
  }
}
```

### 5.3 Cập nhật profile

`PUT /api/users/profile`

Request body `UpdateProfileRequest`:

```json
{
  "name": "Nguyen Van B",
  "phone": "0987654321",
  "gender": "MALE",
  "birthday": "1999-01-01",
  "bio": "Travel lover",
  "address": "Ha Noi"
}
```

Lưu ý:

- Field nào `null` thì BE giữ nguyên.
- `gender` hợp lệ: `MALE`, `FEMALE`, `OTHER`.

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "...",
    "name": "Nguyen Van B",
    "email": "user@example.com",
    "avatar": "...",
    "phone": "0987654321",
    "gender": "MALE",
    "birthday": "1999-01-01",
    "bio": "Travel lover",
    "address": "Ha Noi",
    "createdAt": "..."
  }
}
```

### 5.4 Cập nhật tên cơ bản thông tin account

`PUT /api/users/update-info`

Request body `UserUpdateRequest`:

```json
{
  "id": "user-id",
  "name": "New Name",
  "email": "new@example.com",
  "password": "",
  "contactLink": ""
}
```

Thực tế service hiện tại chỉ dùng `name` cho user đang đăng nhập.

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": {
    "id": "...",
    "name": "New Name",
    "email": "user@example.com"
  }
}
```

### 5.5 Đổi mật khẩu

`PUT /api/users/change-password`

Request body:

```json
{
  "currentPassword": "123456",
  "newPassword": "newPass123"
}
```

Rules:

- `currentPassword` phải đúng.
- `newPassword` tối thiểu 6 ký tự.

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": "Đổi mật khẩu thành công"
}
```

### 5.6 Quên mật khẩu

`POST /api/users/forgot-password`

Request body:

```json
{
  "email": "user@example.com"
}
```

Hành vi:

- BE sinh mật khẩu mới ngẫu nhiên.
- Mã hóa và lưu vào DB.
- Gửi mật khẩu mới qua email.

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": "Mật khẩu mới đã được gửi vào email của bạn."
}
```

FE chỉ cần:

- form nhập email
- thông báo thành công
- không cần màn reset password token

### 5.7 Ping activity

`POST /api/users/ping`

Mục đích:

- Cập nhật last activity để hiển thị online.

Response: `204/200` tùy cách framework serialize, body không quan trọng.

FE có thể gọi định kỳ khi user đang mở app, ví dụ mỗi 1-3 phút.

## 6. Admin account management API

Các API này chỉ dùng nếu FE có màn quản trị người dùng.

### 6.1 Tạo user thường bằng admin

`POST /api/users/register`

Yêu cầu role:

- `ROLE_ADMIN`

Request body:

```json
{
  "name": "User 1",
  "email": "user1@example.com",
  "password": "123456",
  "roleName": "ROLE_USER"
}
```

### 6.2 Tạo supplier

`POST /api/users/register/supplier`

Yêu cầu role:

- `ROLE_SUPPLIER_MANAGER`

Request body giống register user.

### 6.3 Lấy danh sách user theo role

`GET /api/users/by-role/{roleName}`

Ví dụ:

- `GET /api/users/by-role/ROLE_USER`
- `GET /api/users/by-role/ROLE_ADMIN`

Response:

```json
{
  "status": 200,
  "message": "Success",
  "data": [
    {
      "id": "...",
      "name": "...",
      "email": "...",
      "role": "ROLE_USER",
      "avatar": null,
      "lastLogin": null,
      "previousLogin": null,
      "createdAt": "...",
      "passwordUpdatedAt": null,
      "online": false,
      "active": true
    }
  ]
}
```

## 7. Enum FE cần map

### Gender

- `MALE`
- `FEMALE`
- `OTHER`

### Role

Dữ liệu hiện tại dùng dạng string role name:

- `ROLE_USER`
- `ROLE_ADMIN`
- `ROLE_SUPPLIER`
- `ROLE_SUPPLIER_MANAGER`

## 8. Luồng FE khuyến nghị

### 8.1 Đăng nhập

1. Gọi `POST /api/auth/login`
2. Lưu `accessToken`
3. Lưu `X-Device-Id`
4. Gọi `GET /api/users/me` hoặc `GET /api/users/profile` để lấy state user

### 8.2 Đăng ký + kích hoạt

1. Gọi `POST /api/auth/register`
2. Gọi `POST /api/auth/send-otp` với email
3. User nhập OTP
4. Gọi `POST /api/auth/verify-otp`
5. Nếu `true`, cho phép đăng nhập

### 8.3 Mở trang profile

1. Gọi `GET /api/users/profile`
2. Fill form
3. User chỉnh sửa
4. Gọi `PUT /api/users/profile`
5. Update lại state local

### 8.4 Đổi mật khẩu

1. Form nhập mật khẩu cũ và mới
2. Gọi `PUT /api/users/change-password`
3. Nếu thành công, yêu cầu user đăng nhập lại nếu app của bạn muốn chặt chẽ hơn

### 8.5 Quên mật khẩu

1. Form nhập email
2. Gọi `POST /api/users/forgot-password`
3. Hiển thị thông báo kiểm tra email

## 9. Gợi ý dữ liệu state FE

Nên giữ tối thiểu các state sau:

- `accessToken`
- `deviceId`
- `currentUser`
- `profile`
- `isAuthenticated`
- `online` hoặc `lastSeen`

## 10. Lưu ý triển khai

- FE nên tự động refresh token khi gặp `401`.
- Nếu refresh thất bại, redirect về login.
- Luôn gửi `Authorization` với access token cho các API account/profile.
- Với login response, đọc token từ body và `X-Device-Id` từ header, không phải từ body.
- Nếu app chạy local qua HTTP mà refresh cookie không hoạt động, đây là do cookie secure chứ không phải do FE lỗi.

## 11. Kết luận ngắn

Nếu bạn chỉ cần làm khu vực profile + account trên FE, thì 4 API quan trọng nhất là:

- `GET /api/users/profile`
- `PUT /api/users/profile`
- `PUT /api/users/change-password`
- `POST /api/auth/refresh`

Còn nếu làm full account flow thì thêm:

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/send-otp`
- `POST /api/auth/verify-otp`
- `POST /api/users/forgot-password`
- `POST /api/auth/logout`
