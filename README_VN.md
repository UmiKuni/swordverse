# Giới thiệu

**Tên:** Kiếm Giới

**Mô tả:** Trò chơi web trực tuyến 1v1 theo lượt, sử dụng cơ chế tự động chiến đấu và lấy chủ đề kiếm hiệp làm trọng tâm.

**Phiên bản hiện tại:** 1.0

# Cẩm nang

## :book: Cốt truyện

Bạn sẽ bước vào một thế giới nơi kiếm đạo là con đường dẫn đến sức mạnh tối thượng. Để trở thành một **SwordMaster**, bạn cần làm chủ các kiếm kỹ, kết hợp chúng thành những chuỗi đòn mãn nhãn hoặc tung ra một nhát kiếm tất sát để đánh bại những kiếm sĩ hùng mạnh.

## :video_game: Cách chơi

### 1. Xác thực tài khoản

Tạo tài khoản và đăng nhập vào trò chơi.

### 2. Chế độ tạo và tham gia phòng

Tạo một phòng chơi và gửi mã phòng cho bạn bè. Khi cả hai người chơi đều xác nhận sẵn sàng, trận đấu sẽ bắt đầu.

### 3. Trước trận đấu: Chọn Kiếm phái

Cả hai người chơi bí mật chọn một **Kiếm phái chính**. Lựa chọn của hai bên sẽ được công khai cùng lúc.

Sau đó, mỗi người tiếp tục chọn một **Kiếm phái hỗ trợ** và một **Kiếm kỹ hỗ trợ** thuộc Kiếm phái đó. Lựa chọn này cũng sẽ được công khai.

Mỗi Kiếm phái mang đến những đặc trưng riêng, bao gồm:

* Một bộ **Chỉ số**:

| Chỉ số                    | Viết tắt | Mô tả                                                                    |
| ------------------------- | :------: | ------------------------------------------------------------------------ |
| :crossed_swords: Sức mạnh |    STR   | Lượng sát thương bạn có thể gây ra cho đối thủ.                          |
| :heart: Sinh lực          |    HP    | Sinh lực của nhân vật. Khi HP giảm về 0, bạn thua trận.                  |
| :shield: Phòng thủ        |    DEF   | Giảm lượng sát thương nhận từ đối thủ.                                   |
| :zap: Tốc độ tấn công     |    AS    | Thể hiện tốc độ thực hiện đòn tấn công của nhân vật.                     |
| :sparkles: Mana           |    MP    | Tài nguyên được sử dụng để kích hoạt các Hành động.                      |
| :crystal_ball: Linh lực   |    QP    | Tài nguyên đặc biệt được sử dụng để kích hoạt một số Hành động đặc biệt. |

* Một tập hợp các **Kiếm kỹ** có thể sử dụng trong trận đấu. Mỗi Kiếm kỹ sở hữu hiệu ứng và mức tiêu hao MP hoặc QP riêng.

**Danh sách Hành động** cuối cùng của bạn sẽ bao gồm:

**Hành động cơ bản** + **3 Kiếm kỹ chính** + **1 Kiếm kỹ hỗ trợ**

### 4. Bắt đầu trận đấu

Sau khi cả hai người chơi hoàn tất việc chọn Kiếm phái, thông tin Kiếm phái của hai bên sẽ được công khai và trận đấu chính thức bắt đầu.

Một trận đấu diễn ra qua nhiều **Vòng đấu**. Mỗi Vòng đấu lần lượt trải qua **4 Giai đoạn**:

#### 4.1. :hourglass_flowing_sand: Giai đoạn LUÂN CHUYỂN — RENEWAL

* Xử lý tất cả **Hiệu ứng** đang tồn tại.
* Chuyển đổi toàn bộ **MP** còn dư thành **QP** theo tỉ lệ phụ thuộc vào đặc trưng của Kiếm phái.
* Hồi phục toàn bộ **MP**.

