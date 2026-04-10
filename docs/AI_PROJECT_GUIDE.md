# AI Project Guide - Backend GoTogether

Tai lieu nay la nguon tham chieu cho AI/dev khi mo rong codebase, giup giu kien truc nhat quan va giam sai lech quy uoc.

## 1) Tong quan du an

- Ten: `backend_gotogether`
- Nen tang: Spring Boot 3.5.x, Java 21, Maven
- Kieu ung dung: REST API + JWT auth + WebSocket/STOMP
- Persistence: Spring Data JPA + MySQL
- Media: Cloudinary
- API docs: OpenAPI/Swagger

## 2) Cau truc thu muc chinh

- `src/main/java/com/vn/gotogether/config`: security, cors, websocket, response wrapper
- `src/main/java/com/vn/gotogether/controller`: API theo domain (`auth`, `data`, `user`)
- `src/main/java/com/vn/gotogether/service`: xu ly business logic theo domain
- `src/main/java/com/vn/gotogether/repository`: JPA repositories
- `src/main/java/com/vn/gotogether/entity`: entities
- `src/main/java/com/vn/gotogether/dto`: DTO request/response
- `src/main/java/com/vn/gotogether/exception`: exception va global exception handler
- `src/main/java/com/vn/gotogether/utils`: helper (JWT, security util, ...)
- `src/main/resources/application.yaml`: config runtime

## 3) Luong xu ly mac dinh (kien truc)

- Controller nhan request, validate (`@Valid`), lay current user tu `Authentication`/`SecurityContextHolder`.
- Service chua toan bo business rules.
- Repository thao tac DB.
- Tra response:

1. Controller tra object/DTO thong thuong -> duoc `GlobalResponseWrapper` boc thanh `ApiResponse.success(data)`.
2. Neu tra `ApiResponse` hoac `ErrorResponse` thi giu nguyen (khong boc lai).

## 4) Quy uoc API response va loi

- Thanh cong:

1. Uu tien tra DTO/object; wrapper tu dong se boc ve format chung.
2. Co the tra `ResponseEntity` khi can custom status.

- Loi:

1. Su dung exception domain (`InvalidDataException`, `ResourceNotFoundException`, ...).
2. `GlobalExceptionHandler` map exception -> HTTP status + payload loi.

Khuyen nghi khi mo rong:

- Khong tu y tao format response moi neu chua can thiet.
- Uu tien dung lai exception da co; chi tao exception moi khi co nghia nghiep vu ro rang.

## 5) Quy uoc Security

Hien tai `SecurityConfig` cho phep nhieu endpoint `permitAll`, va bat auth cho mot so route quan tri/ghi du lieu.

Khi them API moi:

1. Xac dinh endpoint co can auth khong.
2. Neu can auth, cap nhat matcher trong `SecurityConfig`.
3. Trong service, check quyen theo business (owner/collaborator/admin), khong chi dua vao route-level auth.

JWT:

- Secret va token config nam trong `application.yaml` (`jwt.*`).
- Authorities luu trong claim `authorities`.

## 6) Quy uoc theo domain (rut ra tu code hien tai)

- `itinerary` la module nghiep vu lon: create, clone, list, detail, media, invite, warning.
- Pattern thuong gap:

1. Lay user theo email tu `Authentication` qua `UserRepository`.
2. Validate entity ton tai truoc khi xu ly.
3. Kiem tra permission trong service.
4. Tra DTO de han che lo entity ra ngoai.

## 7) Quy tac khi them tinh nang moi

Checklist bat buoc:

1. Tao/bo sung DTO request-response trong `dto/*`.
2. Them endpoint o `controller/*` (chi giu logic mong).
3. Dua business logic vao `service/*`.
4. Them/truy van repository trong `repository/*`.
5. Neu co luat phan quyen, bo sung check trong service.
6. Xu ly loi bang exception da co.
7. Dam bao response khong vo tinh bi double-wrap.
8. Cap nhat Swagger annotation neu can.
9. Viet test toi thieu cho happy path va permission path.

## 8) Quy tac dat ten va style

- Ten package theo domain: `controller.data`, `service.user`, `repository.auth`, ...
- Ten class ro vai tro: `XxxController`, `XxxService`, `XxxRepository`.
- Uu tien constructor injection (`@RequiredArgsConstructor`).
- Tranh de logic nghiep vu lon trong controller.
- Khong tra truc tiep entity neu co the tra DTO.

## 9) Quy tac du lieu nhay cam va config

Canh bao: file `application.yaml` hien co gia tri nhay cam (mail, db, cloudinary).

Khuyen nghi:

1. Dua secret sang bien moi truong (ENV) hoac secret manager.
2. Giu `application.yaml` o dang placeholder.
3. Tach profile `application-dev.yaml`, `application-prod.yaml`.

## 10) Mau quy trinh mo rong nhanh cho AI

Khi AI duoc giao task moi, nen di theo thu tu:

1. Tim domain lien quan (controller/service/repository/entity/dto).
2. Doc endpoint cu cung domain de tai su dung pattern.
3. Xac dinh tac dong den security va response wrapper.
4. Trien khai thay doi nho nhat co the, giu style hien co.
5. Chay build/test nhanh, sua loi compile neu co.
6. Ghi chu tai lieu neu co convention moi.

## 11) Lenh thuong dung

- Build: `./mvnw clean package` (Windows: `mvnw.cmd clean package`)
- Test: `./mvnw test` (Windows: `mvnw.cmd test`)
- Run dev: `./mvnw spring-boot:run`

## 12) Dinh huong cap nhat tai lieu nay

Cap nhat file nay moi khi co mot trong cac thay doi sau:

1. Them module/domain moi.
2. Doi quy tac response/exception.
3. Doi chinh sach security endpoint.
4. Doi cau truc package hoac coding convention.

Muc tieu: AI/doc co the doc file nay truoc, sau do moi di sau vao code de thuc hien task nhanh va dung quy uoc.
