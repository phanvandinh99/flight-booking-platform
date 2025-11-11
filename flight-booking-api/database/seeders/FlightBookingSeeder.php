<?php

namespace Database\Seeders;

use Illuminate\Database\Seeder;
use App\Models\SanBay;
use App\Models\HangHangKhong;
use App\Models\NguoiDung;
use App\Models\TuyenBay;
use App\Models\MayBay;
use App\Models\ChuyenBay;
use App\Models\GiaVe;
use Illuminate\Support\Facades\Hash;

class FlightBookingSeeder extends Seeder
{
    public function run()
    {
        // === 1. SÂN BAY VIỆT NAM ===
        $airports = [
            ['SGN', 'Sân bay Tân Sơn Nhất', 'Thành phố Hồ Chí Minh', 'TP. Hồ Chí Minh'],
            ['HAN', 'Sân bay Nội Bài', 'Hà Nội', 'Hà Nội'],
            ['DAD', 'Sân bay Đà Nẵng', 'Đà Nẵng', 'Đà Nẵng'],
            ['CXR', 'Sân bay Cam Ranh', 'Cam Ranh', 'Khánh Hòa'],
            ['HPN', 'Sân bay Cát Bi', 'Hải Phòng', 'Hải Phòng'],
            ['VCL', 'Sân bay Chu Lai', 'Chu Lai', 'Quảng Nam'],
            ['VCA', 'Sân bay Cần Thơ', 'Cần Thơ', 'Cần Thơ'],
            ['PQC', 'Sân bay Phú Quốc', 'Phú Quốc', 'Kiên Giang'],
            ['THD', 'Sân bay Thọ Xuân', 'Thanh Hóa', 'Thanh Hóa'],
            ['VCS', 'Sân bay Côn Đảo', 'Côn Đảo', 'Bà Rịa – Vũng Tàu'],
            ['UIH', 'Sân bay Phù Cát', 'Quy Nhơn', 'Bình Định'],
            ['VDO', 'Sân bay Vân Đồn', 'Vân Đồn', 'Quảng Ninh'],
            ['DLK', 'Sân bay Liên Khương', 'Đà Lạt', 'Lâm Đồng'],
            ['BMV', 'Sân bay Buôn Ma Thuột', 'Buôn Ma Thuột', 'Đắk Lắk'],
            ['PXU', 'Sân bay Pleiku', 'Pleiku', 'Gia Lai'],
            ['DIN', 'Sân bay Đồng Hới', 'Đồng Hới', 'Quảng Bình'],
            ['NHA', 'Sân bay Nha Trang (T90)', 'Nha Trang', 'Khánh Hòa'],
            ['CAH', 'Sân bay Cà Mau', 'Cà Mau', 'Cà Mau'],
            ['VKG', 'Sân bay Rạch Giá', 'Rạch Giá', 'Kiên Giang'],
        ];

        foreach ($airports as $ap) {
            SanBay::updateOrCreate(
                ['ma_san_bay' => $ap[0]],
                [
                    'ten_san_bay' => $ap[1],
                    'thanh_pho' => $ap[2],
                    'quoc_gia' => 'Việt Nam'
                ]
            );
        }

        // === 2. HÃNG HÀNG KHÔNG VIỆT NAM ===
        $airlines = [
            ['Vietnam Airlines', 'VN'],
            ['Vietjet Air', 'VJ'],
            ['Bamboo Airways', 'QH'],
            ['Pacific Airlines', 'BL'],
        ];

        $airlineIds = [];
        foreach ($airlines as $al) {
            $airline = HangHangKhong::updateOrCreate(
                ['ma_hang' => $al[1]],
                [
                    'ten_hang' => $al[0],
                    'trang_thai' => 'hoat_dong'
                ]
            );
            $airlineIds[$al[1]] = $airline->id;
        }

        // === 3. NGƯỜI DÙNG MẪU ===
        NguoiDung::updateOrCreate(
            ['email' => 'admin@gmail.com'],
            [
                'ten_day_du' => 'Admin Hệ Thống',
                'mat_khau' => Hash::make('Abc123'),
                'vai_tro' => 'admin'
            ]
        );

        NguoiDung::updateOrCreate(
            ['email' => 'vn@gmail.com'],
            [
                'ten_day_du' => 'Đại Diện Vietnam Airlines',
                'mat_khau' => Hash::make('Abc123'),
                'vai_tro' => 'dai_dien_hang',
                'ma_hang_hang_khong' => $airlineIds['VN']
            ]
        );

        NguoiDung::updateOrCreate(
            ['email' => 'giahuy@gmail.com'],
            [
                'ten_day_du' => 'Khách Hàng Mẫu',
                'mat_khau' => Hash::make('Abc123'),
                'vai_tro' => 'khach_hang'
            ]
        );

        // === 4. TUYẾN BAY NỘI ĐỊA PHỔ BIẾN ===
        $routes = [
            ['SGN', 'HAN'],
            ['HAN', 'SGN'],
            ['SGN', 'DAD'],
            ['DAD', 'SGN'],
            ['HAN', 'DAD'],
            ['DAD', 'HAN'],
            ['SGN', 'PQC'],
            ['PQC', 'SGN'],
            ['HAN', 'PQC'],
            ['PQC', 'HAN'],
            ['SGN', 'CXR'],
            ['CXR', 'SGN'],
            ['HAN', 'CXR'],
            ['CXR', 'HAN'],
            ['SGN', 'VDO'],
            ['VDO', 'SGN'],
            ['HAN', 'VDO'],
            ['VDO', 'HAN'],
            ['DAD', 'PQC'],
            ['PQC', 'DAD'],
        ];

        foreach ($routes as $route) {
            $origin = SanBay::where('ma_san_bay', $route[0])->first();
            $dest = SanBay::where('ma_san_bay', $route[1])->first();
            if ($origin && $dest) {
                TuyenBay::updateOrCreate(
                    [
                        'san_bay_di' => $origin->id,
                        'san_bay_den' => $dest->id
                    ],
                    [
                        'duoc_phe_duyet' => true
                    ]
                );
            }
        }

        // === 5. MÁY BAY THEO HÃNG (dữ liệu thực tế ước lượng) ===
        $aircrafts = [
            // Vietnam Airlines
            ['ma_hang' => 'VN', 'loai' => 'Boeing 787-9', 'ghe' => 270],
            ['ma_hang' => 'VN', 'loai' => 'Airbus A321', 'ghe' => 180],
            // Vietjet Air
            ['ma_hang' => 'VJ', 'loai' => 'Airbus A321neo', 'ghe' => 230],
            ['ma_hang' => 'VJ', 'loai' => 'Airbus A320', 'ghe' => 180],
            // Bamboo Airways
            ['ma_hang' => 'QH', 'loai' => 'Airbus A321neo', 'ghe' => 220],
            ['ma_hang' => 'QH', 'loai' => 'Boeing 787-9', 'ghe' => 290],
            // Pacific Airlines
            ['ma_hang' => 'BL', 'loai' => 'Airbus A320', 'ghe' => 180],
        ];

        foreach ($aircrafts as $ac) {
            MayBay::updateOrCreate(
                [
                    'ma_hang_hang_khong' => $airlineIds[$ac['ma_hang']],
                    'loai_may_bay' => $ac['loai']
                ],
                [
                    'tong_so_ghe' => $ac['ghe'],
                    'so_do_ghe' => json_encode([
                        'pho_thong' => ['1A', '1B', '1C', '1D', '1E', '1F', '2A', '2B'],
                        'thuong_gia' => $ac['ma_hang'] === 'VN' || $ac['ma_hang'] === 'QH'
                            ? ['10A', '10B', '10C'] : null
                    ])
                ]
            );
        }

        // === 6. CHUYẾN BAY MẪU (tạo nhiều chuyến bay với giờ khác nhau để tránh trùng) ===
        $sampleFlights = [
            ['VN', 'VN123', 'SGN', 'HAN'],
            ['VJ', 'VJ456', 'SGN', 'HAN'],
            ['QH', 'QH201', 'HAN', 'DAD'],
            ['BL', 'BL601', 'SGN', 'PQC'],
            ['VN', 'VN890', 'DAD', 'PQC'],
            ['VJ', 'VJ789', 'SGN', 'DAD'],
            ['QH', 'QH301', 'SGN', 'CXR'],
            ['VN', 'VN234', 'HAN', 'PQC'],
            ['VJ', 'VJ567', 'DAD', 'SGN'],
            ['BL', 'BL702', 'HAN', 'SGN'],
        ];

        foreach ($sampleFlights as $flight) {
            $airlineId = $airlineIds[$flight[0]];
            $origin = SanBay::where('ma_san_bay', $flight[2])->first();
            $dest = SanBay::where('ma_san_bay', $flight[3])->first();
            $route = TuyenBay::where('san_bay_di', $origin->id)
                ->where('san_bay_den', $dest->id)->first();

            if ($route) {
                // Chọn máy bay ngẫu nhiên của hãng
                $aircraft = MayBay::where('ma_hang_hang_khong', $airlineId)->inRandomOrder()->first();

                // Tạo chuyến bay cho 7 ngày tới với giờ khác nhau để tránh trùng
                for ($i = 1; $i <= 7; $i++) {
                    // Tạo giờ khởi hành khác nhau: 6h, 8h, 10h, 12h, 14h, 16h, 18h, 20h
                    $hourOffset = (($i - 1) % 8) * 2 + 6; // 6, 8, 10, 12, 14, 16, 18, 20
                    $departure = now()->addDays($i)->setTime($hourOffset, 0);
                    $arrival = $departure->copy()->addHours(2)->addMinutes(15);

                    $chuyenBay = ChuyenBay::updateOrCreate(
                        [
                            'ma_chuyen_bay' => $flight[1],
                            'gio_khoi_hanh' => $departure
                        ],
                        [
                            'ma_hang_hang_khong' => $airlineId,
                            'ma_may_bay' => $aircraft->id,
                            'ma_tuyen_bay' => $route->id,
                            'gio_ha_canh' => $arrival,
                            'tan_suat' => 'hang_ngay',
                            'trang_thai' => 'du_kien'
                        ]
                    );

                    // Tạo giá vé cho các hạng vé
                    // Giá cơ bản thay đổi theo tuyến bay (khoảng cách)
                    $basePrice = 1000000; // Giá cơ bản

                    // Tất cả hãng đều có hạng phổ thông
                    $priceMultipliers = [
                        'pho_thong' => 1.0,
                    ];

                    // Chỉ Vietnam Airlines và Bamboo Airways có hạng thương gia và hạng nhất
                    if ($flight[0] === 'VN' || $flight[0] === 'QH') {
                        $priceMultipliers['thuong_gia'] = 2.0;
                        $priceMultipliers['hang_nhat'] = 3.5;
                    }

                    // Ngày bắt đầu tự động là ngày hiện tại
                    $ngayBatDau = now()->startOfDay();
                    // Ngày kết thúc là 3 tháng sau ngày bắt đầu
                    $ngayKetThuc = $ngayBatDau->copy()->addMonths(3)->endOfDay();

                    foreach ($priceMultipliers as $hangVe => $multiplier) {
                        GiaVe::updateOrCreate(
                            [
                                'ma_chuyen_bay' => $chuyenBay->id,
                                'hang_ve' => $hangVe,
                                'ngay_bat_dau' => $ngayBatDau
                            ],
                            [
                                'gia' => round($basePrice * $multiplier),
                                'hanh_ly_ky_gui' => '20kg',
                                'chinh_sach_huy_ve' => 'Hủy trước 24h: hoàn 80%. Hủy sau 24h: không hoàn tiền.',
                                'chinh_sach_doi_ve' => 'Đổi vé trước 24h: miễn phí. Đổi sau 24h: phí 200.000 VNĐ.',
                                'ngay_ket_thuc' => $ngayKetThuc
                            ]
                        );
                    }
                }
            }
        }
    }
}
