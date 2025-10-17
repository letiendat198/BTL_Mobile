# BTL Lập trình Mobile - Music Player App

## Lưu ý:
- Tạo branch mới để commit rồi lên github tạo pull request
```agsl
    git checkout -b <name>
    git push origin <name>
```

- Không nên tạo Activity cho mỗi giao diện, chỉ cần hàm Composable là được
- Giao diện lớn để trong thư mục `ui/screens`, component tái sử dụng được để trong `ui/components`
- Logic xử lý để trong thư mục `model/...`
- Sử dụng [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel#jetpack-compose_1) để gọi hàm xử lý logic 
- Để navigate giữa các composable, dùng [Jetpack Navigation](https://developer.android.com/guide/navigation),
không cần dùng startActivity hay Fragments giống XML
- Nếu cần navigate từ ViewModel thì đừng :D, dùng cách [này](https://stackoverflow.com/questions/76942195/navigate-using-view-model-in-jetpack-compose)
- Database sử dụng Room, doc ở [đây](https://developer.android.com/training/data-storage/room)
- Để test giao diện của mình khi MainActivity chưa làm gì, sửa phần setContent trong MainActivity