# BTL Lập trình Mobile - Music Player App

## Lưu ý:
- Tạo branch mới để commit rồi lên github tạo pull request
```agsl
    git checkout -b <name>
    git push origin <name>
```

- APP SỬ DỤNG TIẾNG ANH
- Không nên tạo Activity cho mỗi giao diện, chỉ cần hàm Composable là được
- Giao diện lớn để trong thư mục `ui/screens`, component tái sử dụng được để trong `ui/components`
- Logic xử lý để trong thư mục `model/...`
- Sử dụng [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel#jetpack-compose_1) để gọi hàm xử lý logic 
- Để navigate giữa các composable, **ĐỌC** [Jetpack Navigation](https://developer.android.com/guide/navigation/use-graph/navigate#events),
- Nếu cần navigate từ ViewModel thì đừng :D, dùng cách [này](https://stackoverflow.com/questions/76942195/navigate-using-view-model-in-jetpack-compose)
- Database sử dụng Room, doc ở [đây](https://developer.android.com/training/data-storage/room)
- Icon làm ơn tải file XML và import ở [đây](https://fonts.google.com/icons?icon.size=30&icon.color=%231f1f1f&icon.platform=android)
- Khi cần hiển thị danh sách bài hát, sử dụng composable SongList để có sẵn các chức năng và thống nhất giao diện
- Khi cần hiển thị ảnh của bài hát, playlist, album,... sử dụng composable ThumbnailImage để thống nhất giao diện