#### 4.2. :star: Giai đoạn TU LUYỆN — ASCENSION

* Trong mỗi Vòng đấu, cả hai người chơi nhận được 2 **Điểm Tu luyện (LP)**.
* Người chơi bí mật phân bổ LP để nâng cấp **Chỉ số**, học Kiếm kỹ mới hoặc nâng cấp một **Kiếm kỹ** đã sở hữu.
* Sau khi cả hai người chơi hoàn tất và xác nhận việc phân bổ LP, các thay đổi về Chỉ số và Kiếm kỹ sẽ được công khai. Trận đấu sau đó chuyển sang giai đoạn tiếp theo.

#### 4.3. :scroll: Giai đoạn CHIẾN LƯỢC HÀNH ĐỘNG — ACTION STRATEGY

* Mỗi người chơi sở hữu một **HÀNG ĐỢI HÀNH ĐỘNG — ACTION QUEUE**. Các Hành động trong hàng đợi sẽ được tự động thực hiện trong Giai đoạn Chiến đấu.
* Trong mỗi Vòng đấu, người chơi phải điền đủ số lượng **Hành động** cần thiết vào Hàng đợi hành động.
* Quá trình điều chỉnh Hàng đợi hành động diễn ra theo thứ tự:

  1. Xóa tối đa 1 **Hành động** đang có khỏi **Hàng đợi hành động**.
  2. Thêm các **Hành động** mới từ **Kiếm kỹ** hoặc **Hành động cơ bản** vào đầu, cuối hoặc giữa những Hành động hiện có.

|     Vòng đấu     | Kích thước Hàng đợi hành động |
| :--------------: | :---------------------------: |
|         1        |               2               |
|         2        |               3               |
|         3        |               4               |
|         4        |               5               |
|         5        |               6               |
| Từ vòng 6 trở đi |               7               |

* Khi cả hai người chơi đã hoàn tất và xác nhận một **Hàng đợi hành động** hợp lệ, trận đấu sẽ chuyển sang Giai đoạn Chiến đấu.

#### 4.4. :dart: Giai đoạn CHIẾN ĐẤU — BATTLE

Mỗi **Hành động** trong **Hàng đợi hành động** sẽ lần lượt được công khai và thực hiện. Kết quả của từng Hành động được hiển thị trong **Nhật ký chiến đấu — Battle Log**.

Giai đoạn Chiến đấu tiếp tục cho đến khi:

* Tất cả Hành động trong Hàng đợi hành động đã được thực hiện; hoặc
* Một trong hai người chơi đáp ứng **Điều kiện kết thúc trận đấu**.

Nếu toàn bộ Hành động đã được thực hiện nhưng chưa có người chơi nào đáp ứng Điều kiện kết thúc trận đấu, một Vòng đấu mới sẽ bắt đầu.

#### 4.5. :trophy: Điều kiện KẾT THÚC TRẬN ĐẤU — MATCH END

Một người chơi giành chiến thắng khi xảy ra một trong các trường hợp sau:

* HP của đối thủ giảm về 0.
* Đối thủ đầu hàng.
* Đối thủ mất kết nối trong hơn 5 phút.

Khi một trong các điều kiện trên được đáp ứng, trận đấu kết thúc ngay lập tức và kết quả của cả hai người chơi sẽ được hiển thị trên Bảng kết quả trận đấu.

# Công nghệ sử dụng

## Game Client

* React.js để xây dựng các thành phần giao diện của trò chơi.
* Redux Toolkit để quản lý trạng thái toàn cục và RTK Query để gọi API.
* React Router để quản lý định tuyến.
* STOMP để giao tiếp qua WebSocket.

## Game Server

* Spring Boot để xây dựng REST API.
* Spring WebSocket để giao tiếp qua WebSocket.

# Cách chạy dự án

1. Clone repository.
2. Cài đặt các dependency cho cả client và server.
3. Chạy các lệnh khởi động dự án.
   ::: 